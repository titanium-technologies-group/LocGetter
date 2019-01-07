package com.titanium.locgetter.main

import android.location.Location

import io.reactivex.Observable
import io.reactivex.Single
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

    @Deprecated("")
    fun getLatestSavedLocation(): Location?

    /**
     * Gets latest saved location
     */
    fun getLatestSavedLocationSingle(): Single<Location>

    fun getHotLocations(): Subject<Location>

}
