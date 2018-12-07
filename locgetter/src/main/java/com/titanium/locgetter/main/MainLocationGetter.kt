package com.titanium.locgetter.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.titanium.locgetter.exception.PermissionException
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject


class MainLocationGetter constructor(private val appContext: Context,
                                     private val logger: ((String, String) -> Unit)) {

    val hotLocations: Subject<Location> = PublishSubject.create()
    private val locationRequest: LocationRequest
    private var fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(appContext)
    val locationShare: Observable<Location> = Observable.defer { LocationSniffer().startEmittingLocations() }
            .share()

    init {
        locationRequest = LocationRequest.create()
        locationRequest.fastestInterval = 5000
        locationRequest.interval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    internal fun isLocationPermitted() = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun getLatestSavedLocationSingle() = if (isLocationPermitted())
        Single.create<Location> {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener { loc ->
                if (loc.isSuccessful || loc.result != null)
                    it.onSuccess(loc.result!!)
                else
                    it.onError(RuntimeException())
            }
        }
    else
        Single.error(PermissionException(Manifest.permission.ACCESS_FINE_LOCATION))

    fun singleGetLatestSavedLocationBlocking(): Location = getLatestSavedLocationSingle().blockingGet()

    internal fun getLocationSettingsStatus(onResolvable: (ResolvableApiException) -> Unit = {},
                                           subscriber: Emitter<Any>) {
        val builder = LocationSettingsRequest.Builder()
        builder.setAlwaysShow(true)
        builder.addLocationRequest(locationRequest)
        val result = LocationServices.getSettingsClient(appContext).checkLocationSettings(builder.build())
        result.addOnCompleteListener {
            try {
                it.getResult(ApiException::class.java)
                subscriber.onNext("")
                subscriber.onComplete()
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try {
                            onResolvable(exception as ResolvableApiException)
                        } catch (e: Throwable) {
                            subscriber.onError(e)
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                        subscriber.onError(exception)
                }
            }
        }
    }

    private inner class LocationSniffer {
        private var onLocation: ((Location) -> Unit)? = null
        private var listener: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                p0?.lastLocation?.let { onLocation?.invoke(it) }
            }
        }

        fun startEmittingLocations(): Observable<Location> {
            //wrap callback with observable.create
            return Observable.create { e ->
                onLocation = { location ->
                    //as soon as it is disposed - unsubscribe from updates
                    if (e.isDisposed)
                        unSubscribeFromUpdates()
                    else {
                        logger(TAG, "Got new location :$location ,speed :${location.speed}")
                        hotLocations.onNext(location)
                        e.onNext(location)
                    }
                }
                subscribeToUpdates()
            }
        }

        @SuppressLint("MissingPermission")
        private fun subscribeToUpdates() = AndroidSchedulers.mainThread().scheduleDirect { fusedLocationProviderClient.requestLocationUpdates(locationRequest, listener, null) }

        private fun unSubscribeFromUpdates() = fusedLocationProviderClient.removeLocationUpdates(listener)
    }

    companion object {
        val TAG = "MainLocationGetter"
    }
}


abstract class BaseLocationsProvider(protected val mainLocationGetter: MainLocationGetter) : LocationGetter {

    override fun getLatestSavedLocation() = mainLocationGetter.singleGetLatestSavedLocationBlocking()

    override fun getLatestSavedLocationSingle() = mainLocationGetter.getLatestSavedLocationSingle()

    override fun getHotLocations() = mainLocationGetter.hotLocations
}
