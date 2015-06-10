package com.skp.di.rake.client.api.impl;

import com.skp.di.rake.client.api.RakeUserConfig;
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
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class RakeCore {
    private RakeDao dao;
    private RakeHttpClient client;

    private PublishSubject<Integer>    flushable;
    private PublishSubject<JSONObject> trackable;

    private Observable<String> worker;
    private Subscription subscription;

    public RakeCore(RakeDao dao, RakeHttpClient client, RakeUserConfig config) {
        this.dao    = dao;
        this.client = client;

        Observable<Integer> timer = Observable
                .interval(config.getFlushInterval() ,TimeUnit.SECONDS)
                .startWith(-1L)
                .map(x -> null);

        this.flushable = PublishSubject.create();
        this.trackable = PublishSubject.create();

        // if mode is dev, then worker has no timer, and are not flushable
        this.worker = buildWorker(timer, this.flushable, this.trackable, config);

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

    private Observable<String> buildWorker(
            Observable<Integer> timer,
            PublishSubject<Integer> flushable,
            PublishSubject<JSONObject> trackable,
            RakeUserConfig config) {

        Observable<String> incompleteWorker;

        if (RakeUserConfig.Mode.DEV == config.getRunningMode()) {

            incompleteWorker = trackable
                    .map(json -> client.send(Arrays.asList(json)));

        } else { /* Mode.Live */

             incompleteWorker = trackable
                .map(json -> {
                    dao.add(json);
                    return dao.getCount();
                }).filter(count -> count == config.getMaxLogTrackCount())
                .mergeWith(timer.mergeWith(flushable))
                .map(flushCommanded -> {
                    List<JSONObject> tracked = dao.clear();
                    return client.send(tracked); /* return response. it might be null */
                }).filter(responseBody -> null != responseBody);
        }

        return incompleteWorker.onErrorReturn(t -> {
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
