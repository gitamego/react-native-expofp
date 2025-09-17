package com.expofp

import android.app.Application
import android.util.Log
import android.view.View
import com.expofp.common.GlobalLocationProvider
import com.expofp.crowdconnected.CrowdConnectedProvider
import com.expofp.crowdconnected.Mode
import com.expofp.fplan.FplanView
import com.expofp.fplan.models.FplanViewState
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.expofp.R
import com.expofp.fplan.contracts.DownloadOfflinePlanCallback
import com.expofp.fplan.models.OfflinePlanInfo
import android.content.res.AssetManager

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
                lpSettings.setServiceNotificationInfo("Background Location is running", R.drawable.placeholder_icon);

                val locationProvider = CrowdConnectedProvider(application, lpSettings)
                // val locationProvider = CrowdConnectedBackgroundProvider(application, lpSettings)
                GlobalLocationProvider.init(locationProvider)
                GlobalLocationProvider.start()
            }
            if (view.state.equals(FplanViewState.Created)) {
                val info = ExpofpModule.downloadedOfflinePlanInfo
                val url = it.getString("url") ?: ""
                val expoKey = url.substringAfter("https://").substringBefore(".expofp.com")

                val offlinePlanManager = FplanView.getOfflinePlanManager(reactContext)
                val latestOfflinePlan = offlinePlanManager.allOfflinePlansFromCache
                    .filter { offlinePlanInfo -> offlinePlanInfo.expoKey == expoKey }
                    .maxByOrNull { offlinePlanInfo -> offlinePlanInfo.version }
                if (latestOfflinePlan != null) {
                    Log.d("ExpofpModule", latestOfflinePlan.expoKey)
                    view.openOfflinePlan(latestOfflinePlan, "", com.expofp.fplan.models.Settings().withGlobalLocationProvider())
                } else {
                    val ctx = this.reactContext
                    if (ctx != null) {
                        val am = ctx.assets
                        val cachePlanExists = try {
                            am.open("${expoKey}.zip").close()
                            true
                        } catch (e: Exception) {
                            false
                        }

                        if (cachePlanExists) {
                            try {
                                Log.d("ExpofpModule", "openZipFromAssets: ${'$'}candidate")
                                view.openZipFromAssets("${expoKey}.zip", "", com.expofp.fplan.models.Settings().withGlobalLocationProvider(), ctx)
                            } catch (e: Exception) {
                                Log.d("ExpofpModule", "failed to open asset zip, loading url: ${'$'}url")
                                view.load(url, com.expofp.fplan.models.Settings().withGlobalLocationProvider())
                            }
                        } else {
                            Log.d("ExpofpModule", "asset zip not found, loading url: ${'$'}url")
                            view.load(url, com.expofp.fplan.models.Settings().withGlobalLocationProvider())
                        }
                    } else {
                        view.load(url, com.expofp.fplan.models.Settings().withGlobalLocationProvider())
                    }
                }

                offlinePlanManager.downloadOfflinePlanToCache(expoKey, object : DownloadOfflinePlanCallback {
                    override fun onCompleted(offlinePlanInfo: OfflinePlanInfo) {
                        Log.d("ExpofpModule", "downloaded offline plan: ${'$'}{offlinePlanInfo.expoKey} v${'$'}{offlinePlanInfo.version}")
                    }

                    override fun onError(message: String) {
                        Log.e("ExpofpModule", "offline plan download failed: ${'$'}message")
                    }
                })
            }
        }
    }
}