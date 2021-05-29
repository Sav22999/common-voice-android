package org.commonvoice.saverio.utils

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

abstract class OnSwipeTouchListener(ctx: Context) : View.OnTouchListener {

    private val gestureDetector: GestureDetector

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    init {
        gestureDetector = GestureDetector(ctx, GestureListener())
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        try {
            return gestureDetector.onTouchEvent(event)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return false
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        result = true
                    }
                } else if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom()
                    } else {
                        onSwipeTop()
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            onFling()
            return result
        }

        override fun onLongPress(e: MotionEvent?) {
            onLongPress();
            super.onLongPress(e)
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            onDoubleTap();
            return super.onDoubleTap(e)
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            var type = ""
            val diffY = e2!!.y - e1!!.y
            val diffX = e2!!.x - e1!!.x
            if (abs(diffX) > abs(diffY)) {
                if (diffX > 0) {
                    type = "r"//right
                } else {
                    type = "l"//left
                }
            } else {
                if (diffY > 0) {
                    type = "d"//down
                } else {
                    type = "u"//up
                }
            }
            onScroll(type);
            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onShowPress(e: MotionEvent?) {
            onShowPress()
            super.onShowPress(e)
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            onSingleTap()
            return super.onSingleTapConfirmed(e)
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            onSingleTapUp()
            return super.onSingleTapUp(e)
        }
    }

    open fun onSingleTapUp() {}

    open fun onSingleTap() {}

    open fun onShowPress() {}

    open fun onScroll(scrollTo: String = "") {}

    open fun onFling() {}

    open fun onLongPress() {}

    open fun onDoubleTap() {}

    open fun onSwipeRight() {}

    open fun onSwipeLeft() {}

    open fun onSwipeTop() {}

    open fun onSwipeBottom() {}

}