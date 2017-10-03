package com.titanium.locgetter.exception;

import com.google.android.gms.common.api.Status;

public class LocationSettingsException extends RuntimeException {
    private Status status;

    public LocationSettingsException(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}