package com.skp.di.rake.client.api.impl;

import android.util.Log;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.protocol.RakeProtocol;
import com.skp.di.rake.client.utils.Logger;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class RakeCore {
    private RakeDao dao;
    private RakeHttpClient client;

    private PublishSubject<Integer>    flushable;
    private PublishSubject<JSONObject> trackable;

    private Observable<Integer> timer;

    private Observable<String> worker;
    private Subscription subscription;

    public RakeCore(RakeDao dao, RakeHttpClient client, RakeUserConfig config) {
        this.dao    = dao;
        this.client = client;

        this.timer = Observable
                .interval(config.getFlushInterval() ,TimeUnit.SECONDS)
                .startWith(-1L)
                .map(x -> null);

        this.flushable = PublishSubject.create();
        this.trackable = PublishSubject.create();

        // if mode is dev, then worker has no timer, and are not flushable

        if (RakeUserConfig.Mode.DEV == config.getRunningMode())
            this.worker = buildDevWorker(config);
        else /* Mode.LIVE */
            this.worker = buildLiveWorker(config);


        // TODO: subscribe in Live
        this.subscription = subscribe(this.subscription, this.worker, null,
                new Observer<String>() {
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

    private Observable<String> buildDevWorker(RakeUserConfig config) {
        return  trackable
                .observeOn(Schedulers.io())
                .map(json -> {
                    Logger.i("Thread: " + Thread.currentThread().getName());
                    return client.send(Arrays.asList(json));
                })
                .onErrorReturn(t -> {
                    // TODO: onErrorReturn
                    Logger.e("exception occurred in RakeCore", t);
                    return null;
                });
    }

    private Observable<String> buildLiveWorker(RakeUserConfig config) {
        return trackable
                .observeOn(Schedulers.io())
                .map(json -> {
                    dao.add(json);
                    return dao.getCount();
                }).filter(count -> count == config.getMaxLogTrackCount())
                .mergeWith(timer.mergeWith(flushable))
                .map(flushCommanded -> {
                    List<JSONObject> tracked = dao.clear();
                    return client.send(tracked); /* return response. it might be null */
                }).filter(responseBody -> null != responseBody)
                .onErrorReturn(t -> {
                    // TODO: onErrorReturn
                    Logger.e("exception occurred in RakeCore", t);
                    return null;
                });
    }

    private Subscription subscribe(Subscription subscription, Observable<String> worker,
                                   Scheduler scheduler, Observer<String> observer) {
        if (null != subscription) subscription.unsubscribe();

        return worker
                .subscribeOn((null == scheduler) ? Schedulers.io() : scheduler)
                .subscribe(observer);
    }

    public void track(JSONObject json) {
        if (null != json) trackable.onNext(json);
    }

    public void flush() {
        flushable.onNext(null);
    }

    /* the fields, methods below are used only for testing */
    public void subscribeOnTest(Observer<String> observer) {
        subscribe(this.subscription, this.worker,
                AndroidSchedulers.mainThread(), observer);
    }

    public int getLogCount() { return dao.getCount(); }
}
