package com.expofp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.expofp.common.GlobalLocationProvider
import com.expofp.crowdconnected.CrowdConnectedProvider
import com.expofp.crowdconnected.Mode
import com.expofp.crowdconnected.Settings
import com.expofp.fplan.FplanView
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp


class ExpofpViewManager : SimpleViewManager<View>() {
    private var reactContext: ThemedReactContext? = null

    override fun getName() = "ExpofpView"

    override fun createViewInstance(reactContext: ThemedReactContext): View {
        this.reactContext = reactContext
        var view = FplanView(reactContext)

        return view;
    }

    @ReactProp(name = "settings")
    fun setSettings(view: FplanView, settingsMap: ReadableMap?) {
        val context = reactContext?.applicationContext ?: return
        val application = context as? Application ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val activity = reactContext?.currentActivity
            if (activity != null) {
                ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT),
                        2
                )
            }
        }
        settingsMap?.let {
            val lpSettings = com.expofp.crowdconnected.Settings(
                    settingsMap.getString("appKey") ?: "",
                    settingsMap.getString("token") ?: "",
                    settingsMap.getString("secret") ?: "",
                    Mode.IPS_AND_GPS
            )
            lpSettings.setServiceNotificationInfo("Knowledge - ServiceNow is running", R.drawable.common_google_signin_btn_icon_dark);
            lpSettings.setAlias("onesignal_user_id", it.getString("oneSignalUserId") ?: "");
            val locationProvider = CrowdConnectedProvider(application, lpSettings)
            GlobalLocationProvider.init(locationProvider)
            GlobalLocationProvider.start()
            view.init(it.getString("url") ?: "", com.expofp.fplan.models.Settings().withGlobalLocationProvider());
        }
    }
}