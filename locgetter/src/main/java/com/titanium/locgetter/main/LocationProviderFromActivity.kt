package com.titanium.locgetter.main

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.titanium.locgetter.exception.LocationSettingsException
import com.titanium.locgetter.exception.PermissionException
import io.reactivex.Observable
import io.reactivex.ObservableEmitter

/**
 * Multithread-safe implementation of getting user location in reactive way.
 * Copy of my library from https://github.com/titanium-codes/LocGetter but with use of rxjava 1 instead of rxjava 2
 */
class LocationProviderFromActivity(private val activity: FragmentActivity, mainLocationGetter: MainLocationGetter) : BaseLocationsProvider(mainLocationGetter) {

    override fun getLatestLocations() = (if (mainLocationGetter.isLocationPermitted()) Observable.just("") else askPermissions())
            .flatMap { checkSettings() }
            .flatMap { mainLocationGetter.locationShare }

    private fun checkSettings() =
            Observable.create<Any> { emitter ->
                mainLocationGetter.getLocationSettingsStatus({ resolvable -> askSettings(resolvable, emitter) }, emitter)
            }

    private fun askSettings(resolvableApiException: ResolvableApiException, subscriber: ObservableEmitter<Any>) {
        val onError = { if (!subscriber.isDisposed) subscriber.onError(LocationSettingsException()) }
        val askRequest: (Fragment) -> Unit = { f -> f.startIntentSenderForResult(resolvableApiException.resolution.intentSender, 1, null, 0, 0, 0, null) }
        val askResult: (Int, Intent?) -> Unit = { resultCode, _ ->
            when (resultCode) {
                RESULT_OK -> {
                    subscriber.onNext("")
                    subscriber.onComplete()
                }
                else -> onError()
            }
        }
        activity.connectFragment(askRequest, onActivityResult = askResult, onDeAttach = onError)
    }

    private fun askPermissions(): Observable<Any> {
        return Observable.create<Any> { emitter ->
            val permRequest: (Fragment) -> Unit = { f -> f.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1) }
            val onDeAttach: () -> Unit = { if (!emitter.isDisposed) emitter.onError(RuntimeException("timeout")) }
            val permResult: (Boolean) -> Unit = { isGranted ->
                if (isGranted) {
                    emitter.onNext("")
                    emitter.onComplete()
                } else {
                    emitter.onError(PermissionException(Manifest.permission.ACCESS_FINE_LOCATION))
                }
            }
            activity.connectFragment(permRequest, onRequestPermissionsResult = permResult, onDeAttach = onDeAttach)
        }
    }

}
