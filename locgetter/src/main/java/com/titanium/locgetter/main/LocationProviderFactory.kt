package com.titanium.locgetter.main

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.Log

object LocationProviderFactory {


    var mainLocationGetter: MainLocationGetter? = null

    @JvmOverloads
    fun getProvider(context: Context,
                    logger: ((String, String) -> Unit) = { tag, message -> Log.d(tag, message) }): LocationGetter {
        if (mainLocationGetter == null)
            mainLocationGetter = MainLocationGetter(context.applicationContext, logger)

        return when (context) {
            is FragmentActivity -> LocationProviderFromActivity(context, mainLocationGetter!!)
            else -> LocationProviderFromApplication(mainLocationGetter!!)
        }
    }

    @JvmStatic
    fun getInstance() = LocationProviderFactory

}