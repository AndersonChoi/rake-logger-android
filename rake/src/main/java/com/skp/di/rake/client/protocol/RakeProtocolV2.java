package com.skp.di.rake.client.protocol;

import com.skp.di.rake.client.protocol.exception.InsufficientJsonFieldException;
import com.skp.di.rake.client.protocol.exception.InternalServerErrorException;
import com.skp.di.rake.client.protocol.exception.InvalidEndPointException;
import com.skp.di.rake.client.protocol.exception.InvalidJsonSyntaxException;
import com.skp.di.rake.client.protocol.exception.NotRegisteredRakeTokenException;
import com.skp.di.rake.client.protocol.exception.RakeProtocolBrokenException;
import com.skp.di.rake.client.protocol.exception.WrongRakeTokenUsageException;
import com.skp.di.rake.client.utils.RakeLogger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class RakeProtocolV2 implements RakeProtocol {
    /* Ref: http://wiki.skplanet.com/display/DIT/Rake+API+Spec */
    static public final int ERROR_CODE_OK                        = 20000;
    static public final int ERROR_CODE_INSUFFICIENT_JSON_FIELD   = 40001;
    static public final int ERROR_CODE_INVALID_JSON_SYNTAX       = 40002;
    static public final int ERROR_CODE_NOT_REGISTERED_RAKE_TOKEN = 40101;
    static public final int ERROR_CODE_WRONG_RAKE_TOKEN_USAGE    = 40301;
    static public final int ERROR_CODE_INVALID_END_POINT         = 40401;
    static public final int ERROR_CODE_INTERNAL_SERVER_ERROR     = 50001;

    static public final String FIELD_NAME_DATA = "data";
    static public final String FIELD_NAME_COMPRESS = "compress";
    static public final String FIELD_VALUE_COMPRESS = "plain";

    static public StringEntity buildJsonEntity(List<JSONObject> tracked) throws JSONException, UnsupportedEncodingException {
        JSONObject body = new JSONObject();
        body.put(FIELD_NAME_DATA, new JSONArray(tracked));
        body.put(FIELD_NAME_COMPRESS, FIELD_VALUE_COMPRESS);

        return new StringEntity(body.toString());
    }

    private void verifyStatusCode(int statusCode) throws
            RakeProtocolBrokenException,
            InvalidEndPointException,
            InternalServerErrorException {

            switch(statusCode) {
                // TODO 204, 206
                case HttpStatus.SC_NOT_FOUND:
                    throw new InvalidEndPointException("");
                case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                    throw new InternalServerErrorException("");
                default: break; /* pass through */
            }
    }

    private String verifyResponseBody(String responseBody) throws
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
            // TODO MUST 204, 206
            case RakeProtocolV2.ERROR_CODE_OK: /* pass through */
                break;
            case RakeProtocolV2.ERROR_CODE_INSUFFICIENT_JSON_FIELD:
                throw new InsufficientJsonFieldException(responseBody);
            case RakeProtocolV2.ERROR_CODE_INVALID_JSON_SYNTAX:
                throw new InvalidJsonSyntaxException(responseBody);
            case RakeProtocolV2.ERROR_CODE_NOT_REGISTERED_RAKE_TOKEN:
                throw new NotRegisteredRakeTokenException(responseBody);
            case RakeProtocolV2.ERROR_CODE_WRONG_RAKE_TOKEN_USAGE:
                throw new WrongRakeTokenUsageException(responseBody);

            default: throw new RakeProtocolBrokenException(responseBody);
        }

        return responseBody;
    }

    @Override
    public HttpPost buildRequest(List<JSONObject> tracked, String endPoint)
            throws UnsupportedEncodingException, JSONException {
        HttpPost post = new HttpPost(endPoint);

        post.setHeader("Content-Type", "application/json");
        post.setHeader("Accept", "application/json");

        HttpEntity entity = buildJsonEntity(tracked);
        post.setEntity(entity);

        return post;
    }

    @Override
    public void verifyResponse(int statusCode, String responseBody) {
        try {
            verifyStatusCode(statusCode);
            verifyResponseBody(responseBody);
            // TODO metric
        } catch (RakeProtocolBrokenException e) {
            RakeLogger.error(e);
        } catch (InsufficientJsonFieldException e) {
            RakeLogger.error(e);
        } catch (InvalidJsonSyntaxException e) {
            RakeLogger.error(e);
        } catch (NotRegisteredRakeTokenException e) {
            RakeLogger.error(e);
        } catch (WrongRakeTokenUsageException e) {
            RakeLogger.error(e);
        } catch (InvalidEndPointException e) {
            RakeLogger.error(e);
        } catch (InternalServerErrorException e) {
            // signaling retry. this exception will be handled by RakeHttpClient
            throw e;
        }
    }

}
