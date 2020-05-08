package au.gov.health.covidsafe.ui.home.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import au.gov.health.covidsafe.R
import kotlinx.android.synthetic.main.view_card_external_link_card.view.*

class ExternalLinkCard @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_card_external_link_card, this, true)

        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ExternalLinkCard)
        val icon = a.getDrawable(R.styleable.ExternalLinkCard_external_linkCard_icon)
        val title = a.getString(R.styleable.ExternalLinkCard_external_linkCard_title)
        val content = a.getString(R.styleable.ExternalLinkCard_external_linkCard_content)
        val padding = a.getDimension(R.styleable.ExternalLinkCard_external_linkCard_icon_padding, 0f).toInt()
        val iconBackground = a.getResourceId(R.styleable.ExternalLinkCard_external_linkCard_icon_background, R.color.transparent)

        external_link_round_image.setImageDrawable(icon)
        external_link_round_image.setBackgroundResource(iconBackground)
        external_link_round_image.setPadding(padding, padding, padding, padding)
        external_link_headline.text = title
        external_link_content.text = content
        a.recycle()
    }
}