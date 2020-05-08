package au.gov.health.covidsafe.extensions

import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import au.gov.health.covidsafe.R

fun TextView.toHyperlink(textToHyperLink: String? = null, onClick: () -> Unit) {
    val text = this.text
    val spannableString = SpannableString(text)
    val startIndex = if (textToHyperLink.isNullOrEmpty()) {
        0
    } else {
        text.indexOf(textToHyperLink)
    }
    val endIndex = if (textToHyperLink.isNullOrEmpty()) {
        spannableString.length
    } else {
        text.indexOf(textToHyperLink) + textToHyperLink.length
    }
    spannableString.setSpan(URLSpan(""), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
    this.setLinkTextColor(ContextCompat.getColor(context, R.color.dark_green))
    this.setOnClickListener {
        onClick.invoke()
    }

}