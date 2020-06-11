package org.commonvoice.saverio.ui.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayout
import org.commonvoice.saverio.DarkLightTheme
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.json.JSONObject
import org.koin.android.ext.android.inject
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

    private val statsPrefManager: StatsPrefManager by inject()

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

    private var selectedTab: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val main = activity as MainActivity
        main.dashboard_selected = true

        val textStatistics: TextView = root.findViewById(R.id.textDashboardStatistics)
        textStatistics.text = getText(R.string.dashboardStatistics)
        val buttonYou: Button = root.findViewById(R.id.buttonYouStatisticsDashboard)
        val buttonEveryone: Button = root.findViewById(R.id.buttonEveryoneStatisticsDashboard)

        //load "you" values -> loadYouValues() doesn't work here
        try {
            if (main.logged) {
                buttonYou.isGone = false
                selectTab(root, main, 0, forced = true)
                loadData("you", root)
            } else {
                buttonYou.isGone = true
                selectTab(root, main, 1, forced = true)
                loadData("everyone", root)
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
            val labelDashboardNow: TextView = root.findViewById(R.id.labelDashboardVoicesNow)
            labelDashboardNow.text = (getString(R.string.textHour) + " " + labelNow + ":00")
            val labelDashboardBefore: TextView =
                root.findViewById(R.id.labelDashboardVoicesBefore)
            labelDashboardBefore.text = (getString(R.string.textHour) + " " + labelBefore + ":00")
            val textVoicesElements: Array<TextView?> = arrayOf(
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

            val btnSetGoal: Button = root.findViewById(R.id.buttonDashboardSetDailyGoal)
            btnSetGoal.setOnClickListener {
                if (main.logged) {
                    main.openDailyGoalDialog()
                } else {
                    main.noLoggedInNoDailyGoal()
                }
            }

            val goalText = root.findViewById<TextView>(R.id.labelDashboardDailyGoalValue)
            if (main.getDailyGoal() <= 0) {
                goalText.setText(getString(R.string.daily_goal_is_not_set))
                goalText.typeface = Typeface.DEFAULT
                root.findViewById<TextView>(R.id.buttonDashboardSetDailyGoal)
                    .setText(getString(R.string.set_daily_goal))
                //println("Daily goal is not set")
            } else {
                goalText.setText(main.getDailyGoal().toString())
                goalText.typeface = ResourcesCompat.getFont(main, R.font.sourcecodepro)
                root.findViewById<TextView>(R.id.buttonDashboardSetDailyGoal)
                    .setText(getString(R.string.edit_daily_goal))
                //println("Daily goal is set")
            }

            buttonYou.setOnClickListener {
                if (selectTab(root, main, 0)) loadData("you", root)
            }
            buttonEveryone.setOnClickListener {
                if (selectTab(root, main, 1)) loadData("everyone", root)
            }
        } catch (e: Exception) {
            //println("Error: " + e.toString())
        }

        setTheme(main, root)

        return root
    }

    fun selectTab(root: View, view: Context, tab: Int, forced: Boolean = false): Boolean {
        if ((tab != selectedTab) || forced) {
            selectedTab = tab
            val theme = DarkLightTheme()
            val isDark = theme.getTheme(view)
            val tabs: Array<Button> = arrayOf(
                root.findViewById(R.id.buttonYouStatisticsDashboard),
                root.findViewById(R.id.buttonEveryoneStatisticsDashboard)
            )

            for (position in tabs.indices) {
                if (position == tab) {
                    if (isDark) {
                        tabs[position].setTextColor(
                            ContextCompat.getColor(
                                view,
                                R.color.colorBlack
                            )
                        )
                        tabs[position].backgroundTintList =
                            ContextCompat.getColorStateList(view, R.color.colorLightGray)
                    } else {
                        tabs[position].setTextColor(
                            ContextCompat.getColor(
                                view,
                                R.color.colorWhite
                            )
                        )
                        tabs[position].backgroundTintList =
                            ContextCompat.getColorStateList(view, R.color.colorBlack)
                    }
                } else {
                    if (isDark) {
                        tabs[position].setTextColor(
                            ContextCompat.getColor(
                                view,
                                R.color.colorWhite
                            )
                        )
                        tabs[position].backgroundTintList =
                            ContextCompat.getColorStateList(view, R.color.colorDarkDarkGray)
                    } else {
                        tabs[position].setTextColor(
                            ContextCompat.getColor(
                                view,
                                R.color.colorBlack
                            )
                        )
                        tabs[position].backgroundTintList =
                            ContextCompat.getColorStateList(view, R.color.colorDarkWhite)
                    }
                }
            }
            return true;
        }
        return false;
    }

    fun setTheme(view: Context, root: View) {
        val theme = DarkLightTheme()
        val isDark = theme.getTheme(view)
        theme.setElements(view, root.findViewById(R.id.layoutDashboard))

        theme.setElements(view, root.findViewById(R.id.dashboardSectionStatistics))
        theme.setElements(view, root.findViewById(R.id.dashboardSectionToday))
        theme.setElements(view, root.findViewById(R.id.dashboardSectionEver))
        theme.setElements(view, root.findViewById(R.id.dashboardSectionVoicesOnline))
        theme.setElements(view, root.findViewById(R.id.dashboardSectionDailyGoal))

        theme.setElement(isDark, view, 3, root.findViewById(R.id.dashboardSectionStatistics))
        theme.setElement(
            isDark,
            view,
            3,
            root.findViewById(R.id.dashboardSectionToday),
            R.color.colorWhiteTransparent,
            R.color.colorLightBlack
        )
        theme.setElement(
            isDark,
            view,
            3,
            root.findViewById(R.id.dashboardSectionEver),
            R.color.colorWhiteTransparent,
            R.color.colorLightBlack
        )
        theme.setElement(isDark, view, 3, root.findViewById(R.id.dashboardSectionVoicesOnline))
        theme.setElement(isDark, view, 3, root.findViewById(R.id.dashboardSectionDailyGoal))

        theme.setTextView(isDark, view, root.findViewById(R.id.textDashboardVoicesNow) as TextView)
        theme.setTextView(
            isDark,
            view,
            root.findViewById(R.id.textDashboardVoicesBefore) as TextView
        )
        theme.setElement(
            isDark,
            view,
            root.findViewById(R.id.buttonDashboardSetDailyGoal) as Button
        )
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
            val loadStatistics: Boolean = loadStatisticsYesOrNot(type)
            val loadVoices: Boolean = loadVoicesOnlineYesOrNot(type)
            val main = (activity as MainActivity)

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

            //println(" ---->>>> type: " + type + " loadStatistics: " + loadStatistics)
            try {
                if (!loadStatistics && (type == "you" || type == "everyone") && (activity as MainActivity).dashboard_selected) {
                    val textElements: Array<TextView?> = arrayOf(
                        root?.findViewById(R.id.textTodaySpeak),
                        root?.findViewById(R.id.textTodayListen),
                        root?.findViewById(R.id.textEverSpeak),
                        root?.findViewById(R.id.textEverListen)
                    )
                    val value1 = main.getSavedStatisticsValue(type, 0)
                    val value2 = main.getSavedStatisticsValue(type, 1)
                    textElements[0]?.text = value1
                    textElements[1]?.text = value2
                    if (type == "everyone") {
                        val value3 = truncate(
                            main.getSavedStatisticsValue(
                                type,
                                3
                            ).toDouble() / 3600
                        ).toInt().toString() + getString(R.string.textHoursAbbreviation)
                        val value4 = truncate(
                            main.getSavedStatisticsValue(
                                type,
                                2
                            ).toDouble() / 3600
                        ).toInt().toString() + getString(R.string.textHoursAbbreviation)
                        textElements[3]?.text = value3
                        textElements[2]?.text = value4
                    } else {
                        val value3 = main.getSavedStatisticsValue(type, 2)
                        val value4 = main.getSavedStatisticsValue(type, 3)
                        textElements[2]?.text = value3
                        textElements[3]?.text = value4
                        //println("-->><<--")
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
                println("Exception Dashboard-009")
            }
        } catch (e: Exception) {
            println("Exception Dashboard-001 !!")
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun loadStatisticsYesOrNot(type: String): Boolean {
        val main = (activity as MainActivity)
        val savedStats = main.getSavedStatistics(type)

        val oldStats: String = savedStats
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
            val oldDate: List<String> = oldStats.split(" ")[0].split("/") //year/month/day
            val oldTime: List<String> = oldStats.split(" ")[1].split(":") //hours:minutes:seconds

            val newDate: List<String> = newStats.split(" ")[0].split("/") //year/month/day
            val newTime: List<String> = newStats.split(" ")[1].split(":") //hours:minutes:seconds

            returnTrueOrFalse = main.passedThirtySeconds(oldDate, oldTime, newDate, newTime)
        }
        //println(" >> " + returnTrueOrFalse.toString() + " >> " + type + " >> new >> " + newStats)
        //println(" >> " + returnTrueOrFalse.toString() + " >> " + type + " >> old >> " + oldStats)

        if (returnTrueOrFalse) {
            main.setSavedStatistics(type, newStats)
        }

        return returnTrueOrFalse
    }

    @SuppressLint("SimpleDateFormat")
    fun loadVoicesOnlineYesOrNot(type: String): Boolean {
        val main = (activity as MainActivity)
        val savedVoices = main.getSavedVoicesOnline(type)

        val oldVoices: String = savedVoices
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
            val oldDate: List<String> = oldVoices.split(" ")[0].split("/") //year/month/day
            val oldTime: List<String> = oldVoices.split(" ")[1].split(":") //hours:minutes:seconds

            val newDate: List<String> = newVoices.split(" ")[0].split("/") //year/month/day
            val newTime: List<String> = newVoices.split(" ")[1].split(":") //hours:minutes:seconds

            returnTrueOrFalse = main.passedThirtySeconds(oldDate, oldTime, newDate, newTime)
        }
        //println(" >> " + returnTrueOrFalse.toString() + " >> " + type + " >> new >> " + newVoices)
        //println(" >> " + returnTrueOrFalse.toString() + " >> " + type + " >> old >> " + oldVoices)

        if (returnTrueOrFalse) {
            main.setSavedVoicesOnline(type, newVoices)
        }

        return returnTrueOrFalse
    }

    private fun loadDataOf(type: String, index: Int, root: View?) {
        try {
            var valueToReturn = -1
            /*
            var speakOrListen = true //true->speak, false->listen
            if (type.contains("Listen")) {
                speakOrListen = false
            }
            */

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

            val textElements: Array<TextView?> = arrayOf(
                viewToUse?.findViewById(R.id.textTodaySpeak),
                viewToUse?.findViewById(R.id.textTodayListen),
                viewToUse?.findViewById(R.id.textEverSpeak),
                viewToUse?.findViewById(R.id.textEverListen)
            )
            val textVoicesElements: Array<TextView?> = arrayOf(
                viewToUse?.findViewById(R.id.textDashboardVoicesNow),
                viewToUse?.findViewById(R.id.textDashboardVoicesBefore)
            )

            val main = (activity as MainActivity)
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
                                try {
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
                                            val jsonResultTemp =
                                                jsonResult.replace("[{", "").replace("}]", "")
                                            val jsonResultTempArray =
                                                jsonResultTemp.split("},{")
                                            jsonResult =
                                                "[{" + jsonResultTempArray[jsonResultTempArray.size - 1] + "}]"
                                        }
                                        val jsonObj = JSONObject(
                                            jsonResult.substring(
                                                jsonResult.indexOf("{"),
                                                jsonResult.lastIndexOf("}") + 1
                                            )
                                        )
                                        when (type) {
                                            "youEverSpeak" -> {
                                                valueToReturn =
                                                    jsonObj.getString("clips_count").toInt()
                                            }
                                            "youEverListen" -> {
                                                valueToReturn =
                                                    jsonObj.getString("votes_count").toInt()
                                            }
                                            "everyoneEverSpeak" -> {
                                                valueToReturn =
                                                    jsonObj.getString("total").toInt()
                                            }
                                            "everyoneEverListen" -> {
                                                valueToReturn =
                                                    jsonObj.getString("valid").toInt()
                                            }
                                            //println(jsonObj)
                                        }
                                        //println(jsonObj)
                                    } else if (type == "voicesNow" || type == "voicesBefore") {
                                        val jsonResultTemp =
                                            jsonResult.replace("[{", "").replace("}]", "")
                                        val jsonResultTempArray = jsonResultTemp.split("},{")
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
                                } catch (e: Exception) {
                                    println("Exception Dashboard-010")
                                }
                                //println(" >>>> " + type + " -- " + valueToReturn + " -- " + typeToSend)

                                //Set text to the value
                                try {
                                    if ((type == "everyoneEverSpeak" || type == "everyoneEverListen") && selectedTab != 0) {
                                        textElements[index]?.text =
                                            (truncate(valueToReturn.toDouble() / 3600).toInt()
                                                .toString() + getString(
                                                R.string.textHoursAbbreviation
                                            ))
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
                                            if (typeToSend == "you" && selectedTab == 0 || typeToSend == "everyone" && selectedTab != 0)
                                                textElements[index]?.text =
                                                    valueToReturn.toString()
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
            try {
                if (main.dashboard_selected) {
                    if (type == "youTodaySpeak" || type == "youTodayListen") {
                        //error
                        when (type) {
                            "youTodaySpeak" -> {
                                val value = main.getContributing("recordings")
                                if (value == "?") {
                                    valueToReturn = -1
                                } else {
                                    valueToReturn = value.toInt()
                                }
                            }
                            "youTodayListen" -> {
                                val value = main.getContributing("validations")
                                if (value == "?") {
                                    valueToReturn = -1
                                } else {
                                    valueToReturn = value.toInt()
                                }
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
                println("Exception Dashboard-011")
            }

        } catch (e: Exception) {
            //println("Error: " + e.toString())
            println("Exception Dashboard-002 !!")
        }
    }

    fun error3(type: String, index: Int, root: View?) {
        try {
            val main = (activity as MainActivity)
            var typeToSend: String = ""
            if (type.contains("you")) {
                typeToSend = "you"
            } else if (type.contains("everyone")) {
                typeToSend = "everyone"
            }
            if (typeToSend == "you" || typeToSend == "everyone") {
                val textElements: Array<TextView?> = arrayOf(
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
                val textVoicesElements: Array<TextView?> = arrayOf(
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