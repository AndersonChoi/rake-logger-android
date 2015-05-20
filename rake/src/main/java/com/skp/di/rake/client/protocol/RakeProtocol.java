package com.skp.di.rake.client.protocol;

public class RakeProtocol {
    /* Ref: http://wiki.skplanet.com/display/DIT/Rake+API+Spec */
    public static final int ERROR_CODE_OK                        = 20000;
    public static final int ERROR_CODE_INSUFFICIENT_JSON_FIELD   = 40001;
    public static final int ERROR_CODE_INVALID_JSON_SYNTAX       = 40002;
    public static final int ERROR_CODE_NOT_REGISTERED_RAKE_TOKEN = 40101;
    public static final int ERROR_CODE_WRONG_RAKE_TOKEN_USAGE    = 40301;
    public static final int ERROR_CODE_INVALID_END_POINT         = 40401;
    public static final int ERROR_CODE_INTERNAL_SERVER_ERROR     = 50001;

}
