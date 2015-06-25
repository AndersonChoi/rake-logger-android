package com.skp.di.rake.client.core;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.utils.RakeLogger;
import com.skp.di.rake.client.utils.RakeLoggerFactory;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Not thread-safe.
 * Instances of this class should only be used by a single thread.
 */
public class RakeCore {
    private RakeDao dao;
    private RakeHttpClient client;
    private RakeUserConfig config;

    private PublishSubject<List<JSONObject>> flushable;
    private PublishSubject<List<JSONObject>> trackable;
    private PublishSubject<List<JSONObject>> timer;
    private PublishSubject<List<JSONObject>> stop; /* to stop `timer` */
    private Observable<List<JSONObject>> core;
    private Subscription intervalSubscription;
    private Subscription coreSubscription;
    private Observer<List<JSONObject>> metricObserver;

    private Scheduler persistScheduler;
    private Scheduler networkScheduler;

    private RakeLogger debugLogger;

    public RakeCore(RakeDao dao,
                    RakeHttpClient client,
                    RakeUserConfig config) {
        this.dao    = dao;
        this.client = client;
        this.config = config;
        this.debugLogger = RakeLoggerFactory.getLogger(this.getClass(), config);

        this.flushable = PublishSubject.create();
        this.trackable = PublishSubject.create();
        this.timer = PublishSubject.create();
        this.stop = PublishSubject.create();
        this.intervalSubscription = null;

        this.persistScheduler = Schedulers.computation(); /* usually Schedulers.computation() */
        this.networkScheduler = Schedulers.io(); /* usually Schedulers.io() */

        this.metricObserver = new Observer<List<JSONObject>>() {
            @Override
            public void onCompleted() {
                debugLogger.i("RakeCore.onCompleted");
                resubscribeCore();
            }

            @Override
            public void onError(Throwable t) {
                debugLogger.e("RakeCore.onError", t);
                resubscribeCore();
            }

            @Override
            public void onNext(List<JSONObject> metric) {
                if (null == metric) return;
                debugLogger.i("Observer Thread: " + Thread.currentThread().getName());
                debugLogger.i("metric: " + metric.toString());
            }
        };

        // merging flushable, trackable, timer with
        // schedulers that define which thread will be used
        buildCore(config, persistScheduler, networkScheduler, metricObserver);
    }

    private void resubscribeCore() {
        if (null != coreSubscription && !coreSubscription.isUnsubscribed())
            coreSubscription.unsubscribe();

        coreSubscription = core.subscribe(this.metricObserver);
    }

    private void buildCore(RakeUserConfig config,
                          Scheduler persistScheduler,
                          Scheduler networkScheduler,
                          Observer<List<JSONObject>> observer) {

        if (RakeUserConfig.RUNNING_ENV.DEV == config.getRunningMode()) {
            startWithDefaultCore()
                    /* without timer */
                    /* without persistence */
                    .withNetworking(networkScheduler)
                    .withRetry(persistScheduler)
                    .endWithObserver(observer);
        } else { /* LIVE */
            startWithDefaultCore()
                    .withTimer(config.getFlushIntervalAsMilliseconds())
                    .withPersistence(persistScheduler)
                    .withNetworking(networkScheduler)
                    .withRetry(persistScheduler)
                    .endWithObserver(observer);
        }

        // TODO send metric in network scheduler
        // TODO compute metric in persist scheduler
    }

    private RakeCore startWithDefaultCore() {
        if (null != coreSubscription && !coreSubscription.isUnsubscribed())
            coreSubscription.unsubscribe();

        core = trackable.mergeWith(flushable);

        return this;
    }

    private RakeCore withTimer(long flushInterval /* milliseconds */) {
        setFlushInterval(flushInterval);
        core = core.mergeWith(timer);

        return this;
    }

    private RakeCore withPersistence(Scheduler persistScheduler) {
        core = core
                .observeOn(persistScheduler) /* dao access in IO thread */
                .map(nullOrJsonList -> {
                    int totalCount = -1;

                    if (null != nullOrJsonList /* if track */) {
                        debugLogger.i("Persisting Thread: " + Thread.currentThread().getName());
                        totalCount = dao.add(nullOrJsonList);
                    }

                    if (null == nullOrJsonList /* timer or flush */
                            || totalCount >= config.getMaxLogTrackCount()) { /* dao is full */
                        return dao.getAndRemoveOldest(config.getMaxLogTrackCount());
                    }

                    /* track is called, but persistence is not full  */
                    return null;
                });

        return this;
    }

    private RakeCore withNetworking(Scheduler networkScheduler) {
        core = core
                .filter(nullOrJsonList -> null != nullOrJsonList) /* must filter null */
                .observeOn(networkScheduler) /* network operation in another IO thread */
                .map(tracked -> {
                    debugLogger.i("Networking Thread: " + Thread.currentThread().getName());
                    debugLogger.i("sent log count: " + tracked.size());
                    return client.send(tracked); /* return tracked if failed, otherwise return null */
                });

        return this;
    }

    /* retry means that persisting all failed log into SQLite */
    private RakeCore withRetry(Scheduler persistScheduler) {
        core = core
                .map(failed -> {
                    /* iff failed */
                    if (null != failed) {
                        // TODO metric
                        debugLogger.i("Failed. retrying log count: " + failed.size());
                        track(failed);
                    }

                    // TODO returning meaningful things
                    return null;
                });

        return this;
    }

    private void endWithObserver(Observer<List<JSONObject>> observer) {
        coreSubscription = core
                .subscribe(observer); // TODO subscribeOn or observeOn
    }

    public void track(JSONObject json) {
        if (null == json) return;

        debugLogger.i("track called: \n" + json.toString());
        track(Arrays.asList(json));
    }

    private void track(List<JSONObject> jsons) {
        if (null == jsons || 0 == jsons.size()) return;

        trackable.onNext(jsons);
    }

    public void flush() {
        debugLogger.i("flush called");
        flushable.onNext(null);
    }

    public void setFlushInterval(long milliseconds) {
        if (RakeUserConfig.RUNNING_ENV.DEV == this.config.getRunningMode()) {
            debugLogger.i("setFlushInterval is not supported in `RUNNING_ENV.DEV`.");
            return;
        }

        if (null != intervalSubscription && ! intervalSubscription.isUnsubscribed()) {
            stop.onNext(null); /* stop command */
            intervalSubscription.unsubscribe();
        }

        // TODO subscribeOn ComputationThread
        intervalSubscription = Observable
                .interval(milliseconds ,TimeUnit.MILLISECONDS)
                .startWith(-1L) /* flush when app starts */
                .takeUntil(stop)
                .map(x -> {
                    debugLogger.i("Timer fired");
                    this.timer.onNext(null);
                    return x;
                })
                .onErrorResumeNext(t -> null) /* ignore timer errors */
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
                        debugLogger.i("Old timer will be perished.");
                    }

                    @Override
                    public void onError(Throwable e) {
                        debugLogger.i("onError in Observable.Interval for timer ");
                    }

                    @Override
                    public void onNext(Object o) { /* do nothing */ }
                });
    }

    /* to support test */
    public Observable<List<JSONObject>> getTimer() { return timer; }
    public Observable<List<JSONObject>> getTrackable() { return timer; }

    /* to support test */
    public void setTestObserverAndScheduler(Scheduler s, Observer<List<JSONObject>> o) {
        buildCore(this.config, s, s, o);
    }
}
