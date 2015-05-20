package com.skp.di.rake.client.protocol.exception;

public abstract class RakeException extends RuntimeException {
    private static final long serialVersinoUID = 0;
    private Throwable cause;

    public RakeException(String message) {
        super(message);
    }

    public RakeException(Throwable cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }
}
