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
import rx.observables.ConnectableObservable;
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
    private PublishSubject<List<JSONObject>> flushable;
    private PublishSubject<List<JSONObject>> stop; /* to stop `timer` */

    private Observable<List<JSONObject>> stream;
    private Observable<List<JSONObject>> logStream;

    private Subscription timerSubscription;
    private Subscription streamSubscription;
    private Observer<List<JSONObject>> streamObserver;
    private Observer<Long> timerObserver;

    private Scheduler persistScheduler;
    private Scheduler networkScheduler;

    private RakeLogger logger;

    private Long RETRY_BUFFER_TIME = 10000L; /* milliseconds */
    private int RETRY_BUFFER_COUNT = 10; /* buffer at most 10 elements */

    /* constructor */
    public RakeCore(RakeDao dao,
                    Scheduler persistScheduler,
                    RakeHttpClient client,
                    Scheduler networkScheduler,
                    RakeUserConfig config) {

        this.dao    = dao;
        this.client = client;
        this.logger = RakeLoggerFactory.getLogger(this.getClass(), config);

        this.flushable = PublishSubject.create();
        this.trackable = PublishSubject.create();
        this.stop = PublishSubject.create();
        this.timerSubscription = null;

        this.config = config;
        this.persistScheduler = persistScheduler; /* usually Schedulers.computation() */
        this.networkScheduler = networkScheduler; /* usually Schedulers.io() */
        this.streamObserver = createStreamObserver();
        this.timerObserver = createTimerObserver();

        initialize();
    }

    public RakeCore(RakeDao dao,
                    RakeHttpClient client,
                    RakeUserConfig config) {

        this(dao, Schedulers.computation(), client, Schedulers.io(), config);
    }

    public void track(JSONObject json) {
        if (null == json) return;

        logger.i("track called: \n" + json.toString());
        track(Arrays.asList(json));
    }

    private void track(List<JSONObject> jsons) {
        if (null == jsons || 0 == jsons.size()) return;

        trackable.onNext(jsons);
    }

    public void flush() {
        flushable.onNext(null);
    }

    private void initialize() {

        if (null != streamSubscription && !streamSubscription.isUnsubscribed())
            streamSubscription.unsubscribe();

        this.logStream = createLogStream();

        stream =
                logStream /* composed of track, flush, timer stream */

                /* with network operations */
                .observeOn(networkScheduler) /* network operation in another IO thread */
                .map(tracked -> {
                    logger.printCurrentThreadWith("Networking");
                    logger.i("Sent log count: " + tracked.size());

                    /* return tracked if failed, otherwise return null */
                    List<JSONObject> failed = client.send(tracked);

                    if (null != failed && failed.size() > 0) {
                        logger.i("Failed. retrying log count: " + failed.size());
                    }

                    return failed;
                })
                        
                /* with retry, buffer */
                .filter(retry -> null != retry)
                .buffer(RETRY_BUFFER_TIME, TimeUnit.MILLISECONDS, RETRY_BUFFER_COUNT)
                .flatMap(buffers -> Observable.from(buffers))
                .map(failed -> {
                    /* iff failed */
                    trackable.onNext(failed);

                    // TODO metric
                    // TODO returning meaningful things
                    return null;
                });

        streamSubscription = stream.subscribe(streamObserver);
    }

    private Observable<List<JSONObject>> createLogStream() {

        /* since logStream composed of two subjects, we need to make it a connectable observable.
           if not, downstream observers will get more observations than it expected
         */

        ConnectableObservable<List<JSONObject>> logStream =
            trackable
                .mergeWith(createFlushStream())
                .observeOn(persistScheduler) /* access dao only in the persisting thread */
                .map(nullOrJsonList -> {
                    /* if null, flush or timer fired */

                    int maxTrackCount = config.getMaxLogTrackCount();
                    int totalCount = -1;

                    /* if jsonList is 0 or size() == 0, then dao return -1 */
                    if (null != nullOrJsonList) {
                        logger.printCurrentThreadWith("Persisting");
                        totalCount = dao.add(nullOrJsonList);
                        logger.i("Total log count: " + totalCount);
                    }

                    if (config.isDevelopment()              /* if development, flush immediately */
                            || maxTrackCount <= totalCount  /* dao is full */
                            || null == nullOrJsonList) {    /* flush, timer commanded */

                        logger.printCurrentThreadWith("Extracting");
                        return dao.getAndRemoveOldest(maxTrackCount);

                    } else return null; /* otherwise skip */

                })
                .filter(tracked -> null != tracked)
            .publish();

        logStream.connect();

        return logStream;
    }

    private Observable<List<JSONObject>> createFlushStream() {
        setFlushInterval(config.getFlushIntervalAsMilliseconds()); /* enable timer */

        List<JSONObject> initialFlushCommand = null;

        return flushable
                .startWith(initialFlushCommand)
                .filter(c -> config.isLive());
    }

    private void resubscribeStream(Observer<List<JSONObject>> o) {
        if (null != streamSubscription && !streamSubscription.isUnsubscribed())
            streamSubscription.unsubscribe();

        this.streamObserver = o;
        streamSubscription = stream.subscribe(o);
    }

    public void setFlushInterval(long milliseconds) {
        if (config.isDevelopment()) {
            logger.i("setFlushInterval is not supported in `RUNNING_ENV.DEV`.");
            return;
        }

        logger.i("set flush interval: " + milliseconds);

        if (null != timerSubscription && ! timerSubscription.isUnsubscribed()) {
            stop.onNext(null); /* stop command */
            timerSubscription.unsubscribe();
        }

        // TODO subscribeOn ComputationThread
        timerSubscription = Observable
                .interval(milliseconds, TimeUnit.MILLISECONDS)
                .takeUntil(stop)
                .map(x -> {
                    logger.i("Timer fired");
                    flushable.onNext(null);
                    return x;
                })
                .subscribe(this.timerObserver);
    }

    private Observer<Long> createTimerObserver() {
        return new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        logger.i("timerObserver.onCompleted: Old timer will be perished.");
                    }

                    @Override
                    public void onError(Throwable e) {
                        logger.e("timerObserver.onError", e);
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
                logger.i("RakeCore.onCompleted");
                // resubscribeStream(); TODO rebuild?
            }

            @Override
            public void onError(Throwable t) {
                logger.e("RakeCore.onError", t);
                resubscribeStream(streamObserver);
            }

            @Override
            public void onNext(List<JSONObject> metric) {
                if (null == metric) return;
                logger.printCurrentThreadWith("StreamObserver.onNext");
            }
        };
    }

    /* to support test */
    public Observable<List<JSONObject>> getLogStream() {
        return logStream;
    }

    public Observable<List<JSONObject>> getFlushStream() {
        return createFlushStream();
    }
}
