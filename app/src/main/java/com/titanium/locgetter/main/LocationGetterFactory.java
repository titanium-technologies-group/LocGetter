package com.titanium.locgetter.main;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


/**
 * Used to create new instances of {@link LocationGetter}
 */
public class LocationGetterFactory {

    /**
     * Creates default instance of Location getter with initializing new instance of {@link GoogleApiClient}
     * <p>Please call for {@link LocationGetterFactory#getCustomLocationGetter(Context, LocationRequest, GoogleApiClient)} in case if you already
     * have initialized {@link GoogleApiClient} with enabled {@link LocationServices#API}</p>
     *
     * @param applicationContext context for initializing {@link GoogleApiClient}
     * @return new instance of default {@link LocationGetter}
     */
    public static LocationGetter getDefault(@NonNull Context applicationContext) {
        return new LocationGetterImpl(applicationContext, getDefaultLocationRequest(),
            getMinimalGoogleApiClient(applicationContext));
    }

    /**
     * Creates location getter with custom location request
     *
     * @param applicationContext for init google api client
     * @param fastestInterval    defines {@link LocationRequest#getFastestInterval()}
     * @param interval           defines {@link LocationRequest#getInterval()}
     * @param priority           defines {@link LocationRequest#getPriority()}
     * @return new instance of Location getter with specific {@link LocationRequest} and default {@link GoogleApiClient}
     */
    public static LocationGetter getWithCustomLocationRequest(@NonNull Context applicationContext, long fastestInterval,
                                                              long interval, int priority) {
        return new LocationGetterImpl(applicationContext, getLocationRequestByParams(fastestInterval, interval, priority),
            getMinimalGoogleApiClient(applicationContext));
    }

    /**
     * Creates custom location getter
     *
     * @param ctx             to initialize {@link GoogleApiClient}
     * @param request         to define behavior of getting locations
     * @param googleApiClient your initialiazed or not api client.
     */
    public static LocationGetter getCustomLocationGetter(@NonNull Context ctx, @NonNull LocationRequest request, @NonNull GoogleApiClient googleApiClient) {
        return new LocationGetterImpl(ctx, request, googleApiClient);
    }

    private static GoogleApiClient getMinimalGoogleApiClient(Context ctx) {
        return new GoogleApiClient.Builder(ctx)
            .addApi(LocationServices.API)
            .build();
    }

    private static LocationRequest getDefaultLocationRequest() {
        return getLocationRequestByParams(2000, 2000, LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private static LocationRequest getLocationRequestByParams(long fastestInterval, long interval, int priority) {
        LocationRequest locationrequest = new LocationRequest();
        locationrequest.setFastestInterval(fastestInterval);
        locationrequest.setInterval(interval);
        locationrequest.setPriority(priority);
        return locationrequest;
    }

}
