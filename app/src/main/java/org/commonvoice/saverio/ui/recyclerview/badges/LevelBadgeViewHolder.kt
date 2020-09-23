package org.commonvoice.saverio.ui.recyclerview.badges

import androidx.recyclerview.widget.RecyclerView
import org.commonvoice.saverio.databinding.ViewholderBadgeLevelBinding

class LevelBadgeViewHolder(
    private val binding: ViewholderBadgeLevelBinding
): RecyclerView.ViewHolder(binding.root) {

    fun bind(level: Badge.Level) = binding.apply {
        levelNumber.text = level.levelNumber.toString()
        levelNumeral.text = when(level.levelNumber % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }

}