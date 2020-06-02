package org.commonvoice.saverio.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import org.commonvoice.saverio.BuildConfig
import org.commonvoice.saverio.DarkLightTheme
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R
import org.commonvoice.saverio_lib.viewmodels.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)*/
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val main = activity as MainActivity
        main.dashboard_selected = false

        val btnSpeak: Button = root.findViewById(R.id.buttonSpeak)
        val btnListen: Button = root.findViewById(R.id.buttonListen)
        val btnLogin: Button = root.findViewById(R.id.buttonHomeLogin)

        btnSpeak.setOnClickListener {
            main.openSpeakSection()
        }

        btnListen.setOnClickListener {
            main.openListenSection()
        }

        main.checkUserLoggedIn()

        if (main.logged) {
            //login successful -> show username and profile button

            val textLoggedIn: TextView = root.findViewById(R.id.textLoggedUsername)
            textLoggedIn.isGone = false
            textLoggedIn.isVisible = true
            textLoggedIn.text = main.getHiUsernameLoggedIn()
            val btnLogOut: Button = root.findViewById(R.id.buttonHomeLogin)
            btnLogOut.text = getString(R.string.button_home_profile)

            btnLogin.setOnClickListener {
                main.openProfileSection()
                main.checkUserLoggedIn()
            }
        } else {
            btnLogin.setOnClickListener {
                main.openLoginSection()
                main.checkUserLoggedIn()
            }
        }

        main.checkConnection()

        setTheme(main, root)

        main.startAnimation(btnSpeak)
        main.startAnimation(btnListen)

        main.checkNewVersionAvailable()

        main.reviewOnPlayStore()

        return root
    }

    override fun onResume() {
        super.onResume()

        homeViewModel.postStats(BuildConfig.VERSION_NAME, MainActivity.SOURCE_STORE)
    }

    fun setTheme(view: Context, root: View) {
        val theme = DarkLightTheme()
        //theme.setElements(view, root.findViewById(R.id.layoutHome))

        val isDark = theme.getTheme(view)
        theme.setElement(isDark, view, 2, root.findViewById(R.id.homeSectionCVAndroid))
        theme.setElement(isDark, view, 3, root.findViewById(R.id.homeSectionLoginSignup))
        theme.setElement(isDark, view, root.findViewById(R.id.textCommonVoiceAndroid) as TextView, background = false)
        theme.setElement(isDark, view, root.findViewById(R.id.textLoggedUsername) as TextView, background = false)
        theme.setElement(isDark, view, root.findViewById(R.id.buttonHomeLogin) as Button)
        theme.setElement(isDark, root.findViewById(R.id.layoutHome) as ConstraintLayout)
    }
}