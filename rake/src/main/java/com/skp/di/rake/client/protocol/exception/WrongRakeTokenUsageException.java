package com.skp.di.rake.client.protocol.exception;

public class WrongRakeTokenUsageException extends RakeException {
    private static final long serialVersinoUID = 0;
    private Throwable cause;

    static public final int ERROR_CODE = 40301;

    public WrongRakeTokenUsageException(String message) {
        super(message);
    }

    public WrongRakeTokenUsageException(Throwable cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }
}
