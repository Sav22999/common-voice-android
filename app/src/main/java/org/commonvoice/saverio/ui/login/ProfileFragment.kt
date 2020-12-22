package org.commonvoice.saverio.ui.login

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.CookieManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.FragmentProfileBinding
import org.commonvoice.saverio.ui.viewBinding.ViewBoundFragment
import org.commonvoice.saverio.utils.OnSwipeTouchListener
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.api.network.ConnectionManager
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.commonvoice.saverio_lib.utils.ImageDownloader
import org.commonvoice.saverio_lib.viewmodels.LoginViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : ViewBoundFragment<FragmentProfileBinding>() {

    private val loginViewModel by viewModel<LoginViewModel>()

    private val mainPrefManager by inject<MainPrefManager>()
    private val statsPrefManager by inject<StatsPrefManager>()

    private val connectionManager by inject<ConnectionManager>()

    override fun inflate(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(layoutInflater, container, false)
    }

    override fun onStart() {
        super.onStart()

        activity?.setTitle(R.string.button_home_profile)

        if (!connectionManager.isInternetAvailable) {
            findNavController().navigate(R.id.noConnectionFragment)
            return
        }

        if (mainPrefManager.sessIdCookie == null) {
            findNavController().navigate(R.id.loginFragment)
            return
        }

        if (mainPrefManager.areGesturesEnabled) {
            binding.nestedScrollLogin.setOnTouchListener(object :
                OnSwipeTouchListener(requireContext()) {
                override fun onSwipeRight() {
                    activity?.onBackPressed()
                }
            })
        }

        binding.btnLogout.onClick {
            logoutAndExit()
        }

        binding.btnBadges.onClick {
            findNavController().navigate(R.id.badgesFragment)
        }

        binding.labelToModifyInformation.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.labelToModifyInformation.onClick {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://commonvoice.mozilla.org/profile/info")
                )
            )
        }

        binding.textProfileEmail.setText("···")
        binding.textProfileAge.setText("···")
        binding.textProfileGender.setText("···")
        binding.textProfileUsername.setText("···")
        binding.btnBadges.isEnabled = false

        loginViewModel.getUserClient().observe(this, {
            it?.let {
                withBinding {
                    textProfileEmail.setText(it.email)
                    textProfileAge.setText(getAgeString(it.age))
                    textProfileGender.setText(getGenderString(it.gender))
                    textProfileUsername.setText(it.username)

                    mainPrefManager.username = it.username ?: ""

                    btnBadges.isEnabled = true

                    statsPrefManager.allTimeValidated = it.votes_count
                    statsPrefManager.allTimeRecorded = it.clips_count
                    statsPrefManager.allTimeLevel = it.votes_count + it.clips_count

                    lifecycleScope.launch {
                        ImageDownloader.loadImageIntoImageView(it.avatar_url, imageProfileImage)
                    }
                }
            }

            if (it == null) {
                findNavController().navigate(R.id.privacyPolicyFragment)
            }
        })

        requireActivity().actionBar?.setTitle(R.string.button_home_profile)

        setTheme()
    }

    private fun getAgeString(age: String?) = when (age) {
        "teens" -> "< 19"
        "twenties" -> "19-29"
        "thirties" -> "30-39"
        "fourties" -> "40-49"
        "fifties" -> "50-59"
        "sixties" -> "60-69"
        "seventies" -> "70-79"
        "eighties" -> "80-89"
        "nineties" -> "> 89"
        else -> "?"
    }

    private fun getGenderString(gender: String?) = when (gender) {
        "male" -> getString(R.string.txt_gender_male)
        "female" -> getString(R.string.txt_gender_female)
        "other" -> getString(R.string.txt_gender_other)
        else -> "?"
    }

    private fun setTheme() = withBinding {
        theme.setElement(layoutLogin)
        theme.setElement(requireContext(), 3, loginSectionData)
        theme.setElement(requireContext(), 3, loginSectionInformation)
        theme.setElement(requireContext(), 1, loginSectionLogout)
        theme.setElement(requireContext(), btnBadges)
        theme.setElement(requireContext(), btnLogout)
        theme.setElement(
            requireContext(),
            labelToModifyInformation,
            R.color.colorAlertMessage,
            R.color.colorAlertMessageDT
        )
        theme.setTextView(
            requireContext(),
            textProfileUsername
        )
        theme.setTextView(
            requireContext(),
            textProfileEmail
        )
        theme.setTextView(
            requireContext(),
            textProfileAge
        )
        theme.setTextView(
            requireContext(),
            textProfileGender
        )
        theme.setElement(requireContext(), labelProfileUsername)
        theme.setElement(requireContext(), labelProfileEmail)
        theme.setElement(requireContext(), labelProfileAge)
        theme.setElement(requireContext(), labelProfileGender)
        theme.setTextView(requireContext(), textLevel, border = false)
    }

    private fun logoutAndExit() {
        mainPrefManager.sessIdCookie = null
        mainPrefManager.isLoggedIn = false

        requireContext().getSharedPreferences("LOGGED", Context.MODE_PRIVATE).edit()
            .putBoolean("LOGGED", false).apply()
        requireContext().getSharedPreferences("DAILY_GOAL", Context.MODE_PRIVATE).edit()
            .putInt("DAILY_GOAL", 0).apply()

        statsPrefManager.allTimeLevel = 0
        statsPrefManager.allTimeRecorded = 0
        statsPrefManager.allTimeValidated = 0

        mainPrefManager.username = ""

        CookieManager.getInstance().flush()
        CookieManager.getInstance().removeAllCookies(null)
        loginViewModel.clearDB()

        startActivity(Intent(requireContext(), MainActivity::class.java))
    }

}