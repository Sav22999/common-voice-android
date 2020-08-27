package org.commonvoice.saverio.ui.recyclerview.badges

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.commonvoice.saverio.R

class BadgeAdapter(
    private val data: List<Badge>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(Type.values()[viewType]) {
            Type.LEVEL -> {
                LevelBadgeViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.viewholder_badge_level, parent, false)
                )
            }
            Type.ACHIEVEMENT -> {
                AchievementBadgeViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.viewholder_badge_achievement, parent, false)
                )
            }
            else -> {
                throw RuntimeException("Unknown ViewHolder type")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is LevelBadgeViewHolder -> {
                holder.apply {
                    (data[position] as Badge.Level).let {
                        levelText.text = it.levelNumber.toString()
                    }
                }
            }
            is AchievementBadgeViewHolder -> {
                holder.apply {
                    (data[position] as Badge.Achievement).let {
                        achievementText.text = it.achievementText
                        achievementImage.setImageResource(it.achievementImage)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return when(data[position]) {
            is Badge.Level -> Type.LEVEL.ordinal
            is Badge.Achievement -> Type.ACHIEVEMENT.ordinal
            else -> Type.BAD.ordinal
        }
    }

    private enum class Type {
        LEVEL,
        ACHIEVEMENT,
        BAD,
    }

}