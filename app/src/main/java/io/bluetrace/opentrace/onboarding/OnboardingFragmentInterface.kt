package io.bluetrace.opentrace.onboarding

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.button_and_progress.*

abstract class OnboardingFragmentInterface : Fragment() {
    abstract fun getButtonText(): String
    abstract fun onButtonClick(buttonView: View)
    abstract fun onUpdatePhoneNumber(num: String)
    abstract fun onError(error: String)

    private var actionButton: Button? = null

    abstract fun becomesVisible()

    abstract fun getProgressValue(): Int

    private fun setupButton() {
        buttonProgress?.let {
            actionButton = it
            it.text = getButtonText()
            it.setOnClickListener { buttonView ->
                onButtonClick(buttonView)
            }
        }
    }

    fun enableButton() {
        actionButton?.let {
            it.isEnabled = true
        }
    }

    fun disableButton() {
        actionButton?.let {
            it.isEnabled = false
        }
    }

    private fun setupProgress() {
        pbProgress.progress = getProgressValue()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButton()
        setupProgress()
    }


}
