package org.commonvoice.saverio_ads

import android.content.Context
import androidx.startup.Initializer
import com.google.android.gms.ads.MobileAds

class AdsInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        MobileAds.initialize(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }

}