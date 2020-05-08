package au.gov.health.covidsafe.ui.home.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import au.gov.health.covidsafe.R
import kotlinx.android.synthetic.main.view_card_permission_card.view.*

class PermissionStatusCard @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_card_permission_card, this, true)

        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.PermissionStatusCard)
        val title = a.getString(R.styleable.PermissionStatusCard_permissionStatusCard_title)
        a.recycle()

        permission_title.text = title

        val height = context.resources.getDimensionPixelSize(R.dimen.permission_height)
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
    }

    fun render(text: String, correct: Boolean) {
        permission_icon.isSelected = correct
        permission_title.text = text
    }


}