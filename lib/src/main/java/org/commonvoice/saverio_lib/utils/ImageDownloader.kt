package org.commonvoice.saverio_lib.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import androidx.annotation.WorkerThread
import androidx.core.view.isGone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonvoice.saverio_lib.R
import java.net.URL

object ImageDownloader {

    @WorkerThread
    suspend fun loadImageIntoImageView(
        imageUrl: String?,
        imageView: ImageView
    ) = withContext(Dispatchers.IO) {
        val bitmap: Bitmap? = when {
            imageUrl == "null" || imageUrl == null -> {
                null
            }
            !imageUrl.contains("data:image/jpeg;base64,") -> {
                try {
                    val downloadUrl = if (imageUrl.contains("gravatar.com/avatar")) {
                        "$imageUrl?s=1000"
                    } else {
                        imageUrl
                    }

                    URL(downloadUrl).openStream()?.let {
                        BitmapFactory.decodeStream(it)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            else -> {
                try {
                    val base64Image = imageUrl.replace("data:image/jpeg;base64,", "")

                    val decodedImage = Base64.decode(base64Image, Base64.DEFAULT)

                    BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                } catch (e: Exception) {
                    null
                }
            }
        }

        withContext(Dispatchers.Main) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(R.drawable.ic_profileimagerobot)
            }

            imageView.isGone = false
        }
    }

}