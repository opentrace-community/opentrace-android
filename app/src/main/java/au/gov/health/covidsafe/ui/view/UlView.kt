package au.gov.health.covidsafe.ui.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import au.gov.health.covidsafe.R
import kotlinx.android.synthetic.main.view_ul.view.*

class UlView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_ul, this, true)

        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.UlView)
        val title = a.getString(R.styleable.UlView_ul_view_text)

        ul_content.text = title

        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        a.recycle()
    }
}