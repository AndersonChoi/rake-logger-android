package com.skp.di.rake.client.protocol.exception;

public class InsufficientJsonFieldException extends RakeException {

    private static final long serialVersinoUID = 0;
    private Throwable cause;

    static public final int ERROR_CODE = 40001;

    public InsufficientJsonFieldException(String message) {
        super(message);
    }

    public InsufficientJsonFieldException(Throwable cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }
}
