package com.expofp

import android.app.Application
import android.graphics.Color
import android.telecom.Call.Details
import android.view.View
import com.expofp.fplan.FplanEventsListener
import com.expofp.fplan.FplanView
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import okhttp3.Route
import android.util.Log
import com.facebook.react.bridge.ReadableMap
import android.widget.Toast
import com.expofp.common.GlobalLocationProvider
import com.expofp.crowdconnected.Settings
import com.expofp.crowdconnected.CrowdConnectedProvider
import com.expofp.crowdconnected.Mode

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