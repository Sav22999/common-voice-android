package org.commonvoice.saverio.ui.dialogs.specificDialogs

import android.graphics.Typeface
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import com.github.mrindeciso.advanced_dialogs.customDialog.CustomDialogInterface
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.DialogDailyGoalBinding
import org.commonvoice.saverio.utils.onClick
import org.commonvoice.saverio_lib.preferences.MainPrefManager
import org.commonvoice.saverio_lib.preferences.StatsPrefManager
import timber.log.Timber

class DailyGoalDialog(
    private val mainPrefManager: MainPrefManager,
    private val statsPrefManager: StatsPrefManager,
    private val refreshDashboard: () -> Unit,
) : CustomDialogInterface<DialogDailyGoalBinding>(
    makeBackgroundTransparent = true,
    preserveLateralMargin = true,
) {

    private var forcedValue = false

    private var dailyGoalValue = statsPrefManager.dailyGoalObjective

    override fun render(inflater: LayoutInflater): DialogDailyGoalBinding {
        return DialogDailyGoalBinding.inflate(inflater).apply binding@ {
            imageSeekbarMinusDailygoal.onClick {
                forcedValue = true
                seekDailyGoalValue.progress -= 1
            }

            imageSeekbarPlusDailygoal.onClick {
                forcedValue = true
                seekDailyGoalValue.progress += 1
            }

            btnDailyGoalCancel.onClick {
                dismiss()
                refreshDashboard()
            }

            btnDailyGoalDelete.onClick {
                statsPrefManager.dailyGoalObjective = 0
                dismiss()
                refreshDashboard()
            }

            btnDailyGoalSave.onClick {
                statsPrefManager.dailyGoalObjective = dailyGoalValue
                dismiss()
                refreshDashboard()
            }

            seekDailyGoalValue.apply {
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        dailyGoalValue = progress
                        renderDailyGoal(this@binding)
                        forcedValue = false
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        //Empty
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        //Empty
                    }
                })
                forcedValue = true
                progress = dailyGoalValue
            }

            if (seekDailyGoalValue.progress == 0) {
                labelDailyGoalValue.setText(R.string.daily_goal_is_not_set)
                labelDailyGoalValue.textSize = mainPrefManager.textSize * 22f
                labelDailyGoalValue.typeface = Typeface.DEFAULT
            }
        }
    }

    private fun renderDailyGoal(binding: DialogDailyGoalBinding, force: Boolean = forcedValue) = binding.apply {
        val valueToUse = if (force) {
            dailyGoalValue
        } else {
            dailyGoalValue - dailyGoalValue % 5
        }

        Timber.i(valueToUse.toString())

        if (valueToUse == 0) {
            labelDailyGoalValue.setText(R.string.daily_goal_is_not_set)
            labelDailyGoalValue.textSize = mainPrefManager.textSize * 22f
            labelDailyGoalValue.typeface = Typeface.DEFAULT
        } else {
            labelDailyGoalValue.text = valueToUse.toString()
            labelDailyGoalValue.typeface = ResourcesCompat.getFont(
                labelDailyGoalValue.context,
                R.font.sourcecodepro
            )
            labelDailyGoalValue.textSize = mainPrefManager.textSize * 30f
        }

        dailyGoalValue = valueToUse
    }

}