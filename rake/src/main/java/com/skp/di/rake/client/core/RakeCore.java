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

public class RakeCore {
    private RakeDao dao;
    private RakeHttpClient client;
    private RakeUserConfig config;

    private PublishSubject<Integer>    flushable;
    private PublishSubject<JSONObject> trackable;
    private Observable<Integer> timer;
    private Observable<String> worker;
    private Subscription subscription;

    private RakeLogger debugLogger;

    public RakeCore(RakeDao dao, RakeHttpClient client, RakeUserConfig config) {
        this.dao    = dao;
        this.client = client;
        this.config = config;
        this.debugLogger = RakeLoggerFactory.getLogger(this.getClass(), config);

        this.timer = Observable
                .interval(config.getFlushInterval() ,TimeUnit.SECONDS)
                .startWith(-1L) /* flush when app starts */
                .map(x -> {
                    debugLogger.i("Timer fired");
                    return null;
                });

        this.flushable = PublishSubject.create();
        this.trackable = PublishSubject.create();

        // TODO: subscribe in Live
        this.subscription = subscribe(Schedulers.io(),
                new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        debugLogger.i("RakeCore onCompleted");
                    }

                    @Override
                    public void onError(Throwable t) {
                        debugLogger.e("RakeCore.onError", t);
                    }

                    @Override
                    public void onNext(String response) {
                        if (null == response) return;
                        debugLogger.i("Observer Thread: " + Thread.currentThread().getName());
                        debugLogger.i("Server Responses: " + response);
                    }
                });
    }

    private Observable<String> buildWorker(Scheduler scheduler) {
        return trackable
                .mergeWith(timer.mergeWith(flushable).map(fired -> { return null; }))
                .observeOn(scheduler) /* dao access in IO thread */
                .map(nullOrJson-> {
                    /* if null == nullOrJson, timer was fired or flush was commanded */
                    debugLogger.i("Persisting Thread: " + Thread.currentThread().getName());

                    /* if -1, null */
                    int totalCount = dao.add(nullOrJson);

                    if (-1 == totalCount /* timer was fired or, flush was commanded */
                     || totalCount >= config.getMaxLogTrackCount() /* persistence is full */
                     || RakeUserConfig.RUNNING_ENV.DEV == config.getRunningMode()) /* dev mode */ {
                        return dao.getAndRemoveOldest(config.getMaxLogTrackCount());
                    }

                    /* track is called, but persistence is not full  */
                    return null;
                }).filter(nullOrJsonList -> null != nullOrJsonList)
                .observeOn(scheduler) /* network operation in another IO thread */
                .map(tracked -> {
                    debugLogger.i("Networking Thread: " + Thread.currentThread().getName());
                    debugLogger.i("sent log count: " + tracked.size());
                    return client.send(tracked); /* return response. it might be null */
                });
    }

    public Subscription subscribe(Scheduler scheduler, Observer<String> observer) {
        if (null != subscription) subscription.unsubscribe();

        worker = buildWorker(scheduler);

        return worker
                .onErrorReturn(t -> {
                    // TODO: onErrorReturn
                    RakeLogger.e("exception occurred. onErrorReturn", t);
                    return null;
                })
                .subscribe(observer);
    }

    public void track(JSONObject json) {
        if (null == json) return;

        debugLogger.i("track called: \n" + json.toString());
        trackable.onNext(json);

    }

    public void flush() {
        debugLogger.i("flush called");
        flushable.onNext(null);
    }
}
