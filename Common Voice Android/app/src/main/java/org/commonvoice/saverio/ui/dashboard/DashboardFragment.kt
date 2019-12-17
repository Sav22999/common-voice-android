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
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_webbrowser.*
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R
import org.json.JSONObject
import org.w3c.dom.Text
import java.lang.Exception
import kotlin.math.truncate

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var webView: WebView
    private var language = "it"

    var statisticsYou = arrayOf(
        0,
        0,
        0,
        0
    ) //(todaySpeak, todayListen, everSpeak, everListen); "-1" indicates an error -> show "?"
    var statisticsEveryone = arrayOf(
        0,
        0,
        0,
        0
    ) //(todaySpeak, todayListen, everSpeak, everListen); "-1" indicates an error -> show "?"

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
        var tabDashboard: TabLayout = root.findViewById(R.id.tabDashboard)

        //load "you" values -> loadYouValues() doesn't work here
        if (main.logged) {
            tabDashboard.getTabAt(0)?.select() //set YOU tab -> logged-in
            loadStatistics("you", root)
        } else {
            tabDashboard.getTabAt(1)
                ?.select() //set EVERYONE tab -> no-logged-in (YOU section disabled)
            loadStatistics("everyone", root)
        }

        //loadYouValues()//it doesn't load

        tabDashboard.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    if (main.logged) {
                        loadStatistics("you", null)
                    } else {
                        tabDashboard.getTabAt(1)?.select()
                        main.noLoggedInNoStatisticsYou()
                    }
                } else if (tab?.position == 1) {
                    loadStatistics("everyone", null)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        return root
    }

    fun getDashboardValues(type: String): Array<Int> {
        if (type == "you") {
            return this.statisticsYou
        } else if (type == "everyone") {
            return this.statisticsEveryone
        }
        return arrayOf(-1, -1, -1, -1) //-1 indicates and error, show "?"
    }

    fun loadStatistics(type: String, root: View?) {
        //load statistics
        if (type == "you") {
            loadStatisticsOf("youTodaySpeak", 0, root)
            loadStatisticsOf("youTodayListen", 1, root)
            loadStatisticsOf("youEverSpeak", 2, root)
            loadStatisticsOf("youEverListen", 3, root)
        } else if (type == "everyone") {
            loadStatisticsOf("everyoneTodaySpeak", 0, root)
            loadStatisticsOf("everyoneTodayListen", 1, root)
            loadStatisticsOf("everyoneEverSpeak", 2, root)
            loadStatisticsOf("everyoneEverListen", 3, root)
        }
        //println(" >> >> "+loadStatisticsOf("everyoneTodaySpeak"))
    }

    fun loadStatisticsOf(type: String, index: Int, root: View?) {
        var valueToReturn = -1
        var speakOrListen = true //true->speak, false->listen
        if (type.contains("Listen")) {
            speakOrListen = false
        }

        val requestUrl = when (type) {
            "youTodaySpeak" -> "" //?
            "youTodayListen" -> "" //?
            "youEverSpeak" -> "https://voice.mozilla.org/api/v1/user_client" //"clips_count"
            "youEverListen" -> "https://voice.mozilla.org/api/v1/user_client" //"votes_count"
            "everyoneTodaySpeak" -> "https://voice.mozilla.org/api/v1/${(activity as MainActivity).getSelectedLanguage()}/clips/daily_count" //just the value we need
            "everyoneTodayListen" -> "https://voice.mozilla.org/api/v1/${(activity as MainActivity).getSelectedLanguage()}/clips/votes/daily_count" //just the value we need
            "everyoneEverSpeak" -> "https://voice.mozilla.org/api/v1/${(activity as MainActivity).getSelectedLanguage()}/clips/stats" //the last "total"
            "everyoneEverListen" -> "https://voice.mozilla.org/api/v1/${(activity as MainActivity).getSelectedLanguage()}/clips/stats" //the last "valid"
            else -> ""
        }
        var viewToUse: View? = view
        if (root != null) {
            viewToUse = root
        }

        var textElements: Array<TextView?> = arrayOf(
            viewToUse?.findViewById(R.id.textTodaySpeak),
            viewToUse?.findViewById(R.id.textTodayListen),
            viewToUse?.findViewById(R.id.textEverSpeak),
            viewToUse?.findViewById(R.id.textEverListen)
        )

        textElements[index]?.text = "..."

        if (requestUrl != "") {
            try {
                val que = Volley.newRequestQueue(viewToUse?.context)
                val req = object : StringRequest(Request.Method.GET, requestUrl,
                    Response.Listener {
                        var jsonResult = it.toString()
                        //println(" >>>> " + type + " -- " + jsonResult)
                        if (type == "everyoneTodaySpeak" || type == "everyoneTodayListen") {
                            if (jsonResult.toInt() >= 0) {
                                valueToReturn = jsonResult.toInt()
                                //println(" >>>> YES! ")
                            }
                        } else if (type == "youEverSpeak" || type == "youEverListen" || type == "everyoneEverSpeak" || type == "everyoneEverListen") {
                            if (type == "everyoneEverSpeak" || type == "everyoneEverListen") {
                                var jsonResultTemp = jsonResult.replace("[{","").replace("}]","")
                                var jsonResultTempArray = jsonResultTemp.split("},{")
                                jsonResult = "[{"+jsonResultTempArray[jsonResultTempArray.size-1]+"}]"
                            }
                            val jsonObj = JSONObject(
                                jsonResult.substring(
                                    jsonResult.indexOf("{"),
                                    jsonResult.lastIndexOf("}") + 1
                                )
                            )
                            if (type == "youEverSpeak") {
                                valueToReturn = jsonObj.getString("clips_count").toInt()
                            } else if (type == "youEverListen") {
                                valueToReturn = jsonObj.getString("votes_count").toInt()
                            } else if (type == "everyoneEverSpeak") {
                                valueToReturn = jsonObj.getString("total").toInt()
                            } else if (type == "everyoneEverListen") {
                                valueToReturn = jsonObj.getString("valid").toInt()
                            }
                            //println(jsonObj)
                        }
                        //println(" >>>> " + type + " -- " + valueToReturn)
                        if (type == "everyoneEverSpeak" || type == "everyoneEverListen") {
                            textElements[index]?.text = truncate(valueToReturn.toDouble()/3600).toInt().toString()+getString(R.string.textHoursAbbreviation)
                        } else {
                            textElements[index]?.text = valueToReturn.toString()
                        }
                        if (valueToReturn == -1) {
                            error3(index)
                        }
                    }, Response.ErrorListener {
                        //println(" -->> Something wrong: " + it.toString() + " <<-- ")
                        error3(index)
                    }
                ) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        //it permits to get the audio to validate (just if user doesn't do the log-in/sign-up)
                        if ((activity as MainActivity).logged) {
                            headers.put(
                                "Cookie",
                                "connect.sid=" + (activity as MainActivity).userId
                            )
                        }
                        return headers
                    }
                }
                que.add(req)
            } catch (e: Exception) {
                //println(" -->> Something wrong: " + e.toString() + " <<-- ")
               error3(index)
            }
        } else {
            error3(index)
        }
    }

    fun error3(index: Int) {
        var textElements: Array<TextView?> = arrayOf(
            view?.findViewById(R.id.textTodaySpeak),
            view?.findViewById(R.id.textTodayListen),
            view?.findViewById(R.id.textEverSpeak),
            view?.findViewById(R.id.textEverListen)
        )

        textElements[index]?.text = "?"
    }
}