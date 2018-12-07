package com.titanium.locgetter.main

import android.Manifest
import android.location.Location
import com.titanium.locgetter.exception.PermissionException
import io.reactivex.Observable


class LocationProviderFromApplication(mainLocationGetter: MainLocationGetter) : BaseLocationsProvider(mainLocationGetter) {

    override fun getLatestLocations(): Observable<Location> {
        if (!mainLocationGetter.isLocationPermitted())
            return Observable.error(PermissionException(Manifest.permission.ACCESS_FINE_LOCATION))
        return Observable.create<Any> { emitter ->
            mainLocationGetter.getLocationSettingsStatus({ emitter.onError(it) }, emitter)
        }.flatMap { mainLocationGetter.locationShare }
    }

}
