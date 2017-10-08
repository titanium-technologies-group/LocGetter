package com.titanium.locgetter.exception;

public class NoGoogleApiException extends RuntimeException {
    private int errorCode;

    public NoGoogleApiException(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
