package com.titanium.locgetter.main

import android.content.Context
import android.util.Log

object LocationProviderFactory {

    lateinit var mainLocationGetter: MainLocationGetter

    @JvmOverloads
    @Synchronized
    fun getProvider(context: Context,
                    logger: ((String, String) -> Unit) = { tag, message -> Log.d(tag, message) }): LocationGetter {
        if (!::mainLocationGetter.isInitialized)
            mainLocationGetter = MainLocationGetter(context.applicationContext, logger)
        return mainLocationGetter
    }

    @JvmStatic
    fun getInstance() = LocationProviderFactory

}