package org.commonvoice.saverio.ui.login

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.isGone
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.BottomsheetLoginBinding
import org.commonvoice.saverio.databinding.FragmentLoginBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.OnSwipeTouchListener
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.background.ClipsDownloadWorker
import org.commonvoice.saverio_lib.background.SentencesDownloadWorker
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.viewmodels.LoginViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : ViewBoundFragment<FragmentLoginBinding>() {

    private val mainPrefManager by inject<MainPrefManager>()

    private val loginViewModel by viewModel<LoginViewModel>()

    private val workManager by inject<WorkManager>()

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWebBrowser()
    }

    override fun onStart() {
        super.onStart()

        activity?.setTitle(R.string.button_home_login)

        val bottomSheetBinding = BottomsheetLoginBinding.inflate(layoutInflater)

        val bottomSheet = BottomSheetDialog(requireContext()).also {
            it.setContentView(bottomSheetBinding.root)
        }

        if (mainPrefManager.areGesturesEnabled) {
            binding.layoutWebBrowser.setOnTouchListener(object :
                OnSwipeTouchListener(requireContext()) {
                override fun onSwipeRight() {
                    activity?.onBackPressed()
                }
            })
        }

        binding.btnAlreadyAVerificationLinkWebBrowser.onClick {
            bottomSheet.show()

            bottomSheetBinding.textURLVerificationLink.setText("")
            bottomSheetBinding.textURLVerificationLink.requestFocus()
        }

        bottomSheetBinding.textURLVerificationLink.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if ((bottomSheetBinding.textURLVerificationLink.text).contains("https://auth.mozilla.auth0.com/passwordless/verify_redirect?")) {
                    bottomSheet.dismiss()
                    showLoading()
                    binding.webViewBrowser.loadUrl(bottomSheetBinding.textURLVerificationLink.text.toString())
                    return@setOnKeyListener true
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.txt_verification_link_not_valid),
                        Toast.LENGTH_LONG
                    ).show()
                }
                return@setOnKeyListener false
            }
            false
        }

        navigateTo(arguments?.getString("loginUrl"))
        setTheme()
    }

    private fun showLoading() = try {
        withBinding {
            txtLoadingWebBrowser.isGone = false
            imgBackgroundWebBrowser.isGone = false
            imgRobotWebBrowser.isGone = false
            btnAlreadyAVerificationLinkWebBrowser.isGone = true
            stopAnimation(btnAlreadyAVerificationLinkWebBrowser)
            startAnimation(imgRobotWebBrowser, R.anim.login)
        }
    } catch (e: Exception) {

    }

    fun hideLoading(showButton: Boolean = false) = withBinding {
        txtLoadingWebBrowser.isGone = true
        imgBackgroundWebBrowser.isGone = true
        imgRobotWebBrowser.isGone = true
        stopAnimation(imgRobotWebBrowser)
        if (showButton) {
            btnAlreadyAVerificationLinkWebBrowser.isGone = false
            startAnimation(btnAlreadyAVerificationLinkWebBrowser, R.anim.zoom_in)
        }
    }

    private fun setupWebBrowser() = binding.webViewBrowser.apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.userAgentString = settings.userAgentString.replace("; wv", "")

        webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                showLoading()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                hideLoading(
                    showButton = (view?.url?.contains("https://auth.mozilla.auth0.com/login")
                        ?: false)
                )

                val cookies = CookieManager.getInstance().getCookie(url)

                if (url!!.contains("https://commonvoice.mozilla.org/")
                    && cookies != null && cookies.contains("connect.sid=")
                ) {
                    showLoading()

                    Log.wtf("Cookies", cookies)

                    val loginCookie = cookies.split("; ")
                        .find { it.contains("connect.sid=") }
                        ?.substring(12)

                    loginCookie?.let {
                        mainPrefManager.isLoggedIn = true

                        mainPrefManager.sessIdCookie = it

                        loginViewModel.clearDB()

                        SentencesDownloadWorker.attachOneTimeJobToWorkManager(workManager)
                        ClipsDownloadWorker.attachOneTimeJobToWorkManager(workManager)

                        if (arguments?.getString("loginUrl") != null) {
                            findNavController().popBackStack(R.id.profileFragment, false)
                        } else {
                            findNavController().navigateUp()
                        }
                    }
                } else {
                    Log.e("LoginFragment", "??-- I can't get cookie - Something was wrong --??")
                }
            }
        }
    }

    private fun navigateTo(newUrl: String?) = binding.webViewBrowser.apply {
        if ((newUrl != url && newUrl != null) || url == null) {
            Log.wtf("NewURL", newUrl ?: "https://commonvoice.mozilla.org/login")
            loadUrl(newUrl ?: "https://commonvoice.mozilla.org/login")
        }
    }

    private fun setTheme() {
        theme.setElement(
            requireContext(),
            binding.btnAlreadyAVerificationLinkWebBrowser
        )
    }


}