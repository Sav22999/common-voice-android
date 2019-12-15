package org.commonvoice.saverio.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_settings.*
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

        val btnSpeak: Button = root.findViewById(R.id.btn_speak)
        val btnListen: Button = root.findViewById(R.id.btn_listen)
        val btnLogin: Button = root.findViewById(R.id.btn_login)

        val main = activity as MainActivity

        btnSpeak.setOnClickListener{
            main.open_speak_section()
        }

        btnListen.setOnClickListener{
            main.open_listen_section()
        }

        if (main.logged) {
            //login successful -> show username and log-out button

            var textLoggedIn: TextView = root.findViewById(R.id.textLoggedUsername)
            textLoggedIn.isGone = false
            textLoggedIn.isVisible = true
            if (main.user_name == "") textLoggedIn.text = getString(R.string.text_hi_username) + "!"
            else {
                textLoggedIn.text = getString(R.string.text_hi_username) + ", " + main.user_name + "!"
            }
            var btnLogOut: Button = root.findViewById(R.id.btn_login)
            btnLogOut.text = getString(R.string.button_home_logout)

            btnLogin.setOnClickListener{
                main.open_logout_section()
            }
        } else {
            btnLogin.setOnClickListener{
                main.open_login_section()
            }
        }

        return root
    }
}