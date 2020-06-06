package org.commonvoice.saverio

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.daily_goal.view.*
import kotlinx.android.synthetic.main.message_dialog.view.*
import kotlinx.android.synthetic.main.report_sentence_clip.view.*
import org.json.JSONArray
import org.w3c.dom.Text

class MessageDialog {
    private val REQUEST_CODE: Int = 101
    private val DAILY_GOAL = "DAILY_GOAL"
    private var message_type: Int =
        0 // 0->standard (Ok), 1->dailyGoal, 2->standard (Ok) JUST FOR THEME changing ("Dark theme turned on/off), 3->reportClip (listen), 4->reportSentence (Speak)
    private var message_text: String = ""
    private var message_title: String = ""
    private var message_details: String = ""
    private var context: Context? = null;
    private var dailyGoalValue: Int = 0
    private var width: Int = 0
    private var reportType: String = ""
    private var main: MainActivity? = null
    private var speak: SpeakActivity? = null
    private var listen: ListenActivity? = null

    constructor(context: Context, title: String, text: String) {
        this.context = context
        this.message_type = 0
        this.message_title = title
        this.message_text = text
    }

    constructor(context: Context, type: Int, title: String, text: String, details: String = "") {
        this.context = context
        this.message_type = type
        this.message_title = title
        this.message_text = text
        this.message_details = details
    }

    constructor(context: Context, main: MainActivity, type: Int, value: Int = 0, width: Int = 0) {
        this.context = context
        this.dailyGoalValue = value
        this.message_type = type
        this.width = width
        this.main = main
    }

    constructor(context: Context, reportType: String, listen: ListenActivity) {
        //report - Listen
        this.reportType = reportType
        this.listen = listen
        this.message_type = 3
        this.context = context
    }

    constructor(context: Context, reportType: String, speak: SpeakActivity) {
        //report - Speak
        this.reportType = reportType
        this.speak = speak
        this.message_type = 4
        this.context = context
    }

    fun setMessageType(type: Int) {
        this.message_type = type
    }

    fun show() {
        if (this.context != null) {
            when (this.message_type) {
                0, 2 -> {
                    try {
                        val dialogView =
                            LayoutInflater.from(this.context).inflate(R.layout.message_dialog, null)

                        val builder = AlertDialog.Builder(this.context!!)
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
                    } catch (exception: Exception) {
                        println("!!-- Exception: MessageDialogActivity MD01 - Details: " + exception.toString() + " --!!")
                    }
                }
                1 -> {
                    //println("Daily goal")
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
                        val builder = AlertDialog.Builder(this.context!!)
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
                3, 4 -> {
                    try {
                        val dialogView =
                            LayoutInflater.from(this.context)
                                .inflate(R.layout.report_sentence_clip, null)

                        var buttonSend = dialogView.btnSendReport
                        var buttonCancel = dialogView.btnCancelReport

                        var checkBoxOther: CheckBox = dialogView.checkBoxReason5Report
                        var textBoxOther: TextView = dialogView.textReasonOtherReport
                        checkBoxOther.setOnCheckedChangeListener { buttonView, isChecked ->
                            textBoxOther.isVisible = isChecked
                            checkAvailability(dialogView)
                            textBoxOther.requestFocus()
                        }
                        textBoxOther.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {
                            }

                            override fun beforeTextChanged(
                                s: CharSequence?, start: Int, count: Int, after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?, start: Int, before: Int, count: Int
                            ) {
                                checkAvailability(dialogView)
                            }

                        })

                        buttonSend.isEnabled = false

                        var checkBoxes = arrayOf(
                            dialogView.checkBoxReason1Report,
                            dialogView.checkBoxReason2Report,
                            dialogView.checkBoxReason3Report,
                            dialogView.checkBoxReason4Report
                        )
                        for (x in 0 until checkBoxes.size) {
                            checkBoxes[x].setOnCheckedChangeListener { buttonView, isChecked ->
                                checkAvailability(dialogView)
                            }
                        }

                        var reasons = JSONArray()

                        dialogView.checkBoxReason1Report.isChecked = false
                        dialogView.checkBoxReason2Report.isChecked = false
                        dialogView.checkBoxReason3Report.isChecked = false
                        dialogView.checkBoxReason4Report.isChecked = false
                        dialogView.checkBoxReason5Report.isChecked = false
                        dialogView.textReasonOtherReport.isVisible = false

                        if (this.message_type == 3) {
                            dialogView.checkBoxReason4Report.isVisible = false
                            dialogView.checkBoxReason1Report.text =
                                this.listen?.getString(R.string.checkbox_reason1_report_clip)
                            dialogView.titleReportSentenceClip.text =
                                this.listen?.getString(R.string.title_report_clip)
                        } else if (this.message_type == 4) {
                            dialogView.checkBoxReason4Report.isVisible = true
                            dialogView.checkBoxReason1Report.text =
                                this.speak?.getString(R.string.checkbox_reason1_report_sentence)
                            dialogView.titleReportSentenceClip.text =
                                this.speak?.getString(R.string.title_report_sentence)
                        }

                        val builder = AlertDialog.Builder(this.context!!)
                            .setView(dialogView)
                            .setTitle("")
                        //show dialog
                        val alertDialog = builder.show()
                        buttonSend.setOnClickListener {
                            if (dialogView.checkBoxReason1Report.isChecked && this.message_type == 3) reasons.put(
                                "offensive-speeck"
                            )
                            else if (dialogView.checkBoxReason1Report.isChecked && this.message_type == 4) reasons.put(
                                "offensive-language"
                            )
                            if (dialogView.checkBoxReason2Report.isChecked) reasons.put("grammar-or-spelling")
                            if (dialogView.checkBoxReason3Report.isChecked) reasons.put("different-language")
                            if (dialogView.checkBoxReason4Report.isChecked) reasons.put("difficult-pronounce")
                            var other_text = dialogView.textReasonOtherReport.text.toString()
                            if (dialogView.checkBoxReason5Report.isChecked) reasons.put(other_text)
                            if (this.message_type == 3) { //TODO fix this
                                //listen?.reportClip(reasons)
                            } else if (this.message_type == 4) {
                                //speak?.reportSentence(reasons)
                            }
                            alertDialog.dismiss()
                        }
                        buttonCancel.setOnClickListener {
                            alertDialog.dismiss()
                        }
                        setTheme(this.context!!, dialogView)
                    } catch (exception: Exception) {
                        println("!!-- Exception: MessageDialogActivity MD03 - Details: " + exception.toString() + " --!!")
                    }
                }
            }
        }
    }

    fun checkAvailability(view: View) {
        var checkBoxes = arrayOf(
            view.checkBoxReason1Report,
            view.checkBoxReason2Report,
            view.checkBoxReason3Report,
            view.checkBoxReason4Report,
            view.checkBoxReason5Report
        )
        var textReason = view.textReasonOtherReport
        var availableSendingTheReport = false
        if (checkBoxes[0].isChecked) availableSendingTheReport = true
        if (checkBoxes[1].isChecked) availableSendingTheReport = true
        if (checkBoxes[2].isChecked) availableSendingTheReport = true
        if (checkBoxes[3].isChecked) availableSendingTheReport = true
        if (checkBoxes[4].isChecked && textReason.text.toString()
                .replace(" ", "") != ""
        ) availableSendingTheReport = true

        var buttonSend = view.btnSendReport
        if (availableSendingTheReport) buttonSend.isEnabled = true
        else buttonSend.isEnabled = false
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

    fun checkDeleteButtonDeviceScreen(buttonDelete: Button) {
        if (this.width < 1500) {
            //if the screen is smaller than "1500" it hides the "Delete" button
            buttonDelete.isGone = true
        }
    }

    fun setTheme(view: Context, dialogView: View) {
        var theme: DarkLightTheme = DarkLightTheme()

        var isDark = theme.getTheme(view)
        when (this.message_type) {
            0, 2 -> {
                //standard message dialog
                if (this.message_type == 2) isDark = !isDark
                theme.setElement(
                    isDark,
                    dialogView.findViewById(R.id.layoutMessageDialog) as ConstraintLayout
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
                theme.setElement(
                    isDark,
                    dialogView.findViewById(R.id.layoutDailyGoal) as ConstraintLayout
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
                    dialogView.findViewById(R.id.btnDailyGoalSave) as Button
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
            3, 4 -> {
                theme.setElement(
                    isDark,
                    dialogView.findViewById(R.id.layoutReport) as ConstraintLayout
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.checkBoxReason1Report) as CheckBox
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.checkBoxReason2Report) as CheckBox
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.checkBoxReason3Report) as CheckBox
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.checkBoxReason4Report) as CheckBox
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.checkBoxReason5Report) as CheckBox
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.btnCancelReport) as Button
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.btnSendReport) as Button
                )
                theme.setElement(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.titleReportSentenceClip) as TextView,
                    R.color.colorBlack,
                    R.color.colorWhite
                )
                theme.setTextView(
                    isDark,
                    view,
                    dialogView.findViewById(R.id.textReasonOtherReport) as TextView
                )
            }
        }
    }
}