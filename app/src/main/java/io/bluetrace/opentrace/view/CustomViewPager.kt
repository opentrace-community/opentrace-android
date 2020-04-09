package io.bluetrace.opentrace.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class CustomViewPager(
    context: Context?,
    attrs: AttributeSet?
) : ViewPager(context!!, attrs) {
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (isEnabled) {
            super.onTouchEvent(event)
        } else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (isEnabled) {
            super.onInterceptTouchEvent(event)
        } else false
    }

    fun setPagingEnabled(enabled: Boolean) {
        this.isEnabled = enabled
    }

}
