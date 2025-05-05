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
// import com.expofp.crowdconnectedbackground.CrowdConnectedBackgroundProvider
import com.expofp.fplan.FplanView
import com.expofp.fplan.models.FplanViewState
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

    override fun onDropViewInstance(view: View) {
        (view as? FplanView)?.destroy()
        super.onDropViewInstance(view)
    }

    @ReactProp(name = "settings")
    fun setSettings(view: FplanView, settingsMap: ReadableMap?) {
        println("setSettings: $settingsMap")
        settingsMap?.let {
            var appKey = settingsMap.getString("appKey")
            val token = settingsMap.getString("token")
            val secret = settingsMap.getString("secret")
            if (appKey != null && token != null && secret != null) {
                val context = reactContext?.applicationContext ?: return
                val application = context as? Application ?: return
                val aliases = mutableMapOf<String, String>()
                aliases["onesignal_user_id"] = it.getString("oneSignalUserId") ?: ""
                val lpSettings = com.expofp.crowdconnected.Settings(
                        settingsMap.getString("appKey") ?: "",
                        settingsMap.getString("token") ?: "",
                        settingsMap.getString("secret") ?: "",
                        Mode.IPS_AND_GPS,
                        true,
                        aliases
                )
                lpSettings.setServiceNotificationInfo("Background Location is running", R.drawable.common_google_signin_btn_icon_dark);
                val locationProvider = CrowdConnectedProvider(application, lpSettings)
                // val locationProvider = CrowdConnectedBackgroundProvider(application, lpSettings)
                GlobalLocationProvider.init(locationProvider)
                GlobalLocationProvider.start()
            }
            if (view.state.equals(FplanViewState.Created)) {
                view.load(it.getString("url") ?: "", com.expofp.fplan.models.Settings().withGlobalLocationProvider());
            }
        }
    }
}