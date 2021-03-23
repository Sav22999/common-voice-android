package org.commonvoice.saverio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import kotlinx.android.synthetic.main.message_dialog.view.*
import kotlinx.android.synthetic.main.report_bugs_message.view.*
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.koin.core.KoinComponent
import org.koin.core.inject

class MessageDialog : KoinComponent {
    private var message_type: Int =
        0 /*0->standard (Ok), 1->setDailyGoal, 2->standard (Ok) JUST FOR THEME changing ("Dark theme turned on/off),
            3->reportClip (listen), 4->reportSentence (Speak)
            5->info, 6->help, 7->warning, 8->news/changelog, 9->tip
            10->offlineModeMessage, 11->report bug on website
            12->daily goal achieved, 15->show in-app username
            13-> (from Speak) , 14-> (from Listen)*/
    private var message_text: String = ""
    private var message_title: String = ""
    private var message_details: String = ""
    private var context: Context? = null
    private var height: Int = 0
    private var main: MainActivity? = null
    private var listen: ListenActivity? = null
    private var speak: SpeakActivity? = null
    lateinit var clipboardManager: ClipboardManager
    lateinit var clipData: ClipData

    private val theme by inject<DarkLightTheme>() //TODO change this if we want to switch to Dagger-Hilt
    private val mainPrefManager by inject<MainPrefManager>()

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

    fun setMainActivity(main: MainActivity) {
        this.main = main
    }

    fun setListenActivity(listen: ListenActivity) {
        this.listen = listen
        this.speak = null
    }

    fun show() {
        if (this.context != null) {
            when (this.message_type) {
                0, 2, 5, 6, 7, 8, 9, 13, 14, 15 -> {
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
                        if (message_type == 15) {
                            //set "copy" message
                            dialogView.imageCopyText.isGone = false
                            dialogView.imageCopyText.setOnClickListener {
                                clipboardManager.setPrimaryClip(clipData)
                                Toast.makeText(
                                    this.context,
                                    R.string.copie_string,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        dialogView.btnOkMessageDialog.setOnClickListener {
                            //dismiss dialog
                            alertDialog.dismiss()

                            if (message_type == 13) {
                                speak?.onBackPressed()
                            } else if (message_type == 14) {
                                listen?.onBackPressed()
                            }
                        }
                        setTheme(this.context!!, dialogView)
                        setMessageType(this.context!!, dialogView)
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
            }
        }
    }

    private fun setMessageType(view: Context, dialogView: View) {
        if (this.message_type == 0 || this.message_type == 1 || this.message_type == 2 || this.message_type == 3 || this.message_type == 4 || this.message_type == 13 || this.message_type == 14) {
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
        dialogView.textMessageType.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            30F * mainPrefManager.textSize
        )
    }

    fun setTheme(view: Context, dialogView: View) {
        when (this.message_type) {
            0, 2, 5, 6, 7, 8, 9, 13, 14, 15 -> {
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
                    invert = message_type == 2,
                    textSize = 15F
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

                if (this.message_type == 15) {
                    theme.setElement(
                        dialogView.findViewById(R.id.imageCopyText) as ImageView,
                        R.drawable.ic_copy,
                        R.drawable.ic_copy
                    )
                }
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
            }
        }
    }
}