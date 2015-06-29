package com.skp.di.rake.client.network;

import com.skp.di.rake.client.api.RakeUserConfig;
import com.skp.di.rake.client.protocol.RakeProtocol;
import com.skp.di.rake.client.protocol.RakeProtocolV2;
import com.skp.di.rake.client.protocol.exception.RakeException;
import com.skp.di.rake.client.protocol.exception.RakeProtocolBrokenException;
import com.skp.di.rake.client.utils.RakeLogger;
import com.skp.di.rake.client.utils.RakeLoggerFactory;
import com.skp.di.rake.client.utils.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;

/**
 * Not thread-safe.
 * Instances of this class should only be used by a single thread.
 */
public class RakeHttpClient {

    private RakeUserConfig config;
    public enum ContentType { JSON, URL_ENCODED_FORM }
    private String endPoint;
    private String LIVE_MODE_ENDPOINT = "https://rake.skplanet.com:8443/log/track";
    private String DEV_MODE_ENDPOINT  = "https://pg.rake.skplanet.com:8443/log/track";
    static public final int DEFAULT_CONNECTION_TIMEOUT = 3000;
    static public final int DEFAULT_SOCKET_TIMEOUT = 120000;
    private int connectionTimeout;
    private int socketTimeout;
    private RakeLogger logger;
    private RakeProtocol rakeProtocol;

    public void setEndPoint(String endPoint) { this.endPoint = endPoint; }
    /* to support legacy api `setRakeServer` */
    public void setEndPointLegacy(String incompleteEndPoint) { setEndPoint(incompleteEndPoint + "/track"); }
    public String getEndPoint() { return this.endPoint; }

    public void setConnectionTimeout(int milliseconds) { this.connectionTimeout = milliseconds; }
    public void setSocketTImeout(int milliseconds) { this.socketTimeout = milliseconds; }
    public int getConnectionTimeout() { return this.connectionTimeout; }
    public int getSocketTimeout() { return this.socketTimeout; }


    public RakeHttpClient(RakeUserConfig config, RakeProtocol protocol) {
        this.config = config;
        this.rakeProtocol = protocol;
        this.logger = RakeLoggerFactory.getLogger(this.getClass(), config);

        if (RakeUserConfig.RUNNING_ENV.DEV == config.getRunningMode())
            endPoint = DEV_MODE_ENDPOINT;
        else if (RakeUserConfig.RUNNING_ENV.LIVE == config.getRunningMode())
            endPoint = LIVE_MODE_ENDPOINT;
        else
            throw new RakeProtocolBrokenException("Can't set endpoint due to invalid runningMode from RakeUserConfig");

        this.connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        this.socketTimeout = DEFAULT_SOCKET_TIMEOUT;
    }

    public List<JSONObject> send(List<JSONObject> tracked) {

        if (null == tracked || 0 == tracked.size()) return null;

        boolean retry = false;

        try {
            HttpResponse res = executePost(tracked);

            int statusCode = res.getStatusLine().getStatusCode();
            String responseBody = convertHttpResponseToString(res);

            logger.i("Response from server: \n" + responseBody);

            rakeProtocol.verifyResponse(statusCode, responseBody);

        } catch(UnsupportedEncodingException e) {
            logger.e("Cant' build StringEntity using body", e);
        } catch(JSONException e) {
            logger.e("Can't build RakeRequestBody", e);
        } catch(ClientProtocolException e) {
            logger.e("Invalid Network Protocol", e);
        } catch (SocketTimeoutException e) {
            logger.e("Socket timeout occurred", e);
            retry = true;
        } catch (ConnectTimeoutException e) {
            logger.e("Connection timeout occurred", e);
            retry = true;
        } catch(UnknownHostException e) {
            logger.e("No connected network", e);
            retry = true;
        } catch (IOException e) {
            logger.e("Can't send message to server", e);
            retry = true;
        } catch (RakeException e) {
            logger.e("RakeException occurred, might be due to 500", e);
            retry = true;
        } catch(GeneralSecurityException e) {
            logger.e("Can't build HttpsClient", e);
        } catch(OutOfMemoryError e) {
            logger.e("Not enough memory", e);
            retry = true;
        } catch (Exception e) {
            logger.e("Uncaught exception occurred", e);
        }

        /* returning null means, there is no need to retry */
        return (true == retry) ? tracked : null;
    }

    protected HttpResponse executePost(List<JSONObject> tracked)
            throws IOException, JSONException, GeneralSecurityException {

        HttpClient client = null;

        if (endPoint.startsWith("https"))     client = createHttpsClient();
        else if (endPoint.startsWith("http")) client = createHttpClient();
        else throw new RakeProtocolBrokenException("Unsupported endpoint protocol");

        HttpPost post = rakeProtocol.buildRequest(tracked, endPoint);
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
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        HttpConnectionParams.setSoTimeout(params, socketTimeout);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

        return params;
    }
}
