package com.expofp

import android.Manifest
import android.app.Activity
import android.app.Application
import android.os.Build
import android.view.View
import android.widget.Toast
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

  @ReactProp(name = "url")
  fun setUrl(view: FplanView, string: String?) {
    view.init(string);
  }

  @ReactProp(name = "crowdConnectedSettings")
  fun setCrowdConnectedSettings(view: FplanView, settingsMap: ReadableMap?) {
      val context = reactContext?.applicationContext ?: return
      val application = context as? Application ?: return
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val activity = reactContext?.currentActivity
          if (activity != null) {
              ActivityCompat.requestPermissions(
                      activity,
                      arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT),
                      0
              )
          }
      }
      settingsMap?.let {
          val lpSettings = Settings(
              it.getString("appKey") ?: "",
              it.getString("token") ?: "",
              it.getString("secret") ?: "",
              Mode.IPS_AND_GPS
          )
          val locationProvider = CrowdConnectedProvider(application, lpSettings)
          GlobalLocationProvider.init(locationProvider)
          GlobalLocationProvider.start()
      }
  }
}