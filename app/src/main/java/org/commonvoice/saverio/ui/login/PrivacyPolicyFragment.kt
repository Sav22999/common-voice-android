package org.commonvoice.saverio.ui.login

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.CookieManager
import androidx.navigation.fragment.findNavController
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentYouHaveToAcceptPrivacyPolicyBinding
import org.commonvoice.saverio.ui.dialogs.DialogInflater
import org.commonvoice.saverio.ui.dialogs.commonTypes.StandardDialog
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.LoginViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PrivacyPolicyFragment : ViewBoundFragment<FragmentYouHaveToAcceptPrivacyPolicyBinding>() {

    private val loginViewModel by viewModel<LoginViewModel>()

    private val mainPrefManager by inject<MainPrefManager>()
    private val statsPrefManager by inject<StatsPrefManager>()
    private val connectionManager by inject<ConnectionManager>()
    private val dialogInflater by inject<DialogInflater>()

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentYouHaveToAcceptPrivacyPolicyBinding {
        return FragmentYouHaveToAcceptPrivacyPolicyBinding
            .inflate(layoutInflater, container, false)
    }

    override fun onStart() {
        super.onStart()

        dialogInflater.show(requireContext(), StandardDialog(
            titleRes = R.string.youHaveToAcceptPrivacyPolicyTitle,
            messageRes = R.string.youHaveToAcceptPrivacyPolicy
        )
        )

        binding.btnCloseLoginPrivacyPolicy.setOnClickListener {
            logoutAndExit()
        }
        binding.btnOpenPrivacyPolicy.onClick {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://commonvoice.mozilla.org/")))
        }

        connectionManager.liveInternetAvailability.observe(viewLifecycleOwner) {
            if (!it) {
                findNavController().navigateUp()
                findNavController().navigate(R.id.noConnectionFragment)
            }
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
        statsPrefManager.localValidated = 0
        statsPrefManager.localRecorded = 0
        statsPrefManager.localLevel = 0

        CookieManager.getInstance().flush()
        CookieManager.getInstance().removeAllCookies(null)
        loginViewModel.clearDB()

        startActivity(Intent(requireContext(), MainActivity::class.java))
    }

}