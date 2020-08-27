package org.commonvoice.saverio.ui.recyclerview.badges

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.commonvoice.saverio.R

class LevelBadgeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val levelText = itemView.findViewById<TextView>(R.id.levelNumber)
}