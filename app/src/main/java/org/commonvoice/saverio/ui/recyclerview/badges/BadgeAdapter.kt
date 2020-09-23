package org.commonvoice.saverio.ui.recyclerview.badges

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.commonvoice.saverio.databinding.ViewholderBadgeAchievementBinding
import org.commonvoice.saverio.databinding.ViewholderBadgeLevelBinding
import org.commonvoice.saverio.utils.inflateBinding

class BadgeAdapter(
    private val data: List<Badge>,
    private val onBadgeClick: (Badge) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(Type.values()[viewType]) {
            Type.LEVEL -> {
                LevelBadgeViewHolder(
                    parent.inflateBinding(ViewholderBadgeLevelBinding::inflate)
                )
            }
            Type.ACHIEVEMENT -> {
                AchievementBadgeViewHolder(
                    parent.inflateBinding(ViewholderBadgeAchievementBinding::inflate)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is LevelBadgeViewHolder -> {
                holder.bind(data[position] as Badge.Level)
                holder.registerOnClick(onBadgeClick, data[position])
            }
            is AchievementBadgeViewHolder -> {
                (data[position] as? Badge.SpeakAchievement)?.let {
                    holder.bind(it)
                }
                (data[position] as? Badge.ListenAchievement)?.let {
                    holder.bind(it)
                }
                holder.registerOnClick(onBadgeClick, data[position])
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return when(data[position]) {
            is Badge.Level -> Type.LEVEL.ordinal
            is Badge.SpeakAchievement -> Type.ACHIEVEMENT.ordinal
            is Badge.ListenAchievement -> Type.ACHIEVEMENT.ordinal
        }
    }

    private enum class Type {
        LEVEL,
        ACHIEVEMENT,
    }

}