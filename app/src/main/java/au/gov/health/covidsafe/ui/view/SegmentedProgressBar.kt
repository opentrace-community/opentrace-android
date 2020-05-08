package au.gov.health.covidsafe.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import au.gov.health.covidsafe.R

class SegmentedProgressBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = -1) : LinearLayout(context, attrs, defStyle) {
    private val maxValue: Int
    var progress: Int = DEFAULT_PROGRESS
        set(value) {
            field = value
            for (i in 0..childCount) {
                val segment = getChildAt(i)
                segment?.isSelected = i + 1 <= progress
            }
        }

    private val segmentSpacing: Int

    init {
        orientation = HORIZONTAL
        val values = context.obtainStyledAttributes(attrs, R.styleable.SegmentedProgressBar, defStyle, 0)
        maxValue = values.getInt(R.styleable.SegmentedProgressBar_progress_max_value, DEFAULT_MAX_VALUE)
        segmentSpacing = values.getDimensionPixelSize(R.styleable.SegmentedProgressBar_segment_spacing,
                DEFAULT_SEGMENT_SPACING_DP * resources.displayMetrics.density.toInt())
        progress = values.getInt(R.styleable.SegmentedProgressBar_progress_value, DEFAULT_PROGRESS)
        drawProgress()
        values.recycle()
    }

    private fun drawProgress() {
        repeat(maxValue) { index ->
            val lp = generateDefaultLayoutParams()
            lp.height = LayoutParams.WRAP_CONTENT
            lp.width = 0
            lp.weight = 1.0f
            lp.rightMargin = if (index in 1 until maxValue - 1) segmentSpacing else 0
            lp.leftMargin = if (index > 0) segmentSpacing else 0

            val view = View(context)
            view.background = ContextCompat.getDrawable(context, R.drawable.progress_segment)
            view.isSelected = index + 1 <= progress
            addView(view, lp)
        }
    }
}

private const val DEFAULT_MAX_VALUE = 5
private const val DEFAULT_PROGRESS = 0
private const val DEFAULT_SEGMENT_SPACING_DP = 4