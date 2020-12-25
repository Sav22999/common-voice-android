package org.commonvoice.saverio.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.DisplayMetrics
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
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.background.AppUsageUploadWorker
import org.commonvoice.saverio_lib.preferences.FirstRunPrefManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
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
    private val mainPrefManager: MainPrefManager by inject()
    private val workManager: WorkManager by inject()

    override fun onStart() {
        super.onStart()

        //TODO fix this mess once MainActivity is fixed

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

        homeViewModel.checkForNewVersion(BuildConfig.VERSION_NAME).observe(viewLifecycleOwner) {
            showMessageDialog(
                "",
                getString(R.string.message_dialog_new_version_available).replace(
                    "{{*{{n_version}}*}}",
                    it
                )
            )
        }


        setTheme(requireContext())

        startAnimation(binding.buttonSpeak, R.anim.zoom_out)
        startAnimation(binding.buttonListen, R.anim.zoom_out)
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

    private fun showMessageDialog(
        title: String,
        text: String,
        errorCode: String = "",
        details: String = "",
        type: Int = 0
    ) {
        val metrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
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
            var message: MessageDialog? = null
            message = MessageDialog(
                requireContext(),
                type,
                title,
                messageText,
                details = details,
                height = height
            )
            message.show()
        } catch (exception: Exception) {
            println("!!-- Exception: MainActivity - MESSAGE DIALOG: " + exception.toString() + " --!!")
        }
    }


    fun setTheme(view: Context) = withBinding {
        theme.setElement(view, 3, homeSectionCVAndroid)
        theme.setElement(view, 3, homeSectionLoginSignup)
        theme.setElement(
            view,
            textCommonVoiceAndroid,
            background = false
        )
        theme.setElement(
            view,
            textLoggedUsername,
            background = false
        )
        theme.setElement(view, buttonHomeLogin)
        theme.setElement(layoutHome)
    }

}