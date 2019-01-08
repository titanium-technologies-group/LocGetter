package com.titanium.locgetter.main

import android.content.Context
import android.util.Log
import com.titanium.locgetter.exception.MockLocationException

/**
 * Used to create new instances of [LocationGetter]
 */
class LocationGetterBuilder(private val context: Context) {

    /**
     * Custom logger
     * By default all logs goes to [Log.i]
     */
    private var logger: (String, String) -> Unit = { tag, message -> Log.d(tag, message) }

    /**
     * Defines if locations getter will accept mocked locations
     * If set to false, mocked locations will cause a [MockLocationException] to be thrown
     * By default true
     */
    private var acceptMockLocations = true

    /**
     * Custom logger
     * By default all logs goes to [Log.i]
     */
    fun logger(logger: (String, String) -> Unit): LocationGetterBuilder {
        this.logger = logger
        return this
    }

    /**
     * Defines if locations getter will accept mocked locations
     * If set to false, mocked locations will cause a [MockLocationException] to be thrown
     * By default true
     */
    fun acceptMockLocations(acceptMockLocations: Boolean): LocationGetterBuilder {
        this.acceptMockLocations = acceptMockLocations
        return this
    }

    /**
     * Builds new instance of [LocationGetter]
     */
    fun build(): LocationGetter = MainLocationGetter(context.applicationContext, logger, acceptMockLocations)


}
