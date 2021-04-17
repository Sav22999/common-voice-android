package org.commonvoice.saverio.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.commonvoice.saverio.*
import org.commonvoice.saverio.databinding.FragmentHomeBinding
import org.commonvoice.saverio.ui.dialogs.DialogInflater
import org.commonvoice.saverio.ui.dialogs.commonTypes.StandardDialog
import org.commonvoice.saverio.ui.dialogs.messageDialogs.MessageWarningDialog
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_ads.AdLoader
import org.commonvoice.saverio_lib.background.AppUsageUploadWorker
import org.commonvoice.saverio_lib.preferences.FirstRunPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.viewmodels.HomeViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : ViewBoundFragment<FragmentHomeBinding>() {

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater, container, false)
    }

    private val homeViewModel: HomeViewModel by viewModel()

    private val firstRunPrefManager by inject<FirstRunPrefManager>()
    private val statsPrefManager by inject<StatsPrefManager>()
    private val mainPrefManager: MainPrefManager by inject()
    private val settingsPrefManager: SettingsPrefManager by inject()
    private val workManager: WorkManager by inject()
    private val dialogInflater by inject<DialogInflater>()

    override fun onStart() {
        super.onStart()

        //TODO fix this mess once MainActivity is fixed

        (activity as MainActivity).resetStatusBarColor()

        if (mainPrefManager.sessIdCookie != null) {
            val textLoggedIn = binding.textLoggedUsername
            textLoggedIn.isGone = false
            textLoggedIn.isVisible = true
            textLoggedIn.text = if (mainPrefManager.username == "") {
                "${getString(R.string.text_hi_username)}!"
            } else {
                "${getString(R.string.text_hi_username)}, ${mainPrefManager.username}!"
            }

            binding.buttonHomeLogin.setText(R.string.button_home_profile)
        }

        binding.buttonHomeLogin.onClick {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        binding.buttonSpeak.onClick {
            if (firstRunPrefManager.speak) {
                Intent(requireContext(), FirstRunSpeak::class.java).also {
                    startActivity(it)
                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.RECORD_AUDIO
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        MainActivity.RECORD_REQUEST_CODE
                    )
                } else {
                    Intent(requireContext(), SpeakActivity::class.java).also {
                        startActivity(it)
                    }
                }
            }
        }

        binding.buttonListen.onClick {
            if (firstRunPrefManager.listen) {
                Intent(requireContext(), FirstRunListen::class.java).also {
                    startActivity(it)
                }
            } else {
                Intent(requireContext(), ListenActivity::class.java).also {
                    startActivity(it)
                }
            }
        }

        if (settingsPrefManager.automaticallyCheckForUpdates) {
            homeViewModel.checkForNewVersion(BuildConfig.VERSION_NAME).observe(viewLifecycleOwner) {
                if (statsPrefManager.reviewOnPlayStoreCounter >= 1) {
                    dialogInflater.show(
                        requireContext(), StandardDialog(
                            message = getString(R.string.message_dialog_new_version_available).replace(
                                "{{*{{n_version}}*}}",
                                it
                            )
                        )
                    )
                }
            }
        }

        if (mainPrefManager.showAdBanner) {
            AdLoader.setupHomeAdView(requireActivity(), binding.adContainer)
        }

        setTheme(requireContext())

        setupBannerMessage()
        showDialogMessages()

        startAnimation(binding.buttonSpeak, R.anim.zoom_out)
        startAnimation(binding.buttonListen, R.anim.zoom_out)
    }

    override fun onPause() {
        AdLoader.cleanupLayout(binding.adContainer)

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        homeViewModel.postStats(
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE,
            MainActivity.SOURCE_STORE
        )

        lifecycleScope.launch {
            delay(1500)
            homeViewModel.postFileLog(
                BuildConfig.VERSION_CODE,
                MainActivity.SOURCE_STORE
            )
        }

        AppUsageUploadWorker.attachToWorkManager(workManager)
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)

        if (mainPrefManager.showAdBanner) {
            AdLoader.setupHomeAdView(requireActivity(), binding.adContainer)
        }
    }

    private fun setupBannerMessage() {
        homeViewModel.getLastBannerMessage().observe(this) { msg ->
            activity?.window?.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.colorMessageBanner)
            binding.homeMessageBoxBannerContainer.isVisible = true
            binding.textHomeMessageBoxBanner.text = msg.text
            binding.hideMessageBanner.isVisible = msg.canBeClosed ?: true
            binding.hideMessageBanner.onClick {
                homeViewModel.markMessageAsSeen(msg)
                binding.homeMessageBoxBannerContainer.isVisible = false
                activity?.window?.statusBarColor =
                    ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
                setupBannerMessage()
            }

            binding.button1HomeMessageBoxBanner.isVisible = msg.button1Text != null
            binding.button2HomeMessageBoxBanner.isVisible = msg.button2Text != null

            msg.button1Text?.let { binding.button1HomeMessageBoxBanner.text = it }
            msg.button2Text?.let { binding.button2HomeMessageBoxBanner.text = it }
            msg.button1Link?.let { link ->
                binding.button1HomeMessageBoxBanner.onClick {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                }
            }
            msg.button2Link?.let { link ->
                binding.button2HomeMessageBoxBanner.onClick {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                }
            }
        }
    }

    private fun showDialogMessages() {
        homeViewModel.getOtherMessages().observe(this) {
            it.forEach { message ->
                dialogInflater.show(
                    requireContext(),
                    MessageWarningDialog(requireContext(), message)
                )
                homeViewModel.markMessageAsSeen(message)
            }
        }
    }

    fun setTheme(view: Context) = withBinding {
        theme.setElement(view, 3, homeSectionCVAndroid)
        theme.setElement(view, 3, homeSectionLoginSignup)
        theme.setElement(
            view,
            textCommonVoiceAndroid,
            background = false,
            textSize = 30F
        )
        theme.setElement(
            view,
            textLoggedUsername,
            background = false,
            textSize = 22F
        )
        theme.setElement(view, buttonHomeLogin)
        theme.setElement(layoutHome)

        theme.setElement(
            view,
            textHomeMessageBoxBanner,
            background = false,
            textSize = 22F
        )

        theme.setElement(view, textHomeMessageBoxBanner, R.color.colorWhite, R.color.colorWhite)
    }

}