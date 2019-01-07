package com.titanium.locgetter.main

import android.content.Context
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest

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
     * Sets custom logger to [LocationGetter]
     * By default all logs goes to [Log.i]
     */
    fun setLogger(logger: Logger): LocationGetterBuilder {
        this.logger = logger
        return this
    }

    /**
     * Builds new instance of [LocationGetter]
     *  If not defined [GoogleApiClient] and [LocationRequest] will use default
     */
    fun build(): LocationGetter {
        logger?.let { return LocationProviderFactory.getProvider(context) { tag: String, message: String -> it.log(tag, message) } }
        return LocationProviderFactory.getProvider(context)
    }


}
