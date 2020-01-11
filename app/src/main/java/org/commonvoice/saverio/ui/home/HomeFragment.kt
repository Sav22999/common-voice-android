package org.commonvoice.saverio.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val main = activity as MainActivity
        main.dashboard_selected = false

        val btnSpeak: Button = root.findViewById(R.id.btn_speak)
        val btnListen: Button = root.findViewById(R.id.btn_listen)
        val btnLogin: Button = root.findViewById(R.id.btn_login)

        btnSpeak.setOnClickListener {
            main.openSpeakSection()
        }

        btnListen.setOnClickListener {
            main.openListenSection()
        }

        if (main.logged) {
            //login successful -> show username and log-out button

            var textLoggedIn: TextView = root.findViewById(R.id.textLoggedUsername)
            textLoggedIn.isGone = false
            textLoggedIn.isVisible = true
            textLoggedIn.text = main.getHiUsernameLoggedIn()
            var btnLogOut: Button = root.findViewById(R.id.btn_login)
            btnLogOut.text = getString(R.string.button_home_profile)

            btnLogin.setOnClickListener {
                main.openLogoutSection()
            }
        } else {
            btnLogin.setOnClickListener {
                main.openLoginSection()
            }
        }

        main.checkConnection()

        return root
    }
}