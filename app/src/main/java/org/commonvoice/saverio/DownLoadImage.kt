package org.commonvoice.saverio

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Build
import android.widget.ImageView
import androidx.core.view.isGone
import java.net.URL
import java.util.*


// Class to download an image from url and display it into an image view
class DownLoadImage(private val imageView: ImageView) :
    AsyncTask<String, Void, Bitmap?>() {

    private var noImagePassed = false

    override fun doInBackground(vararg urls: String): Bitmap? {
        var urlOfImage = urls[0]
        println(urlOfImage)
        if (urlOfImage == "null") {
            this.noImagePassed = true
            return null
        } else if (!urlOfImage.contains("data:image/jpeg;base64,")) {
            return try {
                if (urlOfImage.contains("gravatar.com/avatar")) {
                    urlOfImage += "?s=1000"
                }
                val inputStream = URL(urlOfImage).openStream()
                val bitMap = BitmapFactory.decodeStream(inputStream)
                //println(bitMap)
                bitMap
            } catch (e: Exception) {
                //println("Exception: "+e.toString())
                //e.printStackTrace()
                null
            }
        } else {
            urlOfImage = urlOfImage.replace("data:image/jpeg;base64,", "")
            return try {
                if (Build.VERSION.SDK_INT >= 26) {
                    val imageAsBytes: ByteArray = Base64.getDecoder().decode(urlOfImage)
                    val bitMap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
                    //println(bitMap.toString())
                    bitMap
                } else {
                    println("Device not supported")
                    null
                }
            } catch (e: Exception) {
                //println("Exception: "+e.toString())
                null
            }
        }
    }

    override fun onPostExecute(result: Bitmap?) {
        if (result != null || !this.noImagePassed) {
            // Successful
            imageView.setImageBitmap(result)
            imageView.isGone = false

            //println("Successful")
        } else if (this.noImagePassed) {
            imageView.setImageResource(R.drawable.ic_profileimagerobot)
            imageView.isGone = false
        } else {
            // Error

            //println("Error")
        }
    }
}