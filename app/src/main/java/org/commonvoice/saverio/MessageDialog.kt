package org.commonvoice.saverio

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import kotlinx.android.synthetic.main.daily_goal.view.*
import kotlinx.android.synthetic.main.message_dialog.view.*
import kotlinx.android.synthetic.main.message_dialog_daily_goal_achived.view.*
import kotlinx.android.synthetic.main.offline_mode_message.view.*
import kotlinx.android.synthetic.main.report_bugs_message.view.*
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import org.koin.core.KoinComponent
import org.koin.core.inject

class MessageDialog : KoinComponent {
    private var message_type: Int =
        0 /*0->standard (Ok), 1->setDailyGoal, 2->standard (Ok) JUST FOR THEME changing ("Dark theme turned on/off),
            3->reportClip (listen), 4->reportSentence (Speak)
            5->info, 6->help, 7->warning, 8->news/changelog, 9->tip
            10->offlineModeMessage, 11->report bug on website
            12->daily goal achieved*/
    private var message_text: String = ""
    private var message_title: String = ""
    private var message_details: String = ""
    private var context: Context? = null
    private var dailyGoalValue: Int = 0
    private var width: Int = 0
    private var height: Int = 0
    private var main: MainActivity? = null
    private var listen: ListenActivity? = null
    private var speak: SpeakActivity? = null

    private val theme by inject<DarkLightTheme>() //TODO change this if we want to switch to Dagger-Hilt
    private val mainPrefManager by inject<MainPrefManager>()
    private val statsPrefManager by inject<StatsPrefManager>()

    constructor(context: Context, title: String, text: String, height: Int = 0) {
        this.context = context
        this.message_type = 0
        this.message_title = title
        this.message_text = text
        this.height = height
    }

    constructor(
        context: Context,
        type: Int,
        title: String,
        text: String,
        details: String = "",
        height: Int = 0
    ) {
        this.context = context
        this.message_type = type
        this.message_title = title
        this.message_text = text
        this.message_details = details
        this.height = height
    }

    constructor(
        context: Context,
        main: MainActivity,
        type: Int,
        value: Int = 0,
        width: Int = 0,
        height: Int = 0
    ) {
        this.context = context
        this.dailyGoalValue = value
        this.message_type = type
        this.width = width
        this.height = height
        this.main = main
    }

    fun setMessageType(type: Int) {
        this.message_type = type
    }

    fun setMainActivity(main: MainActivity) {
        this.main = main
    }

    fun setListenActivity(listen: ListenActivity) {
        this.listen = listen
        this.speak = null
    }

    fun setSpeakActivity(speak: SpeakActivity) {
        this.speak = speak
        this.listen = null
    }

    fun show() {
        if (this.context != null) {
            when (this.message_type) {
                0, 2, 5, 6, 7, 8, 9 -> {
                    try {
                        val dialogView =
                            LayoutInflater.from(this.context).inflate(R.layout.message_dialog, null)

                        val builder =
                            AlertDialog.Builder(this.context!!, R.style.MessageDialogTheme)
                                .setView(dialogView)
                                .setTitle("")
                        //show dialog
                        val alertDialog = builder.show()
                        var message_to_show = this.message_text
                        if (this.message_title != "") {
                            message_to_show = this.message_title + "\n" + message_to_show
                        }
                        dialogView.labelTextMessageDialog.text = message_to_show
                        if (this.message_details != "") {
                            dialogView.labelDetailsMessageDialog.text = this.message_details
                            dialogView.btnShowHideDetailsMessageDialog.isGone = false
                            dialogView.btnShowHideDetailsMessageDialog.paintFlags =
                                Paint.UNDERLINE_TEXT_FLAG
                            dialogView.btnShowHideDetailsMessageDialog.text = "Show details"
                            dialogView.btnShowHideDetailsMessageDialog.setOnClickListener {
                                if (!dialogView.labelDetailsMessageDialog.isGone) {
                                    dialogView.btnShowHideDetailsMessageDialog.text = "Show details"
                                    dialogView.labelDetailsMessageDialog.isGone = true
                                } else {
                                    dialogView.btnShowHideDetailsMessageDialog.text = "Hide details"
                                    dialogView.labelDetailsMessageDialog.isGone = false
                                }
                            }
                        }
                        dialogView.btnOkMessageDialog.setOnClickListener {
                            //dismiss dialog
                            alertDialog.dismiss()
                        }
                        setTheme(this.context!!, dialogView)
                        setMessageType(this.context!!, dialogView)
                    } catch (exception: Exception) {
                        println("!!-- Exception: MessageDialogActivity MD01 - Details: " + exception.toString() + " --!!")
                    }
                }
                1 -> {
                    //dailygoal
                    try {
                        val dialogView =
                            LayoutInflater.from(this.context).inflate(R.layout.daily_goal, null)

                        var buttonDelete = dialogView.btnDailyGoalDelete
                        var buttonSave = dialogView.btnDailyGoalSave
                        var buttonCancel = dialogView.btnDailyGoalCancel

                        checkDeleteButtonDeviceScreen(buttonDelete)

                        var seekBar = dialogView.seekDailyGoalValue
                        seekBar.progress = this.dailyGoalValue
                        setDailyGoalValue(dialogView.labelDailyGoalValue, seekBar.progress)

                        seekBar.setOnSeekBarChangeListener(object :
                            SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                seek: SeekBar,
                                progress: Int, fromUser: Boolean
                            ) {
                                //onProgress
                                setDailyGoalValue(dialogView.labelDailyGoalValue, seek.progress)
                            }

                            override fun onStartTrackingTouch(seek: SeekBar) {
                                //onStart
                            }

                            override fun onStopTrackingTouch(seek: SeekBar) {
                                //onStop
                                setDailyGoalValue(dialogView.labelDailyGoalValue, seek.progress)
                            }
                        })
                        val builder =
                            AlertDialog.Builder(this.context!!, R.style.MessageDialogTheme)
                                .setView(dialogView)
                                .setTitle("")
                        //show dialog
                        val alertDialog = builder.show()
                        buttonSave.setOnClickListener {
                            //save the daily goal
                            statsPrefManager.dailyGoalObjective = dailyGoalValue
                            alertDialog.dismiss()
                            main?.refreshDailyGoalDataInDashboard()
                        }
                        buttonCancel.setOnClickListener {
                            //dismiss dialog - it closes the dialog
                            alertDialog.dismiss()
                            main?.refreshDailyGoalDataInDashboard()
                        }
                        buttonDelete.setOnClickListener {
                            //it delete (set to 0) the daily goal
                            statsPrefManager.dailyGoalObjective = 0
                            alertDialog.dismiss()
                            main?.refreshDailyGoalDataInDashboard()
                        }
                        setTheme(this.context!!, dialogView)
                    } catch (exception: Exception) {
                        println("!!-- Exception: MessageDialogActivity MD02 - Details: " + exception.toString() + " --!!")
                    }
                }
                10 -> {
                    try {
                        val dialogView =
                            LayoutInflater.from(this.context)
                                .inflate(R.layout.offline_mode_message, null)

                        val builder =
                            AlertDialog.Builder(this.context!!, R.style.MessageDialogTheme)
                                .setView(dialogView)
                                .setTitle("")
                        //show dialog
                        val alertDialog = builder.show()

                        dialogView.btnOkMessageDialogOfflineMode.setOnClickListener {
                            //dismiss dialog
                            if (speak != null) {
                                speak?.setShowOfflineModeMessage(!dialogView.checkDoNotShowAnymoreOfflineMode.isChecked)
                            } else if (listen != null) {
                                mainPrefManager.showOfflineModeMessage =
                                    !dialogView.checkDoNotShowAnymoreOfflineMode.isChecked
                            }
                            alertDialog.dismiss()
                        }
                        setTheme(this.context!!, dialogView)
                    } catch (exception: Exception) {
                        println("!!-- Exception: MessageDialogActivity MD01 - Details: " + exception.toString() + " --!!")
                    }
                }
                11 -> {
                    try {
                        val dialogView =
                            LayoutInflater.from(this.context)
                                .inflate(R.layout.report_bugs_message, null)

                        val builder =
                            AlertDialog.Builder(this.context!!, R.style.MessageDialogTheme)
                                .setView(dialogView)
                                .setTitle("")
                        //show dialog
                        val alertDialog = builder.show()

                        dialogView.btnOkMessageDialogReportBug.setOnClickListener {
                            //dismiss dialog
                            mainPrefManager.showReportWebsiteBugs =
                                !dialogView.checkDoNotShowAnymoreReportBug.isChecked

                            alertDialog.dismiss()
                        }

                        if (main != null) {
                            dialogView.btnMessageDialogReportBugMozillaDiscourse.onClick {
                                main?.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://mzl.la/3f7sHqj")
                                    )
                                )
                            }

                            dialogView.btnMessageDialogReportBugGitHub.onClick {
                                main?.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://bit.ly/2Z73TZZ")
                                    )
                                )
                            }
                        }
                        setTheme(this.context!!, dialogView)
                    } catch (exception: Exception) {
                        println("!!-- Exception: MessageDialogActivity MD01 - Details: " + exception.toString() + " --!!")
                    }
                }
                12 -> {
                    try {
                        val dialogView =
                            LayoutInflater.from(this.context)
                                .inflate(R.layout.message_dialog_daily_goal_achived, null)

                        val builder =
                            AlertDialog.Builder(this.context!!, R.style.MessageDialogTheme)
                                .setView(dialogView)
                                .setTitle("")
                        //show dialog
                        val alertDialog = builder.show()
                        var message_to_show = this.message_text
                        if (this.message_title != "") {
                            message_to_show = this.message_title + "\n" + message_to_show
                        }
                        dialogView.labelTextMessageDialogDailyAchieved.text = message_to_show
                        dialogView.btnOkMessageDialogDailyAchieved.setOnClickListener {
                            //dismiss dialog
                            alertDialog.dismiss()
                        }

                        dialogView.btnShareMessageDialogDailyAchieved.setOnClickListener {
                            //share and dismiss dialog
                            alertDialog.dismiss()
                            if (speak != null) {
                                speak?.shareCVAndroidDailyGoal()
                            } else if (listen != null) {
                                listen?.shareCVAndroidDailyGoal()
                            }
                        }
                        setTheme(this.context!!, dialogView)
                        setMessageType(this.context!!, dialogView)
                    } catch (exception: Exception) {
                        println("!!-- Exception: MessageDialogActivity MD01 - Details: " + exception.toString() + " --!!")
                    }
                }
            }
        }
    }

    fun setDailyGoalValue(textBox: TextView, value: Int) {
        if (value == 0) {
            textBox.text =
                context!!.getString(R.string.daily_goal_is_not_set)
            textBox.textSize = 22.toFloat()
            textBox.typeface = Typeface.DEFAULT
        } else {
            textBox.text = value.toString()
            textBox.textSize = 30.toFloat()
            textBox.typeface = ResourcesCompat.getFont(context!!, R.font.sourcecodepro)
        }
        this.dailyGoalValue = value
    }

    private fun checkDeleteButtonDeviceScreen(buttonDelete: Button) {
        if (this.width < 1500) {
            //if the screen is smaller than "1500" it hides the "Delete" button
            buttonDelete.isGone = true
        }
    }

    private fun setMessageType(view: Context, dialogView: View) {
        if (this.message_type == 0 || this.message_type == 1 || this.message_type == 2 || this.message_type == 3 || this.message_type == 4) {
            dialogView.messageDialogSectionMessageType.isGone = true
        } else if (this.message_type == 5) {
            //info
            dialogView.messageDialogSectionMessageType.isGone = false
            /*dialogView.messageDialogSectionMessageType.backgroundTintList =
                ContextCompat.getColorStateList(view, R.color.colorInfo)*/
            dialogView.imageMessageType.setBackgroundResource(R.drawable.ic_info)
            dialogView.textMessageType.text = view.getString(R.string.text_info)
        } else if (this.message_type == 6) {
            //help
            dialogView.messageDialogSectionMessageType.isGone = false
            dialogView.messageDialogSectionMessageType.backgroundTintList =
                ContextCompat.getColorStateList(view, R.color.colorHelp)
            dialogView.imageMessageType.setBackgroundResource(R.drawable.ic_help)
            dialogView.textMessageType.text = view.getString(R.string.text_help)
        } else if (this.message_type == 7) {
            //warning
            dialogView.messageDialogSectionMessageType.isGone = false
            dialogView.messageDialogSectionMessageType.backgroundTintList =
                ContextCompat.getColorStateList(view, R.color.colorWarning)
            dialogView.imageMessageType.setBackgroundResource(R.drawable.ic_warning)
            dialogView.textMessageType.text = view.getString(R.string.text_warning)
        } else if (this.message_type == 8) {
            //news/changelog
            dialogView.messageDialogSectionMessageType.isGone = false
            /*dialogView.messageDialogSectionMessageType.backgroundTintList =
                ContextCompat.getColorStateList(view, R.color.colorChangelog)*/
            dialogView.imageMessageType.setBackgroundResource(R.drawable.ic_news)
            dialogView.textMessageType.text = view.getString(R.string.text_changelog)
        } else if (this.message_type == 9) {
            //tip
            dialogView.messageDialogSectionMessageType.isGone = false
            /*dialogView.messageDialogSectionMessageType.backgroundTintList =
                ContextCompat.getColorStateList(view, R.color.colorTip)*/
            dialogView.imageMessageType.setBackgroundResource(R.drawable.ic_tip)
            dialogView.textMessageType.text = view.getString(R.string.text_tip)
        }
    }

    private fun setImageNoWifi(view: Context, dialogView: View, isDark: Boolean) {
        //set icon "no wifi" in offline-mode message
        if (isDark) {
            (dialogView.findViewById(R.id.imageMessageOfflineMode) as ImageView).imageTintList =
                ContextCompat.getColorStateList(view, R.color.colorWhite)
        } else {
            (dialogView.findViewById(R.id.imageMessageOfflineMode) as ImageView).imageTintList =
                ContextCompat.getColorStateList(view, R.color.colorBlack)
        }
    }

    fun setTheme(view: Context, dialogView: View) {
        when (this.message_type) {
            0, 2, 5, 6, 7, 8, 9 -> {
                //standard message dialog
                if (this.height > 500) {
                    dialogView.messageDialogSectionBackground.layoutParams.height = this.height
                    dialogView.messageDialogSectionBackground.requestLayout()
                } else {
                    dialogView.messageDialogSectionBackground.backgroundTintList =
                        ContextCompat.getColorStateList(view, R.color.colorTransparent)
                }

                theme.setElement(
                    dialogView.findViewById(R.id.messageDialogSectionMiddle) as ConstraintLayout,
                    invert = message_type == 2
                )
                theme.setElement(
                    view,
                    -1,
                    dialogView.findViewById(R.id.messageDialogSectionMiddle) as ConstraintLayout,
                    invert = message_type == 2
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.labelDetailsMessageDialog) as TextView,
                    R.color.colorAlertMessage,
                    R.color.colorAlertMessageDT,
                    invert = message_type == 2
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.btnOkMessageDialog) as Button,
                    invert = message_type == 2
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.labelTextMessageDialog) as TextView,
                    invert = message_type == 2
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.btnShowHideDetailsMessageDialog) as TextView,
                    invert = message_type == 2
                )
            }
            1 -> {
                //daily goal
                if (this.height > 1000) {
                    dialogView.dailyGoalSectionBackground.layoutParams.height = this.height
                    dialogView.dailyGoalSectionBackground.requestLayout()
                } else {
                    dialogView.dailyGoalSectionBackground.backgroundTintList =
                        ContextCompat.getColorStateList(view, R.color.colorTransparent)
                }
                theme.setElement(
                    dialogView.findViewById(R.id.dailyGoalSectionMiddle) as ConstraintLayout
                )
                theme.setElement(
                    view,
                    -1,
                    dialogView.findViewById(R.id.dailyGoalSectionMiddle) as ConstraintLayout
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.labelTextAlertDailyGoalFeature) as TextView,
                    R.color.colorAlertMessage,
                    R.color.colorAlertMessageDT
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.btnDailyGoalSave) as Button
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.btnDailyGoalCancel) as Button
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.btnDailyGoalDelete) as Button
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.labelTextDailyGoal) as TextView
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.labelDailyGoalValue) as TextView
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.seekDailyGoalValue) as SeekBar
                )
            }
            10 -> {
                //offline mode message
                if (this.height > 500) {
                    dialogView.messageDialogSectionBackgroundOfflineMode.layoutParams.height =
                        this.height
                    dialogView.messageDialogSectionBackgroundOfflineMode.requestLayout()
                } else {
                    dialogView.messageDialogSectionBackgroundOfflineMode.backgroundTintList =
                        ContextCompat.getColorStateList(view, R.color.colorTransparent)
                }

                theme.setElement(
                    dialogView.findViewById(R.id.messageDialogSectionMiddleOfflineMode) as ConstraintLayout
                )
                theme.setElement(
                    view,
                    -1,
                    dialogView.findViewById(R.id.messageDialogSectionMiddleOfflineMode) as ConstraintLayout
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.btnOkMessageDialogOfflineMode) as Button
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.textDescriptionOfflineMode) as TextView
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.textDescriptionOfflineMode2) as TextView
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.textDescriptionOfflineMode3) as TextView
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.checkDoNotShowAnymoreOfflineMode) as CheckBox
                )

                setImageNoWifi(view, dialogView, theme.isDark)
            }
            11 -> {
                //report website bug message
                if (this.height > 500) {
                    dialogView.messageDialogSectionBackgroundReportBug.layoutParams.height =
                        this.height
                    dialogView.messageDialogSectionBackgroundReportBug.requestLayout()
                } else {
                    dialogView.messageDialogSectionBackgroundReportBug.backgroundTintList =
                        ContextCompat.getColorStateList(view, R.color.colorTransparent)
                }

                theme.setElement(
                    dialogView.findViewById(R.id.messageDialogSectionMiddleReportBug) as ConstraintLayout
                )
                theme.setElement(
                    view,
                    -1,
                    dialogView.findViewById(R.id.messageDialogSectionMiddleReportBug) as ConstraintLayout
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.btnOkMessageDialogReportBug) as Button
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.btnMessageDialogReportBugGitHub) as Button
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.btnMessageDialogReportBugMozillaDiscourse) as Button
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.textDescriptionReportBug) as TextView
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.checkDoNotShowAnymoreReportBug) as CheckBox
                )

                setImageNoWifi(view, dialogView, theme.isDark)
            }
            12 -> {
                //standard message dialog
                if (this.height > 500) {
                    dialogView.messageDialogSectionBackgroundDailyAchieved.layoutParams.height =
                        this.height
                    dialogView.messageDialogSectionBackgroundDailyAchieved.requestLayout()
                } else {
                    dialogView.messageDialogSectionBackgroundDailyAchieved.backgroundTintList =
                        ContextCompat.getColorStateList(view, R.color.colorTransparent)
                }

                theme.setElement(
                    dialogView.findViewById(R.id.messageDialogSectionMiddleDailyAchieved) as ConstraintLayout,
                    invert = message_type == 2
                )
                theme.setElement(
                    view,
                    -1,
                    dialogView.findViewById(R.id.messageDialogSectionMiddleDailyAchieved) as ConstraintLayout,
                    invert = message_type == 2
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.btnOkMessageDialogDailyAchieved) as Button,
                    invert = message_type == 2
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.btnShareMessageDialogDailyAchieved) as Button,
                    invert = message_type == 2
                )
                theme.setElement(
                    view,
                    dialogView.findViewById(R.id.labelTextMessageDialogDailyAchieved) as TextView,
                    invert = message_type == 2
                )
            }
        }
    }
}