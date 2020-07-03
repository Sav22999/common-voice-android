package org.commonvoice.saverio

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import kotlinx.android.synthetic.main.daily_goal.view.*
import kotlinx.android.synthetic.main.discontinue_app_message.view.*
import kotlinx.android.synthetic.main.message_dialog.view.*
import kotlinx.android.synthetic.main.offline_mode_message.view.*

class MessageDialog {


    private val REQUEST_CODE: Int = 101
    private val DAILY_GOAL = "DAILY_GOAL"
    private var message_type: Int =
        0 /*0->standard (Ok), 1->dailyGoal, 2->standard (Ok) JUST FOR THEME changing ("Dark theme turned on/off),
            3->reportClip (listen), 4->reportSentence (Speak)
            5->info, 6->help, 7->warning, 8->news/changelog, 9->tip
            10->offlineModeMessage, 99->discontinued app message*/
    private var message_text: String = ""
    private var message_title: String = ""
    private var message_details: String = ""
    private var context: Context? = null;
    private var dailyGoalValue: Int = 0
    private var width: Int = 0
    private var height: Int = 0
    private var main: MainActivity? = null
    private var listen: ListenActivity? = null
    private var speak: SpeakActivity? = null

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

    fun setListenActivity(listen: ListenActivity) {
        this.listen = listen
    }

    fun setSpeakActivity(speak: SpeakActivity) {
        this.speak = speak
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
                        dialogView.labelTextMessageDialog.setText(message_to_show)
                        if (this.message_details != "") {
                            dialogView.labelDetailsMessageDialog.setText(this.message_details)
                            dialogView.btnShowHideDetailsMessageDialog.isGone = false
                            dialogView.btnShowHideDetailsMessageDialog.paintFlags =
                                Paint.UNDERLINE_TEXT_FLAG
                            dialogView.btnShowHideDetailsMessageDialog.setText("Show details")
                            dialogView.btnShowHideDetailsMessageDialog.setOnClickListener {
                                if (!dialogView.labelDetailsMessageDialog.isGone) {
                                    dialogView.btnShowHideDetailsMessageDialog.setText("Show details")
                                    dialogView.labelDetailsMessageDialog.isGone = true
                                } else {
                                    dialogView.btnShowHideDetailsMessageDialog.setText("Hide details")
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
                            main?.setDailyGoal(this.dailyGoalValue)
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
                            main?.setDailyGoal(0)
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
                                speak?.setShowOfflineModeMessage(!dialogView.checkDoNotShowAnymore.isChecked)
                            } else if (listen != null) {
                                listen?.setShowOfflineModeMessage(!dialogView.checkDoNotShowAnymore.isChecked)
                            }
                            alertDialog.dismiss()
                        }
                        setTheme(this.context!!, dialogView)
                    } catch (exception: Exception) {
                        println("!!-- Exception: MessageDialogActivity MD01 - Details: " + exception.toString() + " --!!")
                    }
                }
                99 -> {
                    try {
                        val dialogView =
                            LayoutInflater.from(this.context)
                                .inflate(R.layout.discontinue_app_message, null)

                        val builder =
                            AlertDialog.Builder(this.context!!)
                                .setView(dialogView)
                                .setTitle("")
                        //show dialog
                        val alertDialog = builder.show()

                        if (main?.getSourceStore() == "GPS") {
                            dialogView.text7DiscontinueApp.isGone = true
                            dialogView.buttonPayPalDiscontinueApp.isGone = true
                            dialogView.buttonKofiDiscontinueApp.isGone = true
                        }

                        dialogView.buttonDiscourseDiscontinueApp.setOnClickListener {
                            main?.goToDiscourseDiscussionDiscontinued()
                        }

                        dialogView.buttonMatrixDiscontinueApp.setOnClickListener {
                            main?.goToCommonVoiceMozillaMatrixDiscontinued()
                        }

                        dialogView.buttonGitHubDiscontinueApp.setOnClickListener {
                            main?.goToMyGitHubDiscontinued()
                        }

                        dialogView.buttonFacebookDiscontinueApp.setOnClickListener {
                            main?.goToMyFacebookDiscontinued()
                        }

                        dialogView.buttonTwitterDiscontinueApp.setOnClickListener {
                            main?.goToMyTwitterDiscontinued()
                        }

                        dialogView.buttonInstagramDiscontinueApp.setOnClickListener {
                            main?.goToMyInstagramDiscontinued()
                        }

                        dialogView.buttonTelegramDiscontinueApp.setOnClickListener {
                            main?.goToMyTelegramDiscontinued()
                        }

                        dialogView.buttonLinkedInDiscontinueApp.setOnClickListener {
                            main?.goToMyLinkedInDiscountinued()
                        }

                        dialogView.buttonPayPalDiscontinueApp.setOnClickListener {
                            main?.goToMyPayPalDiscontinued()
                        }

                        dialogView.buttonKofiDiscontinueApp.setOnClickListener {
                            main?.goToMyKoFiDiscontinued()
                        }

                        dialogView.buttonOkDiscontinueApp.setOnClickListener {
                            //dismiss dialog
                            main?.setDiscountinuedApp(!dialogView.checkDiscontinueApp.isChecked)
                            alertDialog.dismiss()
                        }
                        setTheme(this.context!!, dialogView)
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
        var theme: DarkLightTheme = DarkLightTheme()
        var isDark = theme.getTheme(view)

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

                if (this.message_type == 2) isDark = !isDark
                theme.setElement(
                    isDark,
                    dialogView.findViewById(R.id.messageDialogSectionMiddle) as ConstraintLayout
                )
                theme.setElement(
                    isDark,
                    view,
                    -1,
                    dialogView.findViewById(R.id.messageDialogSectionMiddle) as ConstraintLayout
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.labelDetailsMessageDialog) as TextView,
                    R.color.colorAlertMessage,
                    R.color.colorAlertMessageDT
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.btnOkMessageDialog) as Button
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.labelTextMessageDialog) as TextView
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.btnShowHideDetailsMessageDialog) as TextView
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
                    isDark,
                    dialogView.findViewById(R.id.dailyGoalSectionMiddle) as ConstraintLayout
                )
                theme.setElement(
                    isDark,
                    view,
                    -1,
                    dialogView.findViewById(R.id.dailyGoalSectionMiddle) as ConstraintLayout
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.labelTextAlertDailyGoalFeature) as TextView,
                    R.color.colorAlertMessage,
                    R.color.colorAlertMessageDT
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.btnDailyGoalCancel) as Button
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.btnDailyGoalDelete) as Button
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.labelTextDailyGoal) as TextView
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.labelDailyGoalValue) as TextView
                )
                theme.setElement(
                    isDark,
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
                    isDark,
                    dialogView.findViewById(R.id.messageDialogSectionMiddleOfflineMode) as ConstraintLayout
                )
                theme.setElement(
                    isDark,
                    view,
                    -1,
                    dialogView.findViewById(R.id.messageDialogSectionMiddleOfflineMode) as ConstraintLayout
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.btnOkMessageDialogOfflineMode) as Button
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.textDescriptionOfflineMode) as TextView
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.textDescriptionOfflineMode2) as TextView
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.textDescriptionOfflineMode3) as TextView
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.checkDoNotShowAnymore) as CheckBox
                )

                setImageNoWifi(view, dialogView, isDark)
            }
        }
    }
}