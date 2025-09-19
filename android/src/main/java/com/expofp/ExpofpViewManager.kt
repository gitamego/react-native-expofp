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

    private fun getExpoKeyFromUrl(url: String): String {
        return url.substringAfter("https://").substringBefore(".expofp.com")
    }

    private fun openMapForUrl(view: FplanView, url: String) {
        val expoKey = getExpoKeyFromUrl(url)
        val settings = com.expofp.fplan.models.Settings().withGlobalLocationProvider()

        val offlinePlanManager = FplanView.getOfflinePlanManager(reactContext)
        val latestOfflinePlan = offlinePlanManager.allOfflinePlansFromCache
                .filter { offlinePlanInfo -> offlinePlanInfo.expoKey == expoKey }
                .maxByOrNull { offlinePlanInfo -> offlinePlanInfo.version }

        if (latestOfflinePlan != null) {
            Log.d("ExpofpModule", latestOfflinePlan.expoKey)
            view.openOfflinePlan(latestOfflinePlan, "", settings)
            return
        }

        val ctx = this.reactContext ?: run {
            view.load(url, settings)
            return
        }

        val am = ctx.assets
        val cachePlanExists = try {
            am.open("${expoKey}.zip").close()
            true
        } catch (e: Exception) {
            false
        }

        if (cachePlanExists) {
            try {
                Log.d("ExpofpModule", "openZipFromAssets: ${expoKey}.zip")
                view.openZipFromAssets("${expoKey}.zip", "", settings, ctx)
                return
            } catch (e: Exception) {
                Log.d("ExpofpModule", "failed to open asset zip, loading url: $url")
                view.load(url, settings)
                return
            }
        }

        Log.d("ExpofpModule", "asset zip not found, loading url: $url")
        view.load(url, settings)
    }

    private fun triggerOfflinePlanDownload(expoKey: String) {
        val offlinePlanManager = FplanView.getOfflinePlanManager(reactContext)
        offlinePlanManager.downloadOfflinePlanToCache(expoKey, object : DownloadOfflinePlanCallback {
            override fun onCompleted(offlinePlanInfo: OfflinePlanInfo) {
                Log.d("ExpofpModule", "downloaded offline plan: ${offlinePlanInfo.expoKey} v${offlinePlanInfo.version}")
            }

            override fun onError(message: String) {
                Log.e("ExpofpModule", "offline plan download failed: $message")
            }
        })
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
                val url = it.getString("url") ?: ""
                val expoKey = getExpoKeyFromUrl(url)

                openMapForUrl(view, url)
                triggerOfflinePlanDownload(expoKey)
            }
        }
    }
}