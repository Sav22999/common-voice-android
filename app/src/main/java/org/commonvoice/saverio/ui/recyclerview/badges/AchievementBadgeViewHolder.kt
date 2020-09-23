package org.commonvoice.saverio.ui.recyclerview.badges

import androidx.recyclerview.widget.RecyclerView
import org.commonvoice.saverio.R
import org.commonvoice.saverio.databinding.ViewholderBadgeAchievementBinding
import org.commonvoice.saverio.utils.onClick

class AchievementBadgeViewHolder(
    private val binding: ViewholderBadgeAchievementBinding
): RecyclerView.ViewHolder(binding.root) {

    fun bind(achievement: Badge.SpeakAchievement) = binding.apply {
        badgesTextViewNumber.text = achievement.achievementText

        badgesImageViewListenSpeak.setImageResource(R.drawable.speak_cv)
    }

    fun bind(achievement: Badge.ListenAchievement) = binding.apply {
        badgesTextViewNumber.text = achievement.achievementText

        badgesImageViewListenSpeak.setImageResource(R.drawable.listen_cv)
    }

    fun registerOnClick(onClick: (Badge) -> Unit, badge: Badge) {
        binding.root.onClick {
            onClick(badge)
        }
    }

}