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
    private Observable<List<JSONObject>> timer;
    private Observable<List<JSONObject>> core;
    private Subscription subscription;
    private Observer<List<JSONObject>> metricObserver;

    private Scheduler persistScheduler;
    private Scheduler networkScheduler;

    private RakeLogger debugLogger;

    public RakeCore(RakeDao dao, RakeHttpClient client, RakeUserConfig config) {
        this.dao    = dao;
        this.client = client;
        this.config = config;
        this.debugLogger = RakeLoggerFactory.getLogger(this.getClass(), config);

        this.timer = Observable
                .interval(config.getFlushIntervalAsMilliseconds() ,TimeUnit.SECONDS)
                .startWith(-1L) /* flush when app starts */
                .map(x -> {
                    debugLogger.i("Timer fired");
                    return null;
                });

        this.flushable = PublishSubject.create();
        this.trackable = PublishSubject.create();

        this.persistScheduler = Schedulers.computation();
        this.networkScheduler = Schedulers.io();

        this.metricObserver = new Observer<List<JSONObject>>() {
            @Override
            public void onCompleted() {
                debugLogger.i("RakeCore onCompleted");
            }

            @Override
            public void onError(Throwable t) {
                debugLogger.e("RakeCore.onError", t);
            }

            @Override
            public void onNext(List<JSONObject> metric) {
                if (null == metric) return;
                debugLogger.i("Observer Thread: " + Thread.currentThread().getName());
                debugLogger.i("metric: " + metric.toString());
            }
        };

        buildCore(config, persistScheduler, networkScheduler, metricObserver);
    }

    public void buildCore(RakeUserConfig config,
                          Scheduler persistScheduler,
                          Scheduler networkScheduler,
                          Observer<List<JSONObject>> observer) {

        startWithDefaultCore()
                .withTimer(config.getFlushIntervalAsMilliseconds())
                .withPersistence(persistScheduler)
                .withNetworking(networkScheduler)
                .withRetry(persistScheduler)
                .endWithObserver(observer);

        // TODO send metric in network scheduler
        // TODO compute metric in persist scheduler
    }

    public RakeCore startWithDefaultCore() {
        if (null != subscription && !subscription.isUnsubscribed())
            subscription.unsubscribe();

        core = trackable.mergeWith(flushable);

        return this;
    }

    public RakeCore withTimer(int flushInterval /* milliseconds */) {
        Observable<List<JSONObject>> timer = Observable
                .interval(flushInterval,TimeUnit.SECONDS)
                .startWith(-1L) /* flush when app starts */
                .map(x -> {
                    debugLogger.i("Timer fired");
                    return null;
                });

        core = core.mergeWith(timer);

        return this;
    }

    public RakeCore withPersistence(Scheduler persistScheduler) {
        core = core
                .observeOn(persistScheduler) /* dao access in IO thread */
                .map(nullOrJsonList -> {
                    /* if null == nullOrJson, timer was fired or flush was commanded */
                    debugLogger.i("Persisting Thread: " + Thread.currentThread().getName());

                    /* if -1, null */
                    int totalCount = dao.add(nullOrJsonList);

                    if (-1 == totalCount /* timer was fired or, flush was commanded */
                            || totalCount >= config.getMaxLogTrackCount() /* persistence is full */
                            || RakeUserConfig.RUNNING_ENV.DEV == config.getRunningMode()) /* dev mode */ {
                        return dao.getAndRemoveOldest(config.getMaxLogTrackCount());
                    }

                    /* track is called, but persistence is not full  */
                    return null;
                }).filter(nullOrJsonList -> null != nullOrJsonList);

        return this;
    }

    public RakeCore withNetworking(Scheduler networkScheduler) {
        core = core
                .observeOn(networkScheduler) /* network operation in another IO thread */
                .map(tracked -> {
                    debugLogger.i("Networking Thread: " + Thread.currentThread().getName());
                    debugLogger.i("sent log count: " + tracked.size());
                    return client.send(tracked); /* return response. it might be null */
                });

        return this;
    }

    /* retry means that persisting all failed log into SQLite */
    public RakeCore withRetry(Scheduler persistScheduler) {
        core = core
                .map(failed -> {
                    // TODO metric,, write retry tests
                    dao.add(failed);
                    // TODO returning meaningful things
                    return null;
                });

        return this;
    }

    public void endWithObserver(Observer<List<JSONObject>> observer) {
        subscription = core
                .onErrorReturn(t -> {
                    // TODO: onErrorReturn
                    // TODO metric
                    RakeLogger.e("exception occurred. onErrorReturn", t);
                    return null;
                })
                        // TODO subscribeOn or observeOn
                .subscribe(observer);
    }

    public void track(JSONObject json) {
        if (null == json) return;

        debugLogger.i("track called: \n" + json.toString());
        trackable.onNext(Arrays.asList(json));

    }

    public void flush() {
        debugLogger.i("flush called");
        flushable.onNext(null);
    }
}
