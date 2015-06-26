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

    private PublishSubject<List<JSONObject>> trackable;
    private PublishSubject<Command> flushable;
    private PublishSubject<List<JSONObject>> stop; /* to stop `timer` */

    private Observable<Command> command;
    private Observable<List<JSONObject>> stream;
    private Subscription timerSubscription;
    private Subscription streamSubscription;
    private Observer<List<JSONObject>> streamObserver;
    private Observer<Long> timerObserver;

    private Scheduler persistScheduler;
    private Scheduler networkScheduler;

    private RakeLogger debugLogger;

    public enum Command {
        TIMER_FIRED, FLUSHED, TRACK_FULL, TRACK_NOT_FULL
    }

    public RakeCore(RakeDao dao,
                    RakeHttpClient client,
                    RakeUserConfig config) {
        this.dao    = dao;
        this.client = client;
        this.debugLogger = RakeLoggerFactory.getLogger(this.getClass(), config);

        this.flushable = PublishSubject.create();
        this.trackable = PublishSubject.create();
        this.stop = PublishSubject.create();
        this.timerSubscription = null;

        this.config = config;
        this.persistScheduler = Schedulers.computation(); /* usually Schedulers.computation() */
        this.networkScheduler = Schedulers.io(); /* usually Schedulers.io() */
        this.streamObserver = createStreamObserver();
        this.timerObserver = createTimerObserver();

        initialize(config, persistScheduler, networkScheduler, streamObserver);
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
        flushable.onNext(Command.FLUSHED);
    }

    public void initialize(RakeUserConfig config,
                           Scheduler persistScheduler,
                           Scheduler networkScheduler,
                           Observer<List<JSONObject>> streamObserver) {

        /* important: variables below are used in all over the class, so always renew these variables */
        this.config = config;
        this.persistScheduler = persistScheduler;
        this.networkScheduler = networkScheduler;
        this.streamObserver = streamObserver;

        if (null != streamSubscription && !streamSubscription.isUnsubscribed())
            streamSubscription.unsubscribe();

        stream =
                /* composed of track, flush, timer stream */
                createLogStream()

                /* enable networking */
                .observeOn(networkScheduler) /* network operation in another IO thread */
                .filter(nullOrJsonList -> null != nullOrJsonList) /* must filter null */
                .map(tracked -> {
                    debugLogger.i("Networking Thread: " + Thread.currentThread().getName());
                    debugLogger.i("Sent log count: " + tracked.size());
                    return client.send(tracked); /* return tracked if failed, otherwise return null */
                })

                /* retry */
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

        streamSubscription = stream.subscribe(streamObserver);
    }

    private Observable<Command> createTrackStream() {
        return trackable
                .observeOn(persistScheduler)
                .map(jsonList -> {
                    if (null == jsonList || 0 == jsonList.size()) return null;
                    debugLogger.i("Persisting Scheduler: " + Thread.currentThread().getName());

                    /* if jsonList is 0 or size() == 0, then dao return -1 */
                    int totalCount = dao.add(jsonList);

                    /* if development or dao is full, flush immediately */
                    if (config.isDevelopment() || config.getMaxLogTrackCount() <= totalCount) { /* dao is full */
                        return Command.TRACK_FULL;
                    } else {
                        return Command.TRACK_NOT_FULL;
                    }
                });
    }

    private Observable<Command> createFlushStream() {
        setFlushInterval(config.getFlushIntervalAsMilliseconds()); /* enable timer */
        return flushable
                .filter(c -> !(config.isDevelopment()))
                .observeOn(persistScheduler);
    }

    private Observable<List<JSONObject>> createLogStream() {

        Observable<Command> trackStream = createTrackStream();
        Observable<Command> flushStream = createFlushStream();

        return trackStream.mergeWith(flushStream)
                .map(command -> {
                    // handling the upstream commands
                    // if necessary, then send tracked N log to downstream
                    if (Command.FLUSHED == command
                            || Command.TRACK_FULL == command
                            || Command.TIMER_FIRED == command) {

                        debugLogger.i("Extracting Scheduler: " + Thread.currentThread().getName());
                        return dao.getAndRemoveOldest(config.getMaxLogTrackCount());
                    }

                    // null will be filtered by withNetworking
                    return null;
                });
    }

    private void resubscribeCore() {
        if (null != streamSubscription && !streamSubscription.isUnsubscribed())
            streamSubscription.unsubscribe();

        streamSubscription = stream.subscribe(this.streamObserver);
    }

    public void setFlushInterval(long milliseconds) {
        if (config.isDevelopment()) {
            debugLogger.i("setFlushInterval is not supported in `RUNNING_ENV.DEV`.");
            return;
        }

        if (null != timerSubscription && ! timerSubscription.isUnsubscribed()) {
            stop.onNext(null); /* stop command */
            timerSubscription.unsubscribe();
        }

        // TODO subscribeOn ComputationThread
        timerSubscription = Observable
                .interval(milliseconds, TimeUnit.MILLISECONDS)
                .startWith(-1L) /* flush when app starts */
                .takeUntil(stop)
                .map(x -> {
                    debugLogger.i("Timer fired");
                    flushable.onNext(Command.TIMER_FIRED);
                    return x;
                })
                .subscribe(this.timerObserver);
    }

    private Observer<Long> createTimerObserver() {
        return new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        debugLogger.i("timerObserver.onCompleted: Old timer will be perished.");
                    }

                    @Override
                    public void onError(Throwable e) {
                        debugLogger.e("timerObserver.onError", e);
                        setFlushInterval(config.getFlushIntervalAsMilliseconds());
                    }

                    @Override
                    public void onNext(Long l) { /* do nothing */ }
                };
    }

    private Observer<List<JSONObject>> createStreamObserver() {
        return new Observer<List<JSONObject>>() {
            @Override
            public void onCompleted() {
                debugLogger.i("RakeCore.onCompleted");
                // resubscribeCore(); TODO rebuild?
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
            }
        };
    }

    /* to support test */
    public Observable<List<JSONObject>> getLogStream() {
        return createLogStream();
    }

    public Observable<Command> getFlushStream() {
        return createFlushStream();
    }

    public Observable<Command> getTrackStream() {
        return createTrackStream();
    }
}
