package com.skp.di.rake.client.network;

import com.skp.di.rake.client.api.Rake;
import com.skp.di.rake.client.config.RakeMetaConfig;
import com.skp.di.rake.client.protocol.RakeProtocol;
import com.skp.di.rake.client.protocol.exception.InsufficientJsonFieldException;
import com.skp.di.rake.client.protocol.exception.InternalServerErrorException;
import com.skp.di.rake.client.protocol.exception.InvalidEndPointException;
import com.skp.di.rake.client.protocol.exception.InvalidJsonSyntaxException;
import com.skp.di.rake.client.protocol.exception.NotRegisteredRakeTokenException;
import com.skp.di.rake.client.protocol.exception.RakeException;
import com.skp.di.rake.client.protocol.exception.RakeProtocolBrokenException;
import com.skp.di.rake.client.protocol.exception.WrongRakeTokenUsageException;
import com.skp.di.rake.client.utils.Logger;
import com.skp.di.rake.client.utils.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.List;

public class RakeHttpClient {

    private RakeNetworkConfig config;

    public RakeHttpClient(RakeNetworkConfig config) {
        this.config = config;
    }

    public String send(List<JSONObject> tracked) {

        if (null == tracked || 0 == tracked.size()) return null;

        String responseBody = null;

        try {
            HttpResponse res = executePost(tracked);
            responseBody = convertHttpResponseToString(res);

            int statusCode = res.getStatusLine().getStatusCode();
            handleRakeException(statusCode, responseBody);

        } catch(UnsupportedEncodingException e) {
            Logger.e("Cant' build StringEntity using body", e);
        } catch(JSONException e) {
            Logger.e("Can't build RakeRequestBody", e);
        } catch(ClientProtocolException e) {
            Logger.e("Can't send message to server", e);
        } catch (IOException e) {
            Logger.e("Can't send message to server", e);
        } catch (RakeException e) {
            throw e; /* to support test */
        } catch(GeneralSecurityException e) {
            Logger.e("Can't build HttpsClient", e);
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

            default: throw new RakeProtocolBrokenException(responseBody);
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

    protected HttpResponse executePost(List<JSONObject> tracked)
            throws IOException, JSONException, GeneralSecurityException {

        HttpClient   client = null;

        if (config.getEndPoint().startsWith("https"))     client = createHttpsClient();
        else if (config.getEndPoint().startsWith("http")) client = createHttpClient();
        else throw new RakeProtocolBrokenException("Unsupported endpoint protocol");

        HttpEntity entity = null;

        if      (config.getContentType() == RakeNetworkConfig.ContentType.JSON)
            entity = RakeProtocol.buildJsonEntity(tracked);
        else if (config.getContentType() == RakeNetworkConfig.ContentType.URL_ENCODED_FORM)
            entity = RakeProtocol.buildUrlEncodedEntity(tracked);
        else throw new RakeProtocolBrokenException("Unsupported contentType");

        HttpPost     post   = createHttpPost(entity);
        HttpResponse response = client.execute(post);

        return response;
    }

    private String convertHttpResponseToString(HttpResponse hr) throws IOException {
        InputStream is = null;
        String responseMessage = null;

        /* convert HTTP response to String */
        try {
            is = hr.getEntity().getContent();
            responseMessage = StringUtils.toString(is);
        } catch(IOException e) { throw e;
        } finally { StringUtils.closeQuietly(is); }

        return responseMessage;
    }

    private HttpClient createHttpClient() {
        HttpParams params = createHttpParams();
        return new DefaultHttpClient(params);
    }


    private HttpClient createHttpsClient() throws GeneralSecurityException, IOException {
        HttpParams params = createHttpParams();

        KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
        store.load(null, null);

        SSLSocketFactory sslSocketFactory = new RakeSSLSocketFactory(store);
        sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);


        Scheme http = new Scheme("http", PlainSocketFactory.getSocketFactory(), 80);
        Scheme https = new Scheme("https", sslSocketFactory, 80);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(http);
        registry.register(https);

        ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(params, registry);

        return new DefaultHttpClient(connectionManager, params);
    }

    private HttpParams createHttpParams() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, config.getConnectionTimeout());
        HttpConnectionParams.setSoTimeout(params, config.getSocketTimeout());
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

        return params;
    }

    private HttpPost createHttpPost(HttpEntity entity) {
        HttpPost post = new HttpPost(config.getEndPoint());
        post.setEntity(entity);

        if (config.getContentType() == RakeNetworkConfig.ContentType.JSON) {
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");
        }

        return post;
    }
}
