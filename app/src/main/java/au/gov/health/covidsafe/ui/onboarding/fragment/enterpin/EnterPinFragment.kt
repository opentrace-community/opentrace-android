package au.gov.health.covidsafe.ui.onboarding.fragment.enterpin

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NavigationRes
import androidx.core.content.ContextCompat
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.extensions.toHyperlink
import au.gov.health.covidsafe.ui.PagerChildFragment
import au.gov.health.covidsafe.ui.UploadButtonLayout
import com.atlassian.mobilekit.module.core.utils.SystemUtils
import kotlinx.android.synthetic.main.fragment_enter_pin.*
import kotlinx.android.synthetic.main.fragment_enter_pin.view.*
import kotlin.math.floor


class EnterPinFragment : PagerChildFragment() {

    companion object {
        const val ENTER_PIN_SESSION = "session"
        const val ENTER_PIN_CHALLENGE_NAME = "challenge_name"
        const val ENTER_PIN_PHONE_NUMBER = "phone_number"
        const val ENTER_PIN_DESTINATION_ID = "destination_id"
        const val ENTER_PIN_PROGRESS = "progress"
    }

    override val navigationIcon = R.drawable.ic_up
    override var stepProgress: Int? = 3

    private val COUNTDOWN_DURATION = 5 * 60L  // OTP Code expiry

    private var alertDialog: AlertDialog? = null
    private var stopWatch: CountDownTimer? = null
    private lateinit var presenter: EnterPinPresenter
    @NavigationRes
    private var destinationId: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View? = inflater.inflate(R.layout.fragment_enter_pin, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val session = it.getString(ENTER_PIN_SESSION)
            val challengeName = it.getString(ENTER_PIN_CHALLENGE_NAME)
            val phoneNumber = it.getString(ENTER_PIN_PHONE_NUMBER)
            destinationId = it.getInt(ENTER_PIN_DESTINATION_ID)
            stepProgress = if (it.containsKey(ENTER_PIN_PROGRESS)) it.getInt(ENTER_PIN_PROGRESS) else null
            enter_pin_headline.text = resources.getString(R.string.enter_pin_headline, resources.getString(R.string.enter_number_prefix), phoneNumber)
            presenter = EnterPinPresenter(this@EnterPinFragment,
                    session,
                    challengeName,
                    phoneNumber)
        }

        enter_pin_wrong_number.toHyperlink {
            popBackStack()
        }

        enter_pin_resend_pin.toHyperlink {
            presenter.resendCode()
        }

        view.pin_issue.movementMethod = LinkMovementMethod.getInstance()

        startTimer()
    }

    override fun onResume() {
        super.onResume()
        updateButtonState()
        pin.onPinChanged = {
            updateButtonState()
            hideInvalidOtp()
        }
    }

    override fun onPause() {
        super.onPause()
        pin.onPinChanged = null
    }

    private fun startTimer() {
        stopWatch = object : CountDownTimer(COUNTDOWN_DURATION * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val numberOfMins = floor((millisUntilFinished * 1.0) / 60000)
                val numberOfMinsInt = numberOfMins.toInt()
                val numberOfSeconds = floor((millisUntilFinished / 1000.0) % 60)
                val numberOfSecondsInt = numberOfSeconds.toInt()
                val finalNumberOfSecondsString = if (numberOfSecondsInt < 10) {
                    "0$numberOfSecondsInt"
                } else {
                    "$numberOfSecondsInt"
                }

                enter_pin_timer_value?.text = "$numberOfMinsInt:$finalNumberOfSecondsString"
            }

            override fun onFinish() {
                enter_pin_timer_value?.text = "0:00"
                enter_pin_resend_pin.isEnabled = true
                activity?.let {
                    enter_pin_resend_pin.setLinkTextColor(ContextCompat.getColor(it, R.color.hyperlink_enabled))
                }
            }
        }
        stopWatch?.start()
        enter_pin_resend_pin.isEnabled = false
        activity?.let {
            enter_pin_resend_pin.setLinkTextColor(ContextCompat.getColor(it, R.color.hyperlink_disabled))
        }
    }

    fun resetTimer() {
        stopWatch?.cancel()
        startTimer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopWatch?.cancel()
        alertDialog?.dismiss()
        enter_pin_resend_pin.setOnClickListener(null)
        enter_pin_wrong_number.setOnClickListener(null)
        root.removeAllViews()
    }

    fun hideKeyboard() {
        activity?.currentFocus?.let { view ->
            SystemUtils.hideSoftKeyboard(view)
        }
    }

    fun showInvalidOtp() {
        enter_pin_error_label.visibility = View.VISIBLE
    }

    private fun hideInvalidOtp() {
        enter_pin_error_label.visibility = View.GONE
    }

    fun showGenericError() {
        alertDialog?.dismiss()
        alertDialog = AlertDialog.Builder(activity)
                .setMessage(R.string.generic_error)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, null).show()
    }

    private fun isIncorrectPinFormat(): Boolean {
        return requireView().pin.isIncomplete
    }

    override fun updateButtonState() {
        if (isIncorrectPinFormat()) {
            disableContinueButton()
        } else {
            enableContinueButton()
        }
    }

    private fun validateOtp() {
        presenter.validateOTP(requireView().pin.value)
    }

    fun showErrorOtpMustBeSixDigits() {

    }

    fun navigateToNextPage() {
        navigateTo(destinationId ?: R.id.action_otpFragment_to_permissionFragment)
    }

    override fun getUploadButtonLayout() = UploadButtonLayout.ContinueLayout(R.string.enter_pin_button) {
        validateOtp()
    }

    fun showCheckInternetError() {
        alertDialog?.dismiss()
        alertDialog = AlertDialog.Builder(activity)
                .setMessage(R.string.generic_internet_error)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, null).show()
    }

}