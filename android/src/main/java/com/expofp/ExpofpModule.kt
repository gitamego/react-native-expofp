package com.expofp

import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.expofp.fplan.SharedFplanView
import com.expofp.fplan.models.Settings
import com.facebook.react.bridge.UiThreadUtil

class ExpofpModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  override fun getName() = "ExpofpModule"

  @ReactMethod
  fun preload(url: String, promise: Promise) {
      try {
          val context = this.reactApplicationContext.applicationContext
          UiThreadUtil.runOnUiThread {
              SharedFplanView.preload(url, Settings(), context)
          }
          promise.resolve(null)
      } catch (e: Exception) {
          promise.reject("PRELOAD_ERROR", e.message)
      }
  }
}