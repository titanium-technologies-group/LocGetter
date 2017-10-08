package com.titanium.locgetter.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


/**
 * Used to create new instances of {@link LocationGetter}
 */
public class LocationGetterBuilder {

    /**
     * Defines interval,priority of location updates
     */
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    /**
     * Application context
     */
    private Context context;
    /**
     * Custom logger
     */
    private Logger logger;

    /**
     * Creates new instance of builder
     */
    public LocationGetterBuilder(@NonNull Context context) {
        this.context = context;
    }

    /**
     * Sets custom location request to this location getter
     *
     * @param locationRequest defines interval , priority of location updates
     */
    public LocationGetterBuilder setLocationRequest(LocationRequest locationRequest) {
        this.locationRequest = locationRequest;
        return this;
    }

    /**
     * Sets custom logger to {@link LocationGetter}
     * By default all logs goes to {@link Log#i}
     */
    public LocationGetterBuilder setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * Sets google api client to this location builder.
     * <p>Important : {@link GoogleApiClient} should contain {@link LocationServices#API}</p>
     * <p>If it is not initialized, it will be initialized in lazy way</p>
     *
     * @param googleApiClient with enabled {@link LocationServices#API}
     */
    public LocationGetterBuilder setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
        return this;
    }

    /**
     * Builds new instance of {@link LocationGetter}
     * <p> If not defined {@link GoogleApiClient} and {@link LocationRequest} will use default</p>
     */
    public LocationGetter build() {
        if (googleApiClient == null)
            googleApiClient = getMinimalGoogleApiClient(context);
        if (locationRequest == null)
            locationRequest = getDefaultLocationRequest();
        if (logger == null)
            logger = getDefaultLogger();
        return new LocationGetterImpl(logger, context, locationRequest, googleApiClient);
    }

    private GoogleApiClient getMinimalGoogleApiClient(Context ctx) {
        return new GoogleApiClient.Builder(ctx)
            .addApi(LocationServices.API)
            .build();
    }

    private LocationRequest getDefaultLocationRequest() {
        return getLocationRequestByParams(Constants.DEFAULT_FASTEST_INTERVAL, Constants.DEFAULT_INTERVAL, LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private Logger getDefaultLogger() {
        return Log::i;
    }

    private LocationRequest getLocationRequestByParams(long fastestInterval, long interval, int priority) {
        LocationRequest locationrequest = new LocationRequest();
        locationrequest.setFastestInterval(fastestInterval);
        locationrequest.setInterval(interval);
        locationrequest.setPriority(priority);
        return locationrequest;
    }

}
