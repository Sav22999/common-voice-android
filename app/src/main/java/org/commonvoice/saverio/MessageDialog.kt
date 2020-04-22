package org.commonvoice.saverio

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import kotlinx.android.synthetic.main.daily_goal.view.*
import kotlinx.android.synthetic.main.message_dialog.view.*

class MessageDialog {
    private val REQUEST_CODE: Int = 101
    private val DAILY_GOAL = "DAILY_GOAL"
    private var message_type: Int =
        0 // 0->standard (Ok), 1->dailyGoal, 2->standard (Ok) JUST FOR THEME changing ("Dark theme turned on/off)
    private var message_text: String = ""
    private var message_title: String = ""
    private var message_details: String = ""
    private var context: Context? = null;
    private var dailyGoalValue: Int = 0
    private var width: Int = 0
    private var main: MainActivity? = null

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

    fun setMessageType(type: Int) {
        this.message_type = type
    }

    fun show() {
        if (this.context != null) {
            if (this.message_type == 0 || this.message_type == 2) {
                try {
                    val dialogView =
                        LayoutInflater.from(this.context).inflate(R.layout.message_dialog, null)
                    val builder = AlertDialog.Builder(this.context!!)
                        .setView(dialogView)
                        .setTitle(message_title)
                    //show dialog
                    val alertDialog = builder.show()
                    dialogView.labelTextMessageDialog.setText(this.message_text)
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
            } else if (this.message_type == 1) {
                println("Daily goal")
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
                        context!!.getSharedPreferences(DAILY_GOAL, REQUEST_CODE.toInt()).edit()
                            .putInt(DAILY_GOAL, this.dailyGoalValue).apply()
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
                        context!!.getSharedPreferences(DAILY_GOAL, REQUEST_CODE.toInt()).edit()
                            .putInt(DAILY_GOAL, 0).apply()
                        alertDialog.dismiss()
                        main?.refreshDailyGoalDataInDashboard()
                    }
                    setTheme(this.context!!, dialogView)
                } catch (exception: Exception) {
                    println("!!-- Exception: MessageDialogActivity MD01 - Details: " + exception.toString() + " --!!")
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

    fun checkDeleteButtonDeviceScreen(buttonDelete: Button) {
        if (this.width < 1500) {
            //if the screen is smaller than "1500" it hides the "Delete" button
            buttonDelete.isGone = true
        }
    }

    fun setTheme(view: Context, dialogView: View) {
        var theme: DarkLightTheme = DarkLightTheme()

        var isDark = theme.getTheme(view)
        if (this.message_type == 0 || this.message_type == 2) {
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
        } else if (this.message_type == 1) {
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
    }
}