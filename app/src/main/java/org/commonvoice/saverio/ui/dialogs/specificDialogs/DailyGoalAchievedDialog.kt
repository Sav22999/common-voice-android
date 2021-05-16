package org.commonvoice.saverio.ui.dialogs.specificDialogs

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import com.github.mrindeciso.advanced_dialogs.customDialog.CustomDialogInterface
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.DialogDailyGoalAchievedBinding
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.dataClasses.DailyGoal

class DailyGoalAchievedDialog(
    private val context: Activity,
    private val dailyGoal: DailyGoal,
) : CustomDialogInterface<DialogDailyGoalAchievedBinding>(
    makeBackgroundTransparent = true
) {

    override fun render(inflater: LayoutInflater): DialogDailyGoalAchievedBinding {
        return DialogDailyGoalAchievedBinding.inflate(inflater).also {
            it.labelTextMessageDialogDailyAchieved.text =
                context.getString(R.string.daily_goal_achieved_message).replace(
                    "{{n_clips}}",
                    "${dailyGoal.validations}"
                ).replace(
                    "{{n_sentences}}",
                    "${dailyGoal.recordings}"
                )

            it.btnOkMessageDialogDailyAchieved.onClick {
                dismiss()
            }

            it.btnShareMessageDialogDailyAchieved.onClick {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "type/palin"
                val textToShare = context.getString(R.string.share_daily_goal_text_on_social).replace(
                    "{{link}}",
                    "https://bit.ly/2XhnO7h"
                )
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, textToShare)
                context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_daily_goal_title)))
                dismiss()
            }
        }
    }

}