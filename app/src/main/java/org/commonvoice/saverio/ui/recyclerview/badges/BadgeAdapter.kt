package org.commonvoice.saverio.ui.recyclerview.badges

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.commonvoice.saverio.R

class BadgeAdapter(
    private val data: List<BadgeData>
): RecyclerView.Adapter<BadgeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        return BadgeViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.viewholder_badge, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        holder.apply {
            badgeBackground.imageTintList = ColorStateList.valueOf(
                data[position].backgroundTint
            )

            badgeLevel.text = data[position].badgeValue

            badgeDescription.text = data[position].descriptionText

            badgeDescriptionImage.isVisible = data[position].descriptionImage != null
            data[position].descriptionImage?.let {
                badgeDescriptionImage.setImageResource(it)
            }

            itemView.alpha = if (data[position].unlocked) 1.0f else 0.2f
        }
    }

    override fun getItemCount(): Int = data.size

}