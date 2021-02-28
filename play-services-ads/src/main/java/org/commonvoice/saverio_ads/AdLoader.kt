package org.commonvoice.saverio_ads

import android.os.Build
import android.util.DisplayMetrics
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView


object AdLoader {

    private val homeAdId =
        if (BuildConfig.DEBUG) R.string.admob_banner_ad_test else R.string.admob_banner_ad_home
    private val speakAdId =
        if (BuildConfig.DEBUG) R.string.admob_banner_ad_test else R.string.admob_banner_ad_speak
    private val listnAdId =
        if (BuildConfig.DEBUG) R.string.admob_banner_ad_test else R.string.admob_banner_ad_listen

    fun setupHomeAdView(activity: FragmentActivity, adContainer: FrameLayout) =
        setupGenericAdView(activity.getString(homeAdId), activity, adContainer)

    fun setupSpeakAdView(activity: FragmentActivity, adContainer: FrameLayout) =
        setupGenericAdView(activity.getString(speakAdId), activity, adContainer)

    fun setupListenAdView(activity: FragmentActivity, adContainer: FrameLayout) =
        setupGenericAdView(activity.getString(listnAdId), activity, adContainer)

    private fun setupGenericAdView(
        id: String,
        activity: FragmentActivity,
        adContainer: FrameLayout
    ) {
        val adView = AdView(activity)

        adView.adUnitId = id

        adContainer.addView(adView)

        val adRequest = AdRequest.Builder().build()
        val adSize = getSize(activity)

        adView.adSize = adSize

        adView.loadAd(adRequest)

        adContainer.requestLayout()
    }

    private fun getSize(activity: FragmentActivity): AdSize {
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.display
        } else {
            activity.windowManager.defaultDisplay
        }
        val outMetrics = DisplayMetrics()
        display?.getMetrics(outMetrics)

        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density

        val adWidth = (widthPixels / density).toInt()

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

}