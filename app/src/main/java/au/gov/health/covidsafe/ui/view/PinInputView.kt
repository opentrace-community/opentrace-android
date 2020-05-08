package au.gov.health.covidsafe.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import kotlinx.android.synthetic.main.view_pin.view.*
import au.gov.health.covidsafe.R

class PinInputView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = -1) : ConstraintLayout(context, attrs, defStyle) {

    private val pinOne: EditText? by lazy { pin_1 }
    private val pinTwo: EditText? by lazy { pin_2 }
    private val pinThree: EditText? by lazy { pin_3 }
    private val pinFour: EditText? by lazy { pin_4 }
    private val pinFive: EditText? by lazy { pin_5 }
    private val pinSix: EditText? by lazy { pin_6 }
    var onPinChanged: (() -> Unit)? = null

    private val allInputs by lazy {
        listOf(pinOne, pinTwo, pinThree, pinFour, pinFive, pinSix)
    }

    val value: String
        get() = allInputs.mapNotNull { it?.text }.joinToString("")

    val isIncomplete: Boolean
        get() = allInputs.any { it?.text.isNullOrEmpty() }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pin, this, true)
        pinOne?.onDigitChanged(pinTwo)
        pinOne?.onDeletePressed(null)

        pinTwo?.onDigitChanged(pinThree)
        pinTwo?.onDeletePressed(pinOne)

        pinThree?.onDigitChanged(pinFour)
        pinThree?.onDeletePressed(pinTwo)

        pinFour?.onDigitChanged(pinFive)
        pinFour?.onDeletePressed(pinThree)

        pinFive?.onDigitChanged(pinSix)
        pinFive?.onDeletePressed(pinFour)

        pinSix?.onDigitChanged(null)
        pinSix?.onDeletePressed(pinFive)
    }

    private fun EditText.onDigitChanged(next: EditText? = null) {
        doAfterTextChanged {
            if (it?.length == 1) {
                next?.requestFocus()
                onPinChanged?.invoke()
            } else if (it.isNullOrBlank()) {
                onPinChanged?.invoke()
            }
        }
    }

    private fun EditText.onDeletePressed(prev: EditText? = null) {
        setOnKeyListener { view, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_DEL && text.isNullOrEmpty()) {
                prev?.requestFocus()
                onPinChanged?.invoke()
                true
            } else {
                false
            }
        }
    }
}
