package com.titanium.locgetter.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.support.v4.content.ContextCompat
import codes.titanium.connectableactivity.launchConnectableActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.titanium.locgetter.exception.LocationSettingsException
import com.titanium.locgetter.exception.MockLocationException
import com.titanium.locgetter.exception.NoLocationPermission
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

internal class MainLocationGetter constructor(private val appContext: Context,
                                              private val logger: ((String, String) -> Unit),
                                              private val acceptMockLocations: Boolean) : LocationGetter {

    override val hotLocations: Subject<Location> = PublishSubject.create()
    private var latestLocation: Location? = null
    private val locationRequest: LocationRequest
    private var fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(appContext)
    private val locationShare: Observable<Location> = LocationSniffer().startEmittingLocations().share()
    private val settingsShare: Observable<Any> = getLocationSettingsStatus().share()
    private val permissionsShare: Observable<Any> = checkPermissions().share()

    init {
        locationRequest = LocationRequest.create()
        locationRequest.fastestInterval = 5000
        locationRequest.interval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun getLatestSavedLocation() = latestLocation

    override fun getLatestLocations() = permissionsShare
        .flatMap { settingsShare }
        .flatMap { locationShare }

    internal fun getLocationSettingsStatus() = Observable.create<Any> { subscriber ->
        if (isLocationEnabled()) {
            subscriber.onNext("")
            subscriber.onComplete()
        } else {
            val builder = LocationSettingsRequest.Builder()
                .setAlwaysShow(true)
                .addLocationRequest(locationRequest)
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
                                showSettings(subscriber, exception as ResolvableApiException)
                            } catch (e: Throwable) {
                                subscriber.onError(e)
                            }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                            subscriber.onError(exception)
                    }
                }
            }
        }
    }

    private fun showSettings(subscriber: ObservableEmitter<Any>, resolvableApiException: ResolvableApiException) {
        val onError = { if (!subscriber.isDisposed) subscriber.onError(LocationSettingsException()) }
        val askRequest: (Activity) -> Unit = { resolvableApiException.startResolutionForResult(it, 0) }
        val askResult: (Int, Intent?) -> Unit = { resultCode, _ ->
            when (resultCode) {
                Activity.RESULT_OK -> {
                    subscriber.onNext("")
                    subscriber.onComplete()
                }
                else -> onError()
            }
        }
        appContext.launchConnectableActivity(askRequest, onActivityResult = askResult, onDeAttach = onError)
    }

    @SuppressLint("NewApi")
    private fun checkPermissions() = Observable.create<Any> { emitter ->
        if (isLocationPermitted()) {
            emitter.onNext("")
            emitter.onComplete()
        } else {
            val permRequest: (Activity) -> Unit = { f -> f.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0) }
            val onDeAttach: () -> Unit = { if (!emitter.isDisposed) emitter.onError(RuntimeException("timeout")) }
            val permResult: (Boolean) -> Unit = { isGranted ->
                if (isGranted) {
                    emitter.onNext("")
                    emitter.onComplete()
                } else {
                    emitter.onError(NoLocationPermission())
                }
            }
            appContext.launchConnectableActivity(permRequest, onRequestPermissionsResult = permResult, onDeAttach = onDeAttach)
        }
    }

    private fun isLocationPermitted() = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun isLocationEnabled() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val lm = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.isLocationEnabled
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        val mode = Settings.Secure.getInt(appContext.contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF)
        mode != Settings.Secure.LOCATION_MODE_OFF
    } else {
        val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private inner class LocationSniffer {
        private var onLocation: ((Location) -> Unit)? = null
        private var listener: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                p0?.lastLocation?.let { onLocation?.invoke(it) }
            }
        }

        @SuppressLint("NewApi")
        fun startEmittingLocations(): Observable<Location> {
            //wrap callback with observable.create
            return Observable.create { e ->
                onLocation = { location ->
                    //as soon as it is disposed - unsubscribe from updates
                    if (e.isDisposed)
                        unSubscribeFromUpdates()
                    else {
                        if (location.isFromMockProvider && !acceptMockLocations) {
                            e.onError(MockLocationException(location))
                        } else {
                            logger(TAG, "Got new location :$location ,speed :${location.speed}")
                            latestLocation = location
                            hotLocations.onNext(location)
                            e.onNext(location)
                        }
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