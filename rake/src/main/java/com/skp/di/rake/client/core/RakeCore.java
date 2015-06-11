package com.skp.di.rake.client.core;

import com.skp.di.rake.client.api.Rake;
import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.network.RakeHttpClient;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.utils.Logger;

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

    public RakeCore(RakeDao dao, RakeHttpClient client, RakeUserConfig config) {
        this.dao    = dao;
        this.client = client;
        this.config = config;

        this.timer = Observable
                .interval(config.getFlushInterval() ,TimeUnit.SECONDS)
                .startWith(-1L)
                .map(x -> {
                    if (config.getWillPrintDebugInfo()) Logger.i("Timer Fired");
                    return null;
                });

        this.flushable = PublishSubject.create();
        this.trackable = PublishSubject.create();

        // TODO: subscribe in Live
        this.subscription = subscribe(Schedulers.io(),
                new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        if (config.getWillPrintDebugInfo()) Logger.i("RakeCore onCompleted");
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (config.getWillPrintDebugInfo()) Logger.e("RakeCore.onError", t);
                    }

                    @Override
                    public void onNext(String response) {
                        if (config.getWillPrintDebugInfo()) Logger.i("Server Response: \n" + response);
                    }
                });
    }

    private Observable<String> buildDevWorker(Scheduler scheduler) {
        return  trackable
                .observeOn(scheduler)
                .map(json -> {
                    if (config.getWillPrintDebugInfo())
                        Logger.i("Network Thread: " + Thread.currentThread().getName());
                    return client.send(Arrays.asList(json));
                });
    }

    private Observable<String> buildLiveWorker(Scheduler scheduler) {
        return trackable
                .observeOn(scheduler)
                .map(json -> {
                    dao.add(json);
                    if (config.getWillPrintDebugInfo()
                            && dao.getCount() == config.getMaxLogTrackCount())
                        Logger.i("Rake is full. Auto-flushed");

                    return dao.getCount();
                }).filter(count -> count == config.getMaxLogTrackCount())
                .mergeWith(timer.mergeWith(flushable))
                .map(flushCommanded -> {
                    List<JSONObject> tracked = dao.clear();
                    return client.send(tracked); /* return response. it might be null */
                }).filter(responseBody -> null != responseBody);
    }

    public Subscription subscribe(Scheduler scheduler, Observer<String> observer) {
        if (null != subscription) subscription.unsubscribe();

        // if mode is dev, then worker has no timer, and are not flushable
        if (RakeUserConfig.RUNNING_ENV.DEV == config.getRunningMode())
            worker = buildDevWorker(scheduler);
        else /* RUNNING_ENV.LIVE */
            worker = buildLiveWorker(scheduler);

        return worker
                .onErrorReturn(t -> {
                    // TODO: onErrorReturn
                    Logger.e("exception occurred in RakeCore", t);
                    return null;
                })
                .subscribeOn(scheduler)
                .subscribe(observer);
    }

    public void track(JSONObject json) {
        if (null != json) trackable.onNext(json);
    }

    public void flush() {
        flushable.onNext(null);
    }

    public int getLogCount() { return dao.getCount(); }
}
