package com.skp.di.rake.client.api.impl;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.protocol.RakeProtocol;
import com.skp.di.rake.client.utils.Logger;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class RakeCore {
    static private RakeCore instance;

    private RakeDao dao;
    private RakeHttpClient client;

    private Observable<Integer>        timer;
    private PublishSubject<Integer>    flushable;
    private PublishSubject<JSONObject> trackable;
    private Observable<String> returned;

    private Subscription returnedSubscription;

    private RakeCore(RakeDao dao, RakeHttpClient client, RakeUserConfig config) {
        this.dao    = dao;
        this.client = client;

        this.timer = Observable
                .interval(config.getFlushInterval() ,TimeUnit.SECONDS)
                .startWith(-1L)
                .map(x -> null);

        this.flushable = PublishSubject.create();
        this.trackable = PublishSubject.create();

        /* TODO: if dev, no timer, flush immediately */
        this.returned =
                trackable.map(json -> {
                    dao.add(json);
                    return dao.getCount();
                }).filter(count -> count == config.getMaxLogTrackCount())
                .mergeWith(timer.mergeWith(flushable))
                .map(flushCommanded -> {
                    if (0 == dao.getCount()) return null;

                    List<JSONObject> tracked = dao.clear();
                    String requestBody = RakeProtocol.buildRakeRequestBody(tracked);
                    return client.send(requestBody); /* return response */
                }).filter(responseBody -> null != responseBody)
                .onErrorReturn(t -> {
                    // TODO: onErrorReturn
                    Logger.e("Unhandled exception in RakeCore", t);
                    return null;
                });


        // TODO: subscribe in Live
        subscribe(null, new Observer<String>() {
            @Override
            public void onCompleted() {
                Logger.i("RakeCore onCompleted");
            }

            @Override
            public void onError(Throwable t) {
                Logger.e("RakeCore.onError", t);
            }

            @Override
            public void onNext(String response) {
                Logger.i(response);
            }
        });
    }

    private void subscribe(Scheduler scheduler, Observer<String> observer) {
        if (null != returnedSubscription) returnedSubscription.unsubscribe();

        returnedSubscription = returned
                .subscribeOn((null == scheduler) ? Schedulers.io() : scheduler)
                .subscribe(observer);
    }

    static public RakeCore getInstance(RakeDao dao, RakeHttpClient client, RakeUserConfig config) {
        if (null == instance) instance = new RakeCore(dao, client, config);

        return instance;
    }

    public void track(JSONObject json) {
        trackable.onNext(json);
    }

    public void flush() {
        flushable.onNext(null);
    }

    /* the fields, methods below are used only for testing */
    public void subscribeOnTest(Observer<String> observer) {
        subscribe(AndroidSchedulers.mainThread(), observer);
    }

    public int getLogCount() { return dao.getCount(); }
    public int clearLog() { return dao.clear().size(); }
}
