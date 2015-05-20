package mock;

import com.skp.di.rake.client.protocol.RakeProtocol;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MockServer {

    static public final int ERROR_CODE_RAKE_PROTOCOL_BROKEN = 90001;

    static private int errorCode;

    static public void setErrorCode(int errorCode) {
        MockServer.errorCode = errorCode;
    }

    /* mocking server action */
    static public HttpResponse respond() {

        return createResponse();
    }

    static private HttpResponse createResponse() {

        HttpResponse res = null;

        try {
            res = new BasicHttpResponse(HttpVersion.HTTP_1_1, 0, null);

            // fill statusCoed
            res.setStatusCode(getStatusCode());

            // fill body
            JSONObject body = createResponseBody();
            StringEntity entity = new StringEntity(body.toString());
            res.setEntity(entity);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return res;
    }

    static private JSONObject createResponseBody() throws JSONException {
        JSONObject body = new JSONObject();

        if (ERROR_CODE_RAKE_PROTOCOL_BROKEN != errorCode) {
            body.put("statusCode", getStatusCode());
            body.put("errorCode", errorCode);
            body.put("message", "");
            body.put("moreInfo", "http://www.google.com");
        }

        return body;
    }

    static private int getStatusCode() {
        switch (errorCode) {
            case RakeProtocol.ERROR_CODE_OK:
                return HttpStatus.SC_OK;
            case RakeProtocol.ERROR_CODE_INSUFFICIENT_JSON_FIELD:
            case RakeProtocol.ERROR_CODE_INVALID_JSON_SYNTAX:
                return HttpStatus.SC_BAD_REQUEST;
            case RakeProtocol.ERROR_CODE_NOT_REGISTERED_RAKE_TOKEN:
                return HttpStatus.SC_UNAUTHORIZED;
            case RakeProtocol.ERROR_CODE_WRONG_RAKE_TOKEN_USAGE:
                return HttpStatus.SC_FORBIDDEN;
            case RakeProtocol.ERROR_CODE_INVALID_END_POINT:
                return HttpStatus.SC_NOT_FOUND;
            case RakeProtocol.ERROR_CODE_INTERNAL_SERVER_ERROR:
                return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }

        return 0;
    }
}
