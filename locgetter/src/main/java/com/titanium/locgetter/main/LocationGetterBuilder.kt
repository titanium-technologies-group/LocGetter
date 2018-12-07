package com.titanium.locgetter.main

import android.content.Context
import android.util.Log

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

/**
 * Used to create new instances of [LocationGetter]
 */
@Deprecated("")
class LocationGetterBuilder(private val context: Context) {

    /**
     * Custom logger
     */
    private var logger: Logger? = null

    /**
     * Sets custom location request to this location getter
     * @param locationRequest defines interval , priority of location updates
     */
    fun setLocationRequest(locationRequest: LocationRequest): LocationGetterBuilder {
        return this
    }

    /**
     * Sets custom logger to [LocationGetter]
     * By default all logs goes to [Log.i]
     */
    fun setLogger(logger: Logger): LocationGetterBuilder {
        this.logger = logger
        return this
    }

    /**
     * Sets google api client to this location builder.
     * Important : [GoogleApiClient] should contain [LocationServices.API]
     * If it is not initialized, it will be initialized in lazy way
     * @param googleApiClient with enabled [LocationServices.API]
     */
    fun setGoogleApiClient(googleApiClient: GoogleApiClient): LocationGetterBuilder {
        return this
    }

    /**
     * Builds new instance of [LocationGetter]
     *  If not defined [GoogleApiClient] and [LocationRequest] will use default
     */
    fun build(): LocationGetter {
        logger?.let { return LocationProviderFactory.getProvider(context) { tag: String, messgae: String -> it.log(tag, messgae) } }
        return LocationProviderFactory.getProvider(context)
    }


}
