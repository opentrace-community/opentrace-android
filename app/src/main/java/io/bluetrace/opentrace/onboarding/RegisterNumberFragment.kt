package io.bluetrace.opentrace.onboarding

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_register_number.*
import io.bluetrace.opentrace.Preference
import io.bluetrace.opentrace.R
import io.bluetrace.opentrace.TracerApp
import io.bluetrace.opentrace.Utils
import io.bluetrace.opentrace.logging.CentralLog

class RegisterNumberFragment : OnboardingFragmentInterface() {
    private var listener: OnFragmentInteractionListener? = null
    private val TAG: String = "RegisterNumberFragment"

    private var mView: View? = null

    override fun getButtonText(): String = "Get OTP"

    override fun becomesVisible() {
        CentralLog.d(TAG, "becomes visible")
        val myActivity = this as OnboardingFragmentInterface
        myActivity.enableButton()
    }

    override fun onButtonClick(buttonView: View) {
        CentralLog.d(TAG, "OnButtonClick")
        disableButtonAndRequestOTP()
    }

    override fun getProgressValue(): Int = 20

    private fun disableButtonAndRequestOTP() {
        var myactivity = this as OnboardingFragmentInterface
        myactivity.disableButton()
        requestOTP()
    }

    private fun requestOTP() {
        mView?.let { view ->
            phone_number_error.visibility = View.INVISIBLE
            var numberText = phone_number.text.toString().trim()

            val fullNumber = "${country_code.selectedCountryCodeWithPlus}${numberText}"

            CentralLog.d(TAG, "The value retrieved: ${fullNumber}")

            val onboardActivity = context as OnboardingActivity
            Preference.putPhoneNumber(TracerApp.AppContext, fullNumber)
            onboardActivity.updatePhoneNumber(fullNumber)
            onboardActivity.requestForOTP(fullNumber)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CentralLog.i(TAG, "View created")
        mView = view

        phone_number.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                phone_number_error.visibility = View.GONE
            }
        })

        phone_number.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                Utils.hideKeyboardFrom(view.context, view)
                disableButtonAndRequestOTP()
                true
            } else {
                false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        CentralLog.i(TAG, "Making view")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_number, container, false)
    }



    override fun onUpdatePhoneNumber(num: String) {
        CentralLog.d(TAG, "onUpdatePhoneNumber $num")
    }

    override fun onError(error: String) {

        phone_number_error.let {
            phone_number_error.visibility = View.VISIBLE
            phone_number_error.text = error
        }
        CentralLog.e(TAG, "error: ${error.toString()}")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        mView = null

        CentralLog.i(TAG, "Detached??")
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}
