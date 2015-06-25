package com.skp.di.rake.client.protocol;

import com.skp.di.rake.client.protocol.exception.InsufficientJsonFieldException;
import com.skp.di.rake.client.protocol.exception.InternalServerErrorException;
import com.skp.di.rake.client.protocol.exception.InvalidEndPointException;
import com.skp.di.rake.client.protocol.exception.InvalidJsonSyntaxException;
import com.skp.di.rake.client.protocol.exception.NotRegisteredRakeTokenException;
import com.skp.di.rake.client.protocol.exception.RakeProtocolBrokenException;
import com.skp.di.rake.client.protocol.exception.WrongRakeTokenUsageException;
import com.skp.di.rake.client.utils.RakeLogger;
import com.skp.di.rake.client.utils.StringUtils;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class RakeProtocol {
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

    static public UrlEncodedFormEntity buildUrlEncodedEntity(List<JSONObject> tracked) throws JSONException, UnsupportedEncodingException {
        List<NameValuePair> pairs = new ArrayList<>(2);

        String FIELD_VALUE_DATA = StringUtils.encodeBase64(new JSONArray(tracked).toString());

        pairs.add(new BasicNameValuePair(
                RakeProtocol.FIELD_NAME_COMPRESS, RakeProtocol.FIELD_VALUE_COMPRESS));
        pairs.add(new BasicNameValuePair(
                RakeProtocol.FIELD_NAME_DATA, FIELD_VALUE_DATA));

        return new UrlEncodedFormEntity(pairs);
    }

    static public StringEntity buildJsonEntity(List<JSONObject> tracked) throws JSONException, UnsupportedEncodingException {
        JSONObject body = new JSONObject();
        body.put(FIELD_NAME_DATA, new JSONArray(tracked));
        body.put(FIELD_NAME_COMPRESS, FIELD_VALUE_COMPRESS);

        return new StringEntity(body.toString());
    }

    static public void verifyStatusCode(int statusCode) throws
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

    static public String verifyErrorCode(String responseBody) throws
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

    static public void handleRakeException(int statusCode, String responseBody) {
        try {
            verifyStatusCode(statusCode);
            verifyErrorCode(responseBody);
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
            RakeLogger.error(e);
        }
    }

}
