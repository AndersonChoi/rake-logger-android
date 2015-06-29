package com.skp.di.rake.client.protocol;

import com.skp.di.rake.client.protocol.exception.InsufficientJsonFieldException;
import com.skp.di.rake.client.protocol.exception.InternalServerErrorException;
import com.skp.di.rake.client.protocol.exception.InvalidEndPointException;
import com.skp.di.rake.client.protocol.exception.InvalidJsonSyntaxException;
import com.skp.di.rake.client.protocol.exception.NotRegisteredRakeTokenException;
import com.skp.di.rake.client.protocol.exception.WrongRakeTokenUsageException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class RakeProtocolCodeSpec {

    @Test
    public void testErrorCodeShouldBeMappedToException() {
        assertEquals(
                RakeProtocolV2.ERROR_CODE_INSUFFICIENT_JSON_FIELD,
                InsufficientJsonFieldException.ERROR_CODE);

        assertEquals(
                RakeProtocolV2.ERROR_CODE_INVALID_JSON_SYNTAX,
                InvalidJsonSyntaxException.ERROR_CODE);

        assertEquals(
                RakeProtocolV2.ERROR_CODE_NOT_REGISTERED_RAKE_TOKEN,
                NotRegisteredRakeTokenException.ERROR_CODE);

        assertEquals(
                RakeProtocolV2.ERROR_CODE_WRONG_RAKE_TOKEN_USAGE,
                WrongRakeTokenUsageException.ERROR_CODE);

        assertEquals(
                RakeProtocolV2.ERROR_CODE_INVALID_END_POINT,
                InvalidEndPointException.ERROR_CODE);

        assertEquals(
                RakeProtocolV2.ERROR_CODE_INTERNAL_SERVER_ERROR,
                InternalServerErrorException.ERROR_CODE);
    }

    @Test
    public void testErrorCodeShouldBeSameAsPredefinedOne() {

        assertEquals(
                20000,
                RakeProtocolV2.ERROR_CODE_OK);

        assertEquals(
                40001,
                RakeProtocolV2.ERROR_CODE_INSUFFICIENT_JSON_FIELD);

        assertEquals(
                40002,
                RakeProtocolV2.ERROR_CODE_INVALID_JSON_SYNTAX);

        assertEquals(
                40101,
                RakeProtocolV2.ERROR_CODE_NOT_REGISTERED_RAKE_TOKEN);

        assertEquals(
                40301,
                RakeProtocolV2.ERROR_CODE_WRONG_RAKE_TOKEN_USAGE);

        assertEquals(
                40401,
                RakeProtocolV2.ERROR_CODE_INVALID_END_POINT);

        assertEquals(
                50001,
                RakeProtocolV2.ERROR_CODE_INTERNAL_SERVER_ERROR);

    }
}
