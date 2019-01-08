package com.titanium.locgetter.main

import android.location.Location
import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface LocationGetter {

    /**
     * Gets observable with all latest [Location]'s of user
     */
    fun getLatestLocations(): Observable<Location>

    /**
     * Gets latest [Location] of user
     */
    fun getLatestLocation() = getLatestLocations().firstOrError()

    /**
     * Gets latest location that was received by location getter
     */
    fun getLatestSavedLocation(): Location?

    /**
     * Subject that emits all locations received by this location getter
     */
    val hotLocations: Subject<Location>

}
