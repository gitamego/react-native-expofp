package com.expofp

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.expofp.fplan.api.app.ExpoFpPlan
import com.expofp.fplan.api.app.model.ExpoFpLinkType
import com.expofp.fplan.api.preloader.ExpoFpPreloadedPlanInfo
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import java.util.concurrent.ConcurrentHashMap

object ExpofpUrlUtils {
  fun getExpoKeyFromUrl(url: String): String? {
    val host = try {
      Uri.parse(url).host
    } catch (_: Throwable) {
      null
    } ?: return null

    // Typical: https://sko26.expofp.com/...
    return host.substringBefore(".expofp.com").takeIf { it.isNotBlank() }
  }

  /**
   * ExpoFP "deep link" slug in query, e.g.:
   * https://sko26.expofp.com/?mandalay-bay-ballroom-f-level-2
   *
   * The Android SDK represents this as a value-less query item, which maps to SearchText
   * and becomes "?<slug>" when rendered by the SDK.
   */
  fun extractAdditionalParamsFromUrl(url: String): List<com.expofp.fplan.api.app.model.ExpoFpPlanParameter> {
    val query = try {
      Uri.parse(url).encodedQuery
    } catch (_: Throwable) {
      null
    } ?: return emptyList()

    if (query.isBlank()) return emptyList()

    // Keep it minimal and safe: only support the ExpoFP "?<slug>" style deep-link.
    if (!query.contains("=") && !query.contains("&")) {
      return listOf(com.expofp.fplan.api.app.model.ExpoFpPlanParameter.SearchText(Uri.decode(query)))
    }

    return emptyList()
  }
}

object ExpofpPreloadCache {
  private val byExpoKey = ConcurrentHashMap<String, ExpoFpPreloadedPlanInfo>()

  fun put(expoKey: String, info: ExpoFpPreloadedPlanInfo) {
    byExpoKey[expoKey] = info
  }

  fun get(expoKey: String): ExpoFpPreloadedPlanInfo? = byExpoKey[expoKey]

  fun clear() {
    byExpoKey.clear()
  }
}

class ExpofpModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  override fun getName() = "ExpofpModule"

  @ReactMethod
  fun preload(url: String, promise: Promise) {
      Log.d("ExpofpModule", "preload: $url")
      Log.d("ExpofpModule", "reactApplicationContext: $reactApplicationContext")
      val context = reactApplicationContext.applicationContext
      Log.d("ExpofpModule", "context: $context")
      val expoKey = ExpofpUrlUtils.getExpoKeyFromUrl(url)
      Log.d("ExpofpModule", "expoKey: $expoKey")
      if (expoKey.isNullOrBlank()) {
        promise.reject("PRELOAD_INVALID_URL", "Invalid ExpoFP URL: missing host/expoKey")
        return
      }

      val additionalParams = ExpofpUrlUtils.extractAdditionalParamsFromUrl(url)

      // ExpoFP preload creates a WebView internally, so it must run on a thread with a Looper
      // (in practice: run on the main/UI thread).
      Handler(Looper.getMainLooper()).post {
        try {
          ExpoFpPlan.initialize(context)

          val preloadedPlanInfo = ExpoFpPlan.preloader.preloadPlan(
            ExpoFpLinkType.ExpoKey(expoKey),
            additionalParams,
            null,
            null,
            false
          )
          Log.d("ExpofpModule", "preloadedPlanInfo: $preloadedPlanInfo")
          ExpofpPreloadCache.put(expoKey, preloadedPlanInfo)

          val result = Arguments.createMap().apply {
            putString("expoKey", expoKey)
            putString("url", url)
          }
          promise.resolve(result)
        } catch (t: Throwable) {
          Log.e("ExpofpModule", "preload failed", t)
          promise.reject("PRELOAD_FAILED", t.message, t)
        }
      }
  }
}