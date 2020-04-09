package io.bluetrace.opentrace.onboarding

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.text.HtmlCompat
import kotlinx.android.synthetic.main.fragment_otp.*
import io.bluetrace.opentrace.Preference
import io.bluetrace.opentrace.R
import io.bluetrace.opentrace.Utils
import io.bluetrace.opentrace.logging.CentralLog
import kotlin.math.floor

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class OTPFragment : OnboardingFragmentInterface() {
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private val TAG: String = "OTPFragment"
    private val COUNTDOWN_DURATION = 60L
    private var stopWatch: CountDownTimer? = null

    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            startTimer()
        } else {
            resetTimer()
        }
    }

    private fun resetTimer() {
        stopWatch?.cancel()
    }

    override fun getButtonText(): String = "Verify"

    override fun becomesVisible() {}

    override fun onButtonClick(view: View) {
        CentralLog.d(TAG, "OnButtonClick 3B")

        val otp = getOtp()
        val onboardActivity = context as OnboardingActivity
        onboardActivity.validateOTP(otp)
    }

    override fun getProgressValue(): Int = 40

    private fun getOtp(): String = simpleOTP.text.toString()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_otp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sent_to.text = HtmlCompat.fromHtml(
            getString(
                R.string.otp_sent,
                "<b>${Preference.getPhoneNumber(context!!)}</b>"
            ), HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        wrongNumber.setOnClickListener {
            CentralLog.d(TAG, "Wrong number pressed")
            val onboardingActivity = activity as OnboardingActivity
            onboardingActivity.navigateToPreviousPage()
        }

        resendCode.setOnClickListener {
            CentralLog.d(TAG, "resend pressed")
            val onboardingActivity = activity as OnboardingActivity
            onboardingActivity.resendCode(phoneNumber)
            resetTimer()
            startTimer()
        }

        simpleOTP.addTextChangedListener(object : TextWatcher {
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
                onError("")
                if (s.length == 6) {
                    Utils.hideKeyboardFrom(view.context, view)
                }
            }
        })

        simpleOTP.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                Utils.hideKeyboardFrom(view.context, view)
                val otp = getOtp()
                val onboardActivity = context as OnboardingActivity
                onboardActivity.validateOTP(otp)
                true
            } else {
                false
            }
        }
    }

    override fun onUpdatePhoneNumber(num: String) {
        CentralLog.d(TAG, "onUpdatePhoneNumber $num")
        sent_to.text = HtmlCompat.fromHtml(
            getString(R.string.otp_sent, "<b>${num}</b>"),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        phoneNumber = num
    }

    private fun startTimer() {
        stopWatch = object : CountDownTimer(COUNTDOWN_DURATION * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val numberOfMins = floor((millisUntilFinished * 1.0) / 60000)
                val numberOfMinsInt = numberOfMins.toInt()
                val numberOfSeconds = floor((millisUntilFinished / 1000.0) % 60)
                val numberOfSecondsInt = numberOfSeconds.toInt()
                var finalNumberOfSecondsString = ""
                if (numberOfSecondsInt < 10) {
                    finalNumberOfSecondsString = "0$numberOfSecondsInt"
                } else {
                    finalNumberOfSecondsString = "$numberOfSecondsInt"
                }

                timer?.text = "$numberOfMinsInt:$finalNumberOfSecondsString"
            }

            override fun onFinish() {
                timer?.text = "0:00"
                resendCode.isEnabled = true
                resendCode.setTextColor(Color.parseColor("#003DB5"))
            }
        }
        stopWatch?.start()
        resendCode.isEnabled = false
        resendCode.setTextColor(Color.parseColor("#DDDDDD"))
    }

    override fun onError(error: String) {
        tv_error.text = error
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }

    }

    override fun onDetach() {
        super.onDetach()
        listener = null

    }

    override fun onDestroy() {
        super.onDestroy()
        stopWatch?.cancel()
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}
