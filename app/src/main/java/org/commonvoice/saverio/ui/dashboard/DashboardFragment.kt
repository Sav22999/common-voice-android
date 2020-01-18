package org.commonvoice.saverio.ui.dashboard

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayout
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R
import org.json.JSONObject
import org.w3c.dom.Text
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap
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

        var main = activity as MainActivity
        main.dashboard_selected = true

        val textStatistics: TextView = root.findViewById(R.id.textDashboardStatistics)
        textStatistics.text = getText(R.string.dashboardStatistics)
        var tabDashboard: TabLayout = root.findViewById(R.id.tabDashboard)

        //load "you" values -> loadYouValues() doesn't work here
        try {
            if (main.logged) {
                tabDashboard.getTabAt(0)?.select() //set YOU tab -> logged-in
                try {
                    loadData("you", root)
                } catch (e: Exception) {
                    //println("Error: " + e.toString())
                }
            } else {
                tabDashboard.getTabAt(1)
                    ?.select() //set EVERYONE tab -> no-logged-in (YOU section disabled)
                try {
                    loadData("everyone", root)
                } catch (e: Exception) {
                    //println("Error: " + e.toString())
                }
            }

            var labelNow: String = ""
            var labelBefore: String = ""
            if (Build.VERSION.SDK_INT < 26) {
                labelNow = SimpleDateFormat("hh").format(Date()).toString()
                if ((labelNow.toInt() - 1) >= 0) {
                    labelBefore = (labelNow.toInt() - 1).toString()
                } else {
                    labelBefore = "23"
                }

            } else {
                val dateTemp = LocalDateTime.now()
                labelNow = dateTemp.hour.toString()
                if ((labelNow.toInt() - 1) >= 0) {
                    labelBefore = (labelNow.toInt() - 1).toString()
                } else {
                    labelBefore = "23"
                }
            }
            var labelDashboardNow: TextView = root.findViewById(R.id.labelDashboardVoicesNow)
            labelDashboardNow.text = getString(R.string.textHour) + " " + labelNow + ":00"
            var labelDashboardBefore: TextView =
                root.findViewById(R.id.labelDashboardVoicesBefore)
            labelDashboardBefore.text = getString(R.string.textHour) + " " + labelBefore + ":00"
            var textVoicesElements: Array<TextView?> = arrayOf(
                root.findViewById(R.id.textDashboardVoicesNow),
                root.findViewById(R.id.textDashboardVoicesBefore)
            )
            textVoicesElements[0]?.text = "..."
            textVoicesElements[1]?.text = "..."

            try {
                loadData("voicesNow", root)
            } catch (e: Exception) {
                println("Error: " + e.toString())
            }
            try {
                loadData("voicesBefore", root)
            } catch (e: Exception) {
                println("Error: " + e.toString())
            }

            //loadYouValues()//it doesn't load

            tabDashboard.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab?.position == 0) {
                        if (main.logged) {
                            try {
                                loadData("you", root)
                            } catch (e: Exception) {
                                //println("Error: " + e.toString())
                            }
                        } else {
                            tabDashboard.getTabAt(1)?.select()
                            main.noLoggedInNoStatisticsYou()
                        }
                    } else if (tab?.position == 1) {
                        try {
                            loadData("everyone", root)
                        } catch (e: Exception) {
                            //println("Error: " + e.toString())
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }

        main.checkConnection()

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

    fun loadData(type: String, root: View?) {
        //load statistics
        try {
            var loadStatistics: Boolean = loadStatisticsYesOrNot(type)
            var loadVoices: Boolean = loadVoicesOnlineYesOrNot(type)
            var main = (activity as MainActivity)

            if (type == "you" && loadStatistics) {
                loadDataOf("youTodaySpeak", 0, root)
                loadDataOf("youTodayListen", 1, root)
                loadDataOf("youEverSpeak", 2, root)
                loadDataOf("youEverListen", 3, root)
            } else if (type == "everyone" && loadStatistics) {
                loadDataOf("everyoneTodaySpeak", 0, root)
                loadDataOf("everyoneTodayListen", 1, root)
                loadDataOf("everyoneEverSpeak", 2, root)
                loadDataOf("everyoneEverListen", 3, root)
            } else if ((type == "voicesNow" || type == "voicesBefore") && loadVoices) {
                loadDataOf(type, -1, root)
            }

            println(" ---->>>> type: " + type + " loadStatistics: " + loadStatistics)
            if (!loadStatistics && (type == "you" || type == "everyone") && (activity as MainActivity).dashboard_selected) {
                var textElements: Array<TextView?> = arrayOf(
                    root?.findViewById(R.id.textTodaySpeak),
                    root?.findViewById(R.id.textTodayListen),
                    root?.findViewById(R.id.textEverSpeak),
                    root?.findViewById(R.id.textEverListen)
                )
                var value1 = main.getSavedStatisticsValue(type, 0)
                var value2 = main.getSavedStatisticsValue(type, 1)
                textElements[0]?.text = value1
                textElements[1]?.text = value2
                if (type == "everyone") {
                    var value3 = truncate(
                        main.getSavedStatisticsValue(
                            type,
                            3
                        ).toDouble() / 3600
                    ).toInt().toString() + getString(R.string.textHoursAbbreviation)
                    var value4 = truncate(
                        main.getSavedStatisticsValue(
                            type,
                            2
                        ).toDouble() / 3600
                    ).toInt().toString() + getString(R.string.textHoursAbbreviation)
                    textElements[3]?.setText(value3)
                    textElements[2]?.setText(value4)
                } else {
                    var value3 = main.getSavedStatisticsValue(type, 2)
                    var value4 = main.getSavedStatisticsValue(type, 3)
                    textElements[2]?.setText(value3)
                    textElements[3]?.setText(value4)
                    println("-->><<--")
                }
            } else if (!loadVoices && type.contains("voices") && (activity as MainActivity).dashboard_selected) {
                var textVoicesElements: Array<TextView?> = arrayOf(
                    root?.findViewById(R.id.textDashboardVoicesNow),
                    root?.findViewById(R.id.textDashboardVoicesBefore)
                )
                var main = (activity as MainActivity)
                textVoicesElements[0]?.text = main.getSavedVoicesOnline("voicesNowValue")
                textVoicesElements[1]?.text = main.getSavedVoicesOnline("voicesBeforeValue")
            }
        } catch (e: Exception) {
            println("Exception Dashboard-001 !!")
        }
    }

    fun loadStatisticsYesOrNot(type: String): Boolean {
        var main = (activity as MainActivity)
        var savedStats = main.getSavedStatistics(type)

        var oldStats: String = savedStats
        var newStats: String = "?"
        if (Build.VERSION.SDK_INT < 26) {
            val dateTemp = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
            newStats = dateTemp.format(Date()).toString()
        } else {
            val dateTemp = LocalDateTime.now()
            newStats =
                dateTemp.year.toString() + "/" + dateTemp.monthValue.toString() + "/" + dateTemp.dayOfMonth.toString() + " " + dateTemp.hour.toString() + ":" + dateTemp.minute.toString() + ":" + dateTemp.second.toString()
        }

        var returnTrueOrFalse: Boolean = true

        if (savedStats != "?") {
            var oldDate: List<String> = oldStats.split(" ")[0].split("/") //year/month/day
            var oldTime: List<String> = oldStats.split(" ")[1].split(":") //hours:minutes:seconds

            var newDate: List<String> = newStats.split(" ")[0].split("/") //year/month/day
            var newTime: List<String> = newStats.split(" ")[1].split(":") //hours:minutes:seconds

            returnTrueOrFalse = passedThirtySeconds(oldDate, oldTime, newDate, newTime)
        }
        //println(" >> " + returnTrueOrFalse.toString() + " >> " + type + " >> new >> " + newStats)
        //println(" >> " + returnTrueOrFalse.toString() + " >> " + type + " >> old >> " + oldStats)

        if (returnTrueOrFalse) {
            main.setSavedStatistics(type, newStats)
        }

        return returnTrueOrFalse
    }

    fun loadVoicesOnlineYesOrNot(type: String): Boolean {
        var main = (activity as MainActivity)
        var savedVoices = main.getSavedVoicesOnline(type)

        var oldVoices: String = savedVoices
        var newVoices: String = "?"
        if (Build.VERSION.SDK_INT < 26) {
            val dateTemp = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
            newVoices = dateTemp.format(Date()).toString()
        } else {
            val dateTemp = LocalDateTime.now()
            newVoices =
                dateTemp.year.toString() + "/" + dateTemp.monthValue.toString() + "/" + dateTemp.dayOfMonth.toString() + " " + dateTemp.hour.toString() + ":" + dateTemp.minute.toString() + ":" + dateTemp.second.toString()
        }

        var returnTrueOrFalse: Boolean = true

        if (savedVoices != "?") {
            var oldDate: List<String> = oldVoices.split(" ")[0].split("/") //year/month/day
            var oldTime: List<String> = oldVoices.split(" ")[1].split(":") //hours:minutes:seconds

            var newDate: List<String> = newVoices.split(" ")[0].split("/") //year/month/day
            var newTime: List<String> = newVoices.split(" ")[1].split(":") //hours:minutes:seconds

            returnTrueOrFalse = passedThirtySeconds(oldDate, oldTime, newDate, newTime)
        }
        //println(" >> " + returnTrueOrFalse.toString() + " >> " + type + " >> new >> " + newVoices)
        //println(" >> " + returnTrueOrFalse.toString() + " >> " + type + " >> old >> " + oldVoices)

        if (returnTrueOrFalse) {
            main.setSavedVoicesOnline(type, newVoices)
        }

        return returnTrueOrFalse
    }

    fun passedThirtySeconds(
        oldDate: List<String>,
        oldTime: List<String>,
        newDate: List<String>,
        newTime: List<String>
    ): Boolean {
        var returnTrueOrFalse: Boolean = true

        // the else-clause indicates the "==", because it shouldn't be never old>new
        if (oldDate[0].toInt() < newDate[0].toInt()) {
            returnTrueOrFalse = true
        } else {
            if (oldDate[1].toInt() < newDate[1].toInt()) {
                returnTrueOrFalse = true
            } else {
                if (oldTime[0].toInt() < newTime[0].toInt()) {
                    returnTrueOrFalse = true
                } else {
                    if (oldTime[1].toInt() < newTime[1].toInt()) {
                        if (newTime[1].toInt() - 1 > oldTime[1].toInt()) {
                            returnTrueOrFalse = true
                        } else {
                            returnTrueOrFalse =
                                newTime[2].toInt() + 60 - oldTime[2].toInt() > 30
                        }
                    } else {
                        if (oldTime[2].toInt() < newTime[2].toInt()) {
                            if (newTime[2].toInt() > 30) {
                                returnTrueOrFalse = newTime[2].toInt() - oldTime[2].toInt() > 30
                            } else {
                                returnTrueOrFalse = false
                            }
                        } else {
                            returnTrueOrFalse = false
                        }
                    }
                }
            }
        }
        return returnTrueOrFalse
    }

    fun loadDataOf(type: String, index: Int, root: View?) {
        try {
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
                "voicesNow" -> "https://voice.mozilla.org/api/v1/${(activity as MainActivity).getSelectedLanguage()}/clips/voices" //voices online current hour
                "voicesBefore" -> "https://voice.mozilla.org/api/v1/${(activity as MainActivity).getSelectedLanguage()}/clips/voices" //voices online the before hour
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
            var textVoicesElements: Array<TextView?> = arrayOf(
                viewToUse?.findViewById(R.id.textDashboardVoicesNow),
                viewToUse?.findViewById(R.id.textDashboardVoicesBefore)
            )

            var main = (activity as MainActivity)
            if (index != -1 && main.dashboard_selected) {
                textElements[index]?.text = "..."
            }

            var typeToSend: String = ""
            if (type.contains("you")) {
                typeToSend = "you"
            } else if (type.contains("everyone")) {
                typeToSend = "everyone"
            }

            if (requestUrl != "") {
                try {
                    val que = Volley.newRequestQueue(viewToUse?.context)
                    val req = object : StringRequest(Request.Method.GET, requestUrl,
                        Response.Listener {
                            if (main.dashboard_selected) {
                                var jsonResult = it.toString()
                                //println(" >>>> " + type + " -- " + jsonResult)
                                if (type == "everyoneTodaySpeak" || type == "everyoneTodayListen") {
                                    if (jsonResult.toInt() >= 0) {
                                        valueToReturn = jsonResult.toInt()
                                        //println(" >>>> YES! ")
                                    }
                                } else if (type == "youTodaySpeak" || type == "youTodayListen") {
                                    //
                                } else if (type == "youEverSpeak" || type == "youEverListen" || type == "everyoneEverSpeak" || type == "everyoneEverListen") {
                                    if (type == "everyoneEverSpeak" || type == "everyoneEverListen") {
                                        var jsonResultTemp =
                                            jsonResult.replace("[{", "").replace("}]", "")
                                        var jsonResultTempArray = jsonResultTemp.split("},{")
                                        jsonResult =
                                            "[{" + jsonResultTempArray[jsonResultTempArray.size - 1] + "}]"
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
                                } else if (type == "voicesNow" || type == "voicesBefore") {
                                    var jsonResultTemp =
                                        jsonResult.replace("[{", "").replace("}]", "")
                                    var jsonResultTempArray = jsonResultTemp.split("},{")
                                    if (type == "voicesNow") {
                                        jsonResult =
                                            "[{" + jsonResultTempArray[jsonResultTempArray.size - 1] + "}]"
                                    } else {
                                        jsonResult =
                                            "[{" + jsonResultTempArray[jsonResultTempArray.size - 2] + "}]"
                                    }
                                    val jsonObj = JSONObject(
                                        jsonResult.substring(
                                            jsonResult.indexOf("{"),
                                            jsonResult.lastIndexOf("}") + 1
                                        )
                                    )
                                    valueToReturn = jsonObj.getString("value").toInt()
                                }
                                println(" >>>> " + type + " -- " + valueToReturn + " -- " + typeToSend)
                                try {
                                    if (type == "everyoneEverSpeak" || type == "everyoneEverListen") {
                                        textElements[index]?.text =
                                            truncate(valueToReturn.toDouble() / 3600).toInt().toString() + getString(
                                                R.string.textHoursAbbreviation
                                            )
                                        main.setSavedStatisticsValue(
                                            typeToSend,
                                            valueToReturn.toString(),
                                            index
                                        )
                                    } else if (type == "voicesNow") {
                                        textVoicesElements[0]?.text = valueToReturn.toString()
                                        main.setSavedVoicesOnline(
                                            type + "Value",
                                            valueToReturn.toString()
                                        )
                                    } else if (type == "voicesBefore") {
                                        textVoicesElements[1]?.text = valueToReturn.toString()
                                        main.setSavedVoicesOnline(
                                            type + "Value",
                                            valueToReturn.toString()
                                        )
                                    } else {
                                        if (index != -1) {
                                            textElements[index]?.text = valueToReturn.toString()
                                            main.setSavedStatisticsValue(
                                                typeToSend,
                                                valueToReturn.toString(),
                                                index
                                            )
                                        }
                                    }
                                } catch (e: Exception) {
                                    println("Exception Dashboard-003 !!")
                                }
                                if (valueToReturn == -1) {
                                    error3(type, index, viewToUse)
                                }
                            }
                        }, Response.ErrorListener {
                            println(" -->> Something wrong: " + type + " -- " + it.toString() + " <<-- ")
                            error3(type, index, viewToUse)
                        }
                    ) {
                        @Throws(AuthFailureError::class)
                        override fun getHeaders(): Map<String, String> {
                            val headers = HashMap<String, String>()
                            //it permits to get the audio to validate (just if user doesn't do the log-in/sign-up)
                            if (main.logged) {
                                headers.put(
                                    "Cookie",
                                    "connect.sid=" + main.userId
                                )
                            }
                            return headers
                        }
                    }
                    que.add(req)
                } catch (e: Exception) {
                    println(" -->> Something wrong (2): " + type + " -- " + e.toString() + " <<-- ")
                    error3(type, index, viewToUse)
                }
            } else {
                //error3(type, index, viewToUse)
            }

            //tempContributing-today
            if (main.dashboard_selected) {
                if (type == "youTodaySpeak" || type == "youTodayListen") {
                    if (type == "youTodaySpeak") {
                        var value = main.getContributing("recordings")
                        if (value == "?") {
                            valueToReturn = -1
                        } else {
                            valueToReturn = value.toInt()
                        }
                    } else if (type == "youTodayListen") {
                        var value = main.getContributing("validations")
                        if (value == "?") {
                            valueToReturn = -1
                        } else {
                            valueToReturn = value.toInt()
                        }
                    }
                    try {
                        if (index != -1) {
                            textElements[index]?.text = valueToReturn.toString()
                            main.setSavedStatisticsValue(
                                typeToSend,
                                valueToReturn.toString(),
                                index
                            )
                        }
                    } catch (e: Exception) {
                        //error
                        println("Exception Dashboard-004 !!")
                    }
                    if (valueToReturn == -1) {
                        error3(type, index, viewToUse)
                    }
                }
            }
            //endTempContributing-today

        } catch (e: Exception) {
            //println("Error: " + e.toString())
            println("Exception Dashboard-002 !!")
        }
    }

    fun error3(type: String, index: Int, root: View?) {
        try {
            var main = (activity as MainActivity)
            var typeToSend: String = ""
            if (type.contains("you")) {
                typeToSend = "you"
            } else if (type.contains("everyone")) {
                typeToSend = "everyone"
            }
            if (typeToSend == "you" || typeToSend == "everyone") {
                var textElements: Array<TextView?> = arrayOf(
                    root?.findViewById(R.id.textTodaySpeak),
                    root?.findViewById(R.id.textTodayListen),
                    root?.findViewById(R.id.textEverSpeak),
                    root?.findViewById(R.id.textEverListen)
                )
                textElements[index]?.text = "?"
                main.setSavedStatisticsValue(
                    typeToSend,
                    "?",
                    index
                )
            } else {
                var textVoicesElements: Array<TextView?> = arrayOf(
                    root?.findViewById(R.id.textDashboardVoicesNow),
                    root?.findViewById(R.id.textDashboardVoicesBefore)
                )

                if (type == "voicesNow") {
                    textVoicesElements[0]?.text = "?"
                    main.setSavedVoicesOnline("voicesNowValue", "?")
                } else {
                    textVoicesElements[1]?.text = "?"
                    main.setSavedVoicesOnline("voicesBeforeValue", "?")
                }
            }
        } catch (e: Exception) {
            println("Error: " + e.toString())
        }
    }
}