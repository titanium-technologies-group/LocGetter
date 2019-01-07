package com.titanium.locgetter.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.support.v4.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.titanium.locgetter.exception.LocationSettingsException
import com.titanium.locgetter.exception.PermissionException
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject


class MainLocationGetter constructor(private val appContext: Context,
                                     private val logger: ((String, String) -> Unit)) : LocationGetter {

    override val hotLocations: Subject<Location> = PublishSubject.create()
    private var latestLocation: Location? = null
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

    override fun getLatestSavedLocation() = latestLocation

    override fun getLatestLocations() = (if (isLocationPermitted()) Observable.just("") else askPermissions())
            .flatMap { checkSettings() }
            .flatMap { locationShare }

    private fun checkSettings() = if (isLocationEnabled())
        Observable.just(1)
    else
        askSettings()

    private fun askSettings() = Observable.create<Any> { emitter ->
        val onError = { if (!emitter.isDisposed) emitter.onError(LocationSettingsException()) }
        val askRequest: (Activity) -> Unit = { f -> f.startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1) }
        val askResult: (Int, Intent?) -> Unit = { _, _ ->
            if (isLocationEnabled()) {
                emitter.onNext("")
                emitter.onComplete()
            } else {
                onError()
            }
        }
        appContext.launchConnectableActivity(askRequest, onActivityResult = askResult, onDeAttach = onError)
    }


    @SuppressLint("NewApi")
    private fun askPermissions(): Observable<Any> {
        return Observable.create<Any> { emitter ->
            val permRequest: (Activity) -> Unit = { f -> f.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1) }
            val onDeAttach: () -> Unit = { if (!emitter.isDisposed) emitter.onError(RuntimeException("timeout")) }
            val permResult: (Boolean) -> Unit = { isGranted ->
                if (isGranted) {
                    emitter.onNext("")
                    emitter.onComplete()
                } else {
                    emitter.onError(PermissionException(Manifest.permission.ACCESS_FINE_LOCATION))
                }
            }
            appContext.launchConnectableActivity(permRequest, onRequestPermissionsResult = permResult, onDeAttach = onDeAttach)
        }
    }

    internal fun isLocationPermitted() = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    internal fun isLocationEnabled(): Boolean {
        val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
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
                        latestLocation = location
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