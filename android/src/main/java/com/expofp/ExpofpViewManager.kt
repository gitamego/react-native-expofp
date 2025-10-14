package com.expofp

import android.app.Application
import com.expofp.fplan.api.app.ExpoFpPlan
import com.expofp.fplan.api.app.model.ExpoFpLinkType
import com.expofp.fplan.api.locationProvider.IExpoFpLocationProvider
import com.expofp.fplan.ui.ExpoFpView
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.expofp.crowdconnected.ExpoFpCrowdConnectedLocationProvider
// import com.expofp.crowdconnectedbackground.ExpoFpCrowdConnectedBackgroundLocationProvider
import com.expofp.crowdconnected.ExpoFpCrowdConnectedLocationProviderSettings
import com.expofp.crowdconnected.ExpoFpCrowdConnectedNavigationType

class ExpofpViewManager : SimpleViewManager<ExpoFpView>() {
    private var reactContext: ThemedReactContext? = null

    override fun getName() = "ExpofpView"

    override fun createViewInstance(reactContext: ThemedReactContext): ExpoFpView {
        this.reactContext = reactContext
        ExpoFpPlan.initialize(reactContext.applicationContext)
        return ExpoFpView(reactContext)
    }

    override fun onDropViewInstance(view: ExpoFpView) {
        super.onDropViewInstance(view)
    }

    private fun getExpoKeyFromUrl(url: String): String {
        return url.substringAfter("https://").substringBefore(".expofp.com")
    }

    private fun createCrowdConnectedProvider(settingsMap: ReadableMap): IExpoFpLocationProvider? {
        val context = reactContext?.applicationContext ?: return null
        val application = (context as? Application) ?: return null

        val cc = if (settingsMap.hasKey("crowdConnected")) settingsMap.getMap("crowdConnected") else settingsMap
        if (cc == null) return null

        val appKey = if (cc.hasKey("appKey")) cc.getString("appKey") else null
        val token = if (cc.hasKey("token")) cc.getString("token") else null
        val secret = if (cc.hasKey("secret")) cc.getString("secret") else null
        if (appKey.isNullOrEmpty() || token.isNullOrEmpty() || secret.isNullOrEmpty()) return null
        val aliases = mutableMapOf<String, String>()
        aliases["onesignal_user_id"] = cc.getString("oneSignalUserId") ?: ""
        val settings = ExpoFpCrowdConnectedLocationProviderSettings(
            appKey = appKey,
            token = token,
            secret = secret,
            navigationType = ExpoFpCrowdConnectedNavigationType.ALL,
            isAllowedInBackground = false,
            isHeadingEnabled = true,
            aliases = aliases,
            notificationText = "Indoor navigation is active",
            serviceIcon = R.drawable.placeholder_icon
        )

        // ExpoFpCrowdConnectedBackgroundLocationProvider(application, settings)
        return ExpoFpCrowdConnectedLocationProvider(application, settings)
    }

    @ReactProp(name = "settings")
    fun setSettings(view: ExpoFpView, settingsMap: ReadableMap?) {
        if (settingsMap == null) return
        val url = settingsMap.getString("url") ?: return
        val context = reactContext?.applicationContext ?: return
        val expoKey = getExpoKeyFromUrl(url)

        ExpoFpPlan.initialize(context)
        val p = ExpoFpPlan.createPlanPresenter(planLink = ExpoFpLinkType.ExpoKey(expoKey))
        val ccProvider = createCrowdConnectedProvider(settingsMap)
        if (ccProvider != null) p.setLocationProvider(ccProvider)
        view.attachPresenter(p)
    }
}