package com.skp.di.rake.client.logger;


import com.skp.di.rake.client.protocol.RakeProtocol;
import com.skp.di.rake.client.protocol.exception.InsufficientJsonFieldException;
import com.skp.di.rake.client.protocol.exception.InternalServerErrorException;
import com.skp.di.rake.client.protocol.exception.InvalidEndPointException;
import com.skp.di.rake.client.protocol.exception.InvalidJsonSyntaxException;
import com.skp.di.rake.client.protocol.exception.NotRegisteredRakeTokenException;
import com.skp.di.rake.client.protocol.exception.RakeProtocolBrokenException;
import com.skp.di.rake.client.protocol.exception.WrongRakeTokenUsageException;

import mock.MockServer;
import mock.TestRakeFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeLoggerHttpResponseSpec {

    Rake logger;
    JSONObject json;

    @Before
    public void setUp() throws JSONException {
        logger = TestRakeFactory.getLogger("token", null);

        json = new JSONObject();
        json.put("rake_lib", "0.0.1");
    }

    @After
    public void tearDown() {

    }

    @Test(expected= InsufficientJsonFieldException.class)
    public void testInsufficientJsonFieldException() {
        logger.track(json);

        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_INSUFFICIENT_JSON_FIELD);
        logger.flush();
    }

    @Test(expected= InvalidJsonSyntaxException.class)
    public void testInvalidJsonSyntaxException() {
        logger.track(json);

        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_INVALID_JSON_SYNTAX);
        logger.flush();
    }

    @Test(expected= NotRegisteredRakeTokenException.class)
    public void testNotRegisteredRakeTokenException() {
        logger.track(json);

        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_NOT_REGISTERED_RAKE_TOKEN);
        logger.flush();
    }

    @Test(expected= WrongRakeTokenUsageException.class)
    public void testWrongRakeTokenUsageException() {
        logger.track(json);

        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_WRONG_RAKE_TOKEN_USAGE);
        logger.flush();
    }

    @Test(expected= InvalidEndPointException.class)
    public void testInvalidEndPointException() {
        logger.track(json);

        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_INVALID_END_POINT);
        logger.flush();
    }

    @Test(expected= InternalServerErrorException.class)
    public void testInternalServerErrorException() {
        logger.track(json);

        MockServer.setErrorCode(RakeProtocol.ERROR_CODE_INTERNAL_SERVER_ERROR);
        logger.flush();
    }

    @Test(expected= RakeProtocolBrokenException.class)
    public void testRakeProtocolBrokenExceptionWhenServerReturnInvalidJsonFormat() {
        logger.track(json);

        /* mock server will return invalid json format */
        MockServer.setErrorCode(MockServer.ERROR_CODE_RAKE_PROTOCOL_BROKEN);
        logger.flush();
    }

    @Test(expected= RakeProtocolBrokenException.class)
    public void testRakeProtocolBrokenExceptionWhenServerReturnInvalidErrorAndStatusCode() {
        logger.track(json);

        /* mock server will return undefined error code and status code */
        MockServer.setErrorCode(909014);
        logger.flush();
    }

}
