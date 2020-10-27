package org.commonvoice.saverio.ui.login

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.CookieManager
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.MessageDialog
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentYouHaveToAcceptPrivacyPolicyBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.LoginViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PrivacyPolicyFragment : ViewBoundFragment<FragmentYouHaveToAcceptPrivacyPolicyBinding>() {

    private val loginViewModel by viewModel<LoginViewModel>()

    private val mainPrefManager by inject<MainPrefManager>()
    private val statsPrefManager by inject<StatsPrefManager>()

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentYouHaveToAcceptPrivacyPolicyBinding {
        return FragmentYouHaveToAcceptPrivacyPolicyBinding.inflate(layoutInflater, container, false)
    }

    override fun onStart() {
        super.onStart()

        showMessageDialog(
            getString(R.string.youHaveToAcceptPrivacyPolicyTitle),
            getString(R.string.youHaveToAcceptPrivacyPolicy)
        )

        binding.btnCloseLoginPrivacyPolicy.setOnClickListener {
            logoutAndExit()
        }
        binding.btnOpenPrivacyPolicy.onClick {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://commonvoice.mozilla.org/")))
        }
    }

    private fun showMessageDialog(
        title: String,
        text: String,
        errorCode: String = "",
        details: String = ""
    ) {
        val metrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
        //val width = metrics.widthPixels
        val height = metrics.heightPixels
        try {
            var messageText = text
            if (errorCode != "") {
                if (messageText.contains("{{*{{error_code}}*}}")) {
                    messageText = messageText.replace("{{*{{error_code}}*}}", errorCode)
                } else {
                    messageText = messageText + "\n\n[Message Code: EX-" + errorCode + "]"
                }
            }
            val message = MessageDialog(requireContext(), 0, title, messageText, details = details, height = height)
            message.show()
        } catch (exception: Exception) {

        }
    }

    private fun logoutAndExit() {
        mainPrefManager.sessIdCookie = null

        requireContext().getSharedPreferences("LOGGED", Context.MODE_PRIVATE).edit().putBoolean("LOGGED", false).apply()
        requireContext().getSharedPreferences("USERNAME", Context.MODE_PRIVATE).edit().putString("USERNAME", "").apply()
        requireContext().getSharedPreferences("DAILY_GOAL", Context.MODE_PRIVATE).edit().putInt("DAILY_GOAL", 0).apply()

        statsPrefManager.allTimeLevel = 0
        statsPrefManager.allTimeRecorded = 0
        statsPrefManager.allTimeValidated = 0

        CookieManager.getInstance().flush()
        CookieManager.getInstance().removeAllCookies(null)
        loginViewModel.clearDB()

        startActivity(Intent(requireContext(), MainActivity::class.java))
    }

}