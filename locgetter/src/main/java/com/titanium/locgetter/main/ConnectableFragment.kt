package com.titanium.locgetter.main

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

internal class ConnectableFragment : Fragment() {

    lateinit var onConnect: () -> Unit
    lateinit var onDisconnect: () -> Unit
    lateinit var onActivityResultWrapper: (Int, Intent?) -> Unit
    lateinit var onRequestPermissionsResultWrapper: (Boolean) -> Unit

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onConnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        onDisconnect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
            onActivityResultWrapper(resultCode, data)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) =
            onRequestPermissionsResultWrapper(grantResults[0] == PackageManager.PERMISSION_GRANTED)
}

fun FragmentActivity.connectFragment(onReady: (Fragment) -> Unit?,
                                     onActivityResult: (Int, Intent?) -> Unit = { _, _ -> },
                                     onRequestPermissionsResult: (Boolean) -> Unit = { },
                                     onDeAttach: () -> Unit = {}) {
    val connectableFragment = ConnectableFragment()
    connectableFragment.onConnect = { onReady(connectableFragment) }
    connectableFragment.onActivityResultWrapper = onActivityResult
    connectableFragment.onRequestPermissionsResultWrapper = onRequestPermissionsResult
    connectableFragment.onDisconnect = onDeAttach
    runOnUiThread { supportFragmentManager.beginTransaction().add(connectableFragment, "TAG").commit() }
}