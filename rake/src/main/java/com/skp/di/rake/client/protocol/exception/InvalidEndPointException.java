package com.skp.di.rake.client.protocol.exception;

public class InvalidEndPointException extends RakeException {
    private static final long serialVersinoUID = 0;
    private Throwable cause;

    static public final int ERROR_CODE = 40401;

    public InvalidEndPointException(String message) {
        super(message);
    }

    public InvalidEndPointException(Throwable cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }
}
