package org.commonvoice.saverio.ui.dashboard

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_webbrowser.*
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R
import org.w3c.dom.Text

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var webView: WebView
    private var language = "it"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val textStatistics: TextView = root.findViewById(R.id.text_dashboardStatistics)
        textStatistics.text = getText(R.string.dashboardStatistics)
        var main = activity as MainActivity
        main.loadStatistics()
        var tabDashboard: TabLayout = root.findViewById(R.id.tabDashboard)

        //load "you" values -> loadYouValues() doesn't work here
        var values: Array<Int>;
        if (main.logged) {
            values = main.getDashboardValues("you")//get array of value (logged-in)
        } else {
            values =
                main.getDashboardValues("everyone")//get array of value - everyone (no-logged-in)
            tabDashboard.getTabAt(1)?.select()
        }
        var index = 0
        var textElements: Array<TextView> = arrayOf(
            root.findViewById(R.id.textTodaySpeak),
            root.findViewById(R.id.textTodayListen),
            root.findViewById(R.id.textEverSpeak),
            root.findViewById(R.id.textEverListen)
        )
        while (index < 4) {
            if (values[index] != -1) {
                textElements[index].text = values[index].toString()
            } else {
                textElements[index].text = "?"
            }
            index++
        }

        //loadYouValues()//it doesn't load

        tabDashboard.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    if (main.logged) {
                        loadYouValues()
                    } else {
                        tabDashboard.getTabAt(1)?.select()
                        main.noLoggedInNoStatisticsYou()
                    }
                } else if (tab?.position == 1) {
                    loadEveryoneValues()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        return root
    }

    fun loadYouValues() {
        var values = (activity as MainActivity).getDashboardValues("you")//get array of value
        setDashboardValues(values)
    }

    fun loadEveryoneValues() {
        var values = (activity as MainActivity).getDashboardValues("everyone")//get array of value
        setDashboardValues(values)
    }

    fun setDashboardValues(values: Array<Int>) {
        var index = 0
        var textElements: Array<TextView?> = arrayOf(
            view?.findViewById(R.id.textTodaySpeak),
            view?.findViewById(R.id.textTodayListen),
            view?.findViewById(R.id.textEverSpeak),
            view?.findViewById(R.id.textEverListen)
        )
        while (index < 4) {
            if (values[index] != -1) {
                textElements[index]?.text = values[index].toString()
            } else {
                textElements[index]?.text = "?"
            }
            index++
        }
    }
}