package com.expofp

import android.graphics.Color
import android.telecom.Call.Details
import android.view.View
import com.expofp.fplan.FplanEventsListener
import com.expofp.fplan.FplanView
import com.expofp.fplan.Settings
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import okhttp3.Route




class ExpofpViewManager : SimpleViewManager<View>() {
  override fun getName() = "ExpofpView"

  override fun createViewInstance(reactContext: ThemedReactContext): View {
    var settings: Settings = Settings();

    var view = FplanView(reactContext)
    view.init("https://demo.expofp.com", settings);
    return view;
  }
  @ReactProp(name = "url")
  fun setUrl(view: FplanView, string: String?) {
    view.init(string)
  }

  @ReactProp(name = "color")
  fun setColor(view: View, color: String) {
    view.setBackgroundColor(Color.parseColor(color))
  }
}
