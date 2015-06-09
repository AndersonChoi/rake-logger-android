package com.skp.di.rake.client.utils;

import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.mock.SampleDevConfig;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import rx.exceptions.OnErrorThrowable;

public class TestUtils {
    static private RakeMetaConfig config = new RakeMetaConfig(new SampleDevConfig());

    static public HttpClient createHttpClient() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, config.getHttpConnectionTimeout());
        HttpConnectionParams.setSoTimeout(params, config.getHttpSocketTimeout());
        HttpClient client = new DefaultHttpClient(params);

        return client;
    }

    static public HttpPost createHttpPost(StringEntity se) {
        HttpPost post = new HttpPost(config.getEndpoint());
        post.setEntity(se);
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Accept", "application/json");

        return post;
    }

    static public StringEntity createEntity(String body) throws UnsupportedEncodingException {
        return new StringEntity(body);
    }

    static public <E> List<E> toList(Iterable<E> iter) {
        ArrayList<E> list = new ArrayList<E>();

        for(E item : iter) { list.add(item); }

        return list;
    }

    static public HttpResponse sendHttpPost(String body) {
        HttpClient client = null;
        HttpPost     post = null;
        HttpResponse res  = null;

        try {
            client = createHttpClient();
            StringEntity se = createEntity("{}");
            post = createHttpPost(se);
            res = client.execute(post);
        } catch (Exception e) { throw OnErrorThrowable.from(e); }

        return res;
    }
}
