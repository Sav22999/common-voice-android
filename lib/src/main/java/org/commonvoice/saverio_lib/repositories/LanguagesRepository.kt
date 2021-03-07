package org.commonvoice.saverio_lib.repositories

import com.squareup.moshi.Moshi
import org.commonvoice.saverio_lib.api.RetrofitFactory
import org.commonvoice.saverio_lib.api.responseBodies.ResponseLanguage
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager

class LanguagesRepository(
    private val settingsPrefManager: SettingsPrefManager,
    retrofitFactory: RetrofitFactory,
) {

    private val languagesClient = retrofitFactory.makeLanguagesService()

    private val moshiAdapter = Moshi.Builder().build().adapter(ResponseLanguage::class.java)

    var localLanguages: ResponseLanguage
        get() = moshiAdapter.fromJson(settingsPrefManager.appLanguages) ?: ResponseLanguage.DEFAULT_INSTANCE
        set(value) { settingsPrefManager.appLanguages = moshiAdapter.toJson(value) }

    suspend fun getServerAvailableLanguages(): ResponseLanguage? {
        return languagesClient.getAvailableLanguages().body()
    }

}