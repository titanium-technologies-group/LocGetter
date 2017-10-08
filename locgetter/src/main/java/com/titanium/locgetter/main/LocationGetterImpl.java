package com.titanium.locgetter.main;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.titanium.locgetter.exception.LocationSettingsException;
import com.titanium.locgetter.exception.NoGoogleApiException;
import com.titanium.locgetter.exception.PermissionException;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.titanium.locgetter.main.Constants.ACCESS_LOCATION_PERMISSION_RESULT;
import static com.titanium.locgetter.main.Constants.THROWABLE_KEY_LOCATION;

class LocationGetterImpl implements LocationGetter {

    private static final String TAG = "LocationGetter";
    private Logger logger;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Context appContext;
    private Location latestLoc;

    /**
     * @param logger          for custom logging
     * @param ctx             application context to prevent leaks
     * @param request         location request to define
     * @param googleApiClient needed for turning on locations api
     */
    LocationGetterImpl(Logger logger, Context ctx, LocationRequest request, GoogleApiClient googleApiClient) {
        this.logger = logger;
        this.googleApiClient = googleApiClient;
        appContext = ctx;
        locationRequest = request;
    }

    @Override
    public Observable<Location> getLatestLocations() {
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return Observable.just(googleApiClient)
                .subscribeOn(Schedulers.newThread())
                .doOnNext(this::initGoogleApiClient)
                .doOnNext(client -> checkGoogleApiAvailability())
                .map(this::getLocationSettingsStatus)
                .doOnNext(this::checkSettingsStatus)
                .flatMap(integer -> new LocationSniffer().startEmittingLocations());
        }
        return Observable.error(new PermissionException(Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_LOCATION_PERMISSION_RESULT));
    }

    private void checkGoogleApiAvailability() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int errorCode = googleApiAvailability.isGooglePlayServicesAvailable(appContext);
        if (errorCode != ConnectionResult.SUCCESS) {
            throw new NoGoogleApiException(errorCode);
        }
    }

    private void initGoogleApiClient(GoogleApiClient googleApiClient) {
        if (!googleApiClient.isConnected()) {
            ConnectionResult res = googleApiClient.blockingConnect();
            if (!res.isSuccess()) {
                throw new RuntimeException(THROWABLE_KEY_LOCATION);
            }
        }
        logger.log(TAG, "Google api client connected");
    }

    private Status getLocationSettingsStatus(GoogleApiClient googleApiClient) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.setAlwaysShow(true);
        builder.addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        return result.await().getStatus();
    }

    private void checkSettingsStatus(Status status) {
        logger.log(TAG, "checkSettingsStatus -> settings status = " + status);
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                logger.log(TAG, "checkSettingsStatus:Location is enabled");
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                logger.log(TAG, "checkSettingsStatus:Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                throw new LocationSettingsException(status);
        }
    }

    @Override
    public Single<Location> getLatestLocation() {
        return getLatestLocations()
            .firstOrError();
    }

    @Nullable
    @Override
    public Location getLatestSavedLocation() {
        return latestLoc;
    }

    private class LocationSniffer {
        private LocationListener listener;

        @SuppressWarnings({"MissingPermission"})
        private Observable<Location> startEmittingLocations() {
            //wrap callback with observable.create
            return Observable.create(e -> {
                listener = location -> {
                    //as soon as it is disposed - unsubscribe from updates
                    if (e.isDisposed())
                        unSubscribeFromUpdates();
                    else {
                        logger.log(TAG, "Got new location :" + location);
                        latestLoc = location;
                        e.onNext(location);
                    }
                };
                subscribeToUpdates();
            });
        }

        @SuppressWarnings({"MissingPermission"})
        private void subscribeToUpdates() {
            //subscribe should happen on main thread
            AndroidSchedulers.mainThread().scheduleDirect(() -> LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, listener));
        }

        private void unSubscribeFromUpdates() {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, listener);
        }
    }
}
