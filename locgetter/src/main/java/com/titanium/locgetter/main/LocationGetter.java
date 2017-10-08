package com.titanium.locgetter.main;

import android.location.Location;
import android.support.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface LocationGetter {
    /**
     * Gets observable with all latest {@link Location}'s of user
     *
     * @return Observable emiting actual user locations
     * @throws com.titanium.locgetter.exception.PermissionException       when there is missing permission for location
     * @throws com.titanium.locgetter.exception.LocationSettingsException when location settings are turned off
     * @throws com.titanium.locgetter.exception.NoGoogleApiException      when there is no google api on phone
     * @throws RuntimeException                                           when google api client cannot connect
     */
    Observable<Location> getLatestLocations();

    /**
     * Gets latest {@link Location} of user
     *
     * @throws com.titanium.locgetter.exception.PermissionException       when there is missing permission for location
     * @throws com.titanium.locgetter.exception.LocationSettingsException when location settings are turned off
     * @throws com.titanium.locgetter.exception.NoGoogleApiException      when there is no google api on phone
     * @throws RuntimeException                                           when google api client cannot connect
     */
    Single<Location> getLatestLocation();

    /**
     * Gets latest saved location
     * <p>Can be null in case if location managed wasn't used before and there is no buffered locations</p>
     */
    @Nullable
    Location getLatestSavedLocation();
}
