package org.commonvoice.saverio.ui.recyclerview.badges

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.commonvoice.saverio.R

class AchievementBadgeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val achievementText = itemView.findViewById<TextView>(R.id.badgesTextViewNumber)
    val achievementImage = itemView.findViewById<ImageView>(R.id.badgesImageViewListenSpeak)
}