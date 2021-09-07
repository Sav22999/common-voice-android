package org.commonvoice.saverio.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import org.commonvoice.saverio.MainActivity
import org.commonvoice.saverio.R
import org.commonvoice.saverio_lib.preferences.SettingsPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import java.util.*


class NotificationsDailyGoalReceiver : BroadcastReceiver() {

    lateinit var title: String
    lateinit var text: String

    var connected = false
    val pattern = "YYYY-MM-dd"

    private var hourWhenShow = 17
    private var hourWhenShowSecond = -1

    lateinit var context: Context

    private val settingsPreferences by lazy {
        context.getSharedPreferences(
            "settingsPreferences",
            Context.MODE_PRIVATE
        )
    }

    private val statsPreferences by lazy {
        context.getSharedPreferences(
            "statsPreferences",
            Context.MODE_PRIVATE
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        sendNow(
            title = context.getString(R.string.message_dailygoal_notification_title),
            text = context.getString(R.string.message_dailygoal_notification_text)
        )
    }

    fun sendNow(title: String, text: String) {
        this.title = title
        this.text = text
        sendNotification(context, title, text, true)
    }

    fun sendNotification(
        context: Context,
        title: String,
        message: String,
        autoCancel: Boolean = true
    ) {
        val notificationNumber = notificationsCounter
        hourWhenShow = dailyGoalNotificationsHour
        hourWhenShowSecond = dailyGoalNotificationsHourSecond
        val NOTIFICATION_CHANNEL_ID =
            "${context.packageName.replace(".", "_")}_notification_${notificationNumber}"
        val NOTIFICATION_CHANNEL_NAME = "${context.packageName}_notification".replace(".", "_")
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                importance
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        var notificationBuilder =
            NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_icon_one_color)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setAutoCancel(autoCancel)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val savedDate = dailyGoalNotificationsLastSentDate
        val savedDateSecond = dailyGoalNotificationsLastSentDateSecond

        val c = Calendar.getInstance()
        val currentDate =
            "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH + 1)}-${c.get(Calendar.DAY_OF_MONTH)}"

        if (dailyGoalNotifications) {
            if (c.get(Calendar.HOUR_OF_DAY) >= hourWhenShow && currentDate != savedDate && dailyGoalObjective > 0) {
                println("^^^^^^^^^^^^^^^^^^^^ > enabled first")
                notificationManager!!.notify(
                    notificationNumber,
                    notificationBuilder.build()
                )
                dailyGoalNotificationsLastSentDate = currentDate
                incrementNotificationCounter()
            } else if (c.get(Calendar.HOUR_OF_DAY) >= hourWhenShowSecond && currentDate != savedDateSecond && dailyGoalObjective > 0) {
                println("^^^^^^^^^^^^^^^^^^^^ > enabled second")
                notificationManager!!.notify(
                    notificationNumber,
                    notificationBuilder.build()
                )
                dailyGoalNotificationsLastSentDateSecond = currentDate
                incrementNotificationCounter()
            } else if (dailyGoalObjective == 0) {
                //Daily goal not set
                println("^^^^^^^^^^^^^^^^^^^^ > daily goal not set")
            } else if (c.get(Calendar.HOUR_OF_DAY) < hourWhenShow) {
                //Too early o'clock (first alert)
                println("^^^^^^^^^^^^^^^^^^^^ > too early first")
            } else if (c.get(Calendar.HOUR_OF_DAY) < hourWhenShowSecond) {
                //Too early o'clock (second alert)
                println("^^^^^^^^^^^^^^^^^^^^ > too early second")
            } else {
                //Notification already sent
                //println("^^^^^^^^^^^^^^^^^^^^ > already sent (or already achieved)")
            }
        } else {
            //Notifications disabled
            //println("^^^^^^^^^^^^^^^^^^^^ > disabled")
        }
    }

    private fun incrementNotificationCounter() {
        //increment notifications counter number
        notificationsCounter = (notificationsCounter + 1)
    }

    var dailyGoalNotifications: Boolean
        get() = settingsPreferences.getBoolean(
            SettingsPrefManager.Keys.DAILY_GOAL_NOTIFICATIONS.name,
            true
        )
        set(value) = settingsPreferences.edit()
            .putBoolean(SettingsPrefManager.Keys.DAILY_GOAL_NOTIFICATIONS.name, value)
            .apply()

    var dailyGoalNotificationsHour: Int
        get() = settingsPreferences.getInt(
            SettingsPrefManager.Keys.DAILY_GOAL_NOTIFICATIONS_HOUR.name,
            17
        )
        set(value) = settingsPreferences.edit()
            .putInt(SettingsPrefManager.Keys.DAILY_GOAL_NOTIFICATIONS_HOUR.name, value)
            .apply()

    var dailyGoalNotificationsHourSecond: Int
        get() = settingsPreferences.getInt(
            SettingsPrefManager.Keys.DAILY_GOAL_NOTIFICATIONS_HOUR_SECOND.name,
            17
        )
        set(value) = settingsPreferences.edit()
            .putInt(SettingsPrefManager.Keys.DAILY_GOAL_NOTIFICATIONS_HOUR_SECOND.name, value)
            .apply()

    var dailyGoalNotificationsLastSentDate: String
        get() = settingsPreferences.getString(
            SettingsPrefManager.Keys.DAILY_GOAL_NOTIFICATIONS_HOUR_LAST_SENT_DATE.name,
            ""
        )!! //YYYY-MM-DD
        set(value) = settingsPreferences.edit()
            .putString(
                SettingsPrefManager.Keys.DAILY_GOAL_NOTIFICATIONS_HOUR_LAST_SENT_DATE.name,
                value
            )
            .apply()

    var dailyGoalNotificationsLastSentDateSecond: String
        get() = settingsPreferences.getString(
            SettingsPrefManager.Keys.DAILY_GOAL_NOTIFICATIONS_HOUR_LAST_SENT_DATE_SECOND.name,
            ""
        )!! //YYYY-MM-DD
        set(value) = settingsPreferences.edit()
            .putString(
                SettingsPrefManager.Keys.DAILY_GOAL_NOTIFICATIONS_HOUR_LAST_SENT_DATE_SECOND.name,
                value
            )
            .apply()

    var notificationsCounter: Int
        get() = settingsPreferences.getInt(SettingsPrefManager.Keys.NOTIFICATIONS_COUNTER.name, 0)
        set(value) = settingsPreferences.edit()
            .putInt(SettingsPrefManager.Keys.NOTIFICATIONS_COUNTER.name, value)
            .apply()

    var dailyGoalObjective: Int
        get() {
            return statsPreferences.getInt(StatsPrefManager.Keys.DAILY_GOAL_OBJECTIVE.name, 0)
        }
        set(value) {
            statsPreferences.edit().putInt(StatsPrefManager.Keys.DAILY_GOAL_OBJECTIVE.name, value)
                .apply()
        }
}