package com.expofp

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise

class ExpofpModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  override fun getName() = "ExpofpModule"

  @ReactMethod
  fun preload(url: String, promise: Promise) {
      Log.d("ExpofpModule", "preload: not implemented")
  }
}