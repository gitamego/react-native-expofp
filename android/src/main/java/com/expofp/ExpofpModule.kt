package com.expofp

import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.expofp.fplan.SharedFplanView

class ExpofpModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  override fun getName() = "ExpofpModule"

  @ReactMethod
  fun preload(url: String, promise: Promise) {
      try {
          val context = this.reactApplicationContext.applicationContext
          SharedFplanView.preload(url, com.expofp.fplan.models.Settings().withGlobalLocationProvider(), context)
          promise.resolve(null)
      } catch (e: Exception) {
          promise.reject("PRELOAD_ERROR", e.message)
      }
  }
}