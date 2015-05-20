package com.skp.di.rake.client.protocol.exception;

public class RakeProtocolBrokenException extends RakeException {
    private static final long serialVersinoUID = 0;
    private Throwable cause;

    static public final int ERROR_CODE = 40101;

    public RakeProtocolBrokenException(String message) {
        super(message);
    }

    public RakeProtocolBrokenException(Throwable cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }
}
