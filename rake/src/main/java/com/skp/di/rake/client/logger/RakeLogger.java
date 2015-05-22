package com.skp.di.rake.client.logger;

import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.persistent.RakeDao;
import com.skp.di.rake.client.protocol.exception.InsufficientJsonFieldException;
import com.skp.di.rake.client.protocol.RakeProtocol;
import com.skp.di.rake.client.protocol.exception.InternalServerErrorException;
import com.skp.di.rake.client.protocol.exception.InvalidEndPointException;
import com.skp.di.rake.client.protocol.exception.InvalidJsonSyntaxException;
import com.skp.di.rake.client.protocol.exception.NotRegisteredRakeTokenException;
import com.skp.di.rake.client.protocol.exception.RakeException;
import com.skp.di.rake.client.protocol.exception.RakeProtocolBrokenException;
import com.skp.di.rake.client.protocol.exception.WrongRakeTokenUsageException;
import com.skp.di.rake.client.utils.Logger;
import com.skp.di.rake.client.utils.StringUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

public class RakeLogger implements Rake {

    private RakeDao dao;
    private String endPoint = RakeMetaConfig.END_POINT;

    public RakeLogger(RakeDao dao) {
        this.dao = dao;
    }

    @Override
    public void track(JSONObject log) {
        if (null == log) return;
        if (log.toString().equals("{\"\":\"\"}")) return;

        if (RakeMetaConfig.MAX_TRACK_COUNT == dao.getCount())
            flush();

        dao.add(log);
    }

    @Override
    public int getCount() {
        return dao.getCount();
    }

    @Override
    public String flush() {
        List<JSONObject> tracked = dao.clear();

        /* createRequestBody returns json string */
        String body = createRequestBody(tracked);
        String responseMessage = null;

        if (null != body) responseMessage = send(body);

        /* returning null means that sent nothing */
        return responseMessage;
    }

    protected String send(String body) {
        String responseBody = null;

        try {
            HttpResponse res = executePost(body);
            responseBody = convertHttpResponseToString(res);

            int statusCode = res.getStatusLine().getStatusCode();
            handleRakeException(statusCode, responseBody);

        } catch (IOException e) {
            Logger.e("Can't send message to server", e);
        } catch (RakeException e) {
            throw e; /* to support test */
        } catch (Exception e) {
            Logger.e("Uncaught exception occurred", e);
        }

        return responseBody;
    }

    protected void handleRakeException(int statusCode, String responseBody) {
        try {
            verifyResponse(statusCode, responseBody);
        } catch (RakeProtocolBrokenException e) {
            Logger.e(e);
        } catch (InsufficientJsonFieldException e) {
            Logger.e(e);
        } catch (InvalidJsonSyntaxException e) {
            Logger.e(e);
        } catch (NotRegisteredRakeTokenException e) {
            Logger.e(e);
        } catch (WrongRakeTokenUsageException e) {
            Logger.e(e);
        } catch (InvalidEndPointException e) {
            Logger.e(e);
        } catch (InternalServerErrorException e) {
            Logger.e(e);
        }
    }

    protected void verifyResponse(int statusCode, String responseBody) {
        verifyStatus(statusCode);
        verifyErrorCode(responseBody);
    }

    private String verifyErrorCode(String responseBody) throws
            InsufficientJsonFieldException,
            InvalidJsonSyntaxException,
            NotRegisteredRakeTokenException,
            WrongRakeTokenUsageException {

        JSONObject response = null;
        int errorCode = 0;

        try {
            response = new JSONObject(responseBody);
            errorCode = response.getInt("errorCode");
        } catch (JSONException e) {
            throw new RakeProtocolBrokenException(e);
        }

        switch(errorCode) {
            case RakeProtocol.ERROR_CODE_OK: /* pass through */
                break;
            case RakeProtocol.ERROR_CODE_INSUFFICIENT_JSON_FIELD:
                throw new InsufficientJsonFieldException(responseBody);
            case RakeProtocol.ERROR_CODE_INVALID_JSON_SYNTAX:
                throw new InvalidJsonSyntaxException(responseBody);
            case RakeProtocol.ERROR_CODE_NOT_REGISTERED_RAKE_TOKEN:
                throw new NotRegisteredRakeTokenException(responseBody);
            case RakeProtocol.ERROR_CODE_WRONG_RAKE_TOKEN_USAGE:
                throw new WrongRakeTokenUsageException(responseBody);

            default: throw new RakeProtocolBrokenException("");
        }

        return responseBody;
    }

    private void verifyStatus(int statusCode) throws
            RakeProtocolBrokenException,
            InvalidEndPointException,
            InternalServerErrorException {

        switch(statusCode) {
            case HttpStatus.SC_NOT_FOUND:
                throw new InvalidEndPointException("");
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                throw new InternalServerErrorException("");
            default: break; /* pass through */
        }
    }

    protected HttpResponse executePost(String body) throws IOException {
        HttpClient client = createHttpClient();
        HttpPost   post   = createHttpPost(body);

        HttpResponse response = null;

        /* send post message to server */
        response = client.execute(post);

        return response;
    }

    private String convertHttpResponseToString(HttpResponse hr) throws IOException {
        InputStream is = null;
        String responseMessage = null;

        /* convert HTTP response to String */
        try {
            is = hr.getEntity().getContent();
            responseMessage = StringUtils.toString(is);
        } catch(IOException e) {
            throw e;
        } finally {
            StringUtils.closeQuietly(is);
        }

        return responseMessage;
    }

    private String createRequestBody(List<JSONObject> tracked) {
        if (0 == tracked.size()) return null;

        Iterator<JSONObject> i = tracked.iterator();
        JSONArray dataField = new JSONArray();

        while(i.hasNext()) {
            JSONObject log = i.next();
            dataField.put(log);
        }

        JSONObject flushed = new JSONObject();

        try {
            flushed.put("data", dataField);
        } catch (JSONException e) {
            Logger.e("Can't create request body", e);
        }

        String body = flushed.toString();

        return body;
    }

    private HttpClient createHttpClient() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, RakeMetaConfig.CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, RakeMetaConfig.SOCKET_TIMEOUT);
        HttpClient client = new DefaultHttpClient(params);

        return client;
    }

    private HttpPost createHttpPost(String body) {
        HttpPost post = new HttpPost(endPoint);
        StringEntity se = null;

        try {
            se = new StringEntity(body);
        } catch (UnsupportedEncodingException e) {
            Logger.e("Can't build StringEntity", e);
        }

        post.setEntity(se);
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Accept", "application/json");

        return post;
    }

    @Override
    public void clear() {
        dao.clear();
    }
}
