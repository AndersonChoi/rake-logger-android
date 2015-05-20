package com.skp.di.rake.client.protocol.exception;

public class InvalidJsonSyntaxException extends RakeException {
    private static final long serialVersinoUID = 0;
    private Throwable cause;

    static public final int ERROR_CODE = 40002;

    public InvalidJsonSyntaxException(String message) {
        super(message);
    }

    public InvalidJsonSyntaxException(Throwable cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }
}
