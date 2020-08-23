package org.commonvoice.saverio.ui.recyclerview.badges

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.commonvoice.saverio.R

class BadgeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val badgeBackground = itemView.findViewById<ImageView>(R.id.badgeBackground)
    val badgeLevel = itemView.findViewById<TextView>(R.id.badgesTextViewNumber)
    val badgeDescription = itemView.findViewById<TextView>(R.id.badgesTextViewDescription)
    val badgeDescriptionImage = itemView.findViewById<ImageView>(R.id.badgesImageViewListenSpeak)
}