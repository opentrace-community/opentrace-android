package au.gov.health.covidsafe.ui.upload.presentation

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.ui.PagerChildFragment
import au.gov.health.covidsafe.ui.UploadButtonLayout
import au.gov.health.covidsafe.ui.onboarding.fragment.enternumber.EnterNumberFragment
import au.gov.health.covidsafe.ui.view.UploadingDialog
import au.gov.health.covidsafe.ui.view.UploadingErrorDialog
import com.atlassian.mobilekit.module.core.utils.SystemUtils
import kotlinx.android.synthetic.main.fragment_verify_upload_pin.*
import kotlinx.android.synthetic.main.fragment_verify_upload_pin.view.*


class VerifyUploadPinFragment : PagerChildFragment() {

    interface OnUploadErrorInterface {
        fun onPositiveClicked()
        fun onNegativeClicked()
    }

    private var dialog: Dialog? = null

    private lateinit var presenter : VerifyUploadPinPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_verify_upload_pin, container, false)

    override val navigationIcon: Int? = R.drawable.ic_up
    override var stepProgress: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = VerifyUploadPinPresenter(this)
    }

    override fun onResume() {
        super.onResume()
        pin.onPinChanged = {
            updateButtonState()
            hideInvalidOtp()
        }
    }

    override fun onPause() {
        super.onPause()
        pin.onPinChanged = null
    }

    override fun getUploadButtonLayout() = UploadButtonLayout.ContinueLayout(R.string.action_verify_upload_pin) {
        presenter.uploadData(requireView().pin.value)
    }

    override fun updateButtonState() {
        if (isIncorrectPinFormat()) {
            disableContinueButton()
        } else {
            enableContinueButton()
        }
    }

    private fun isIncorrectPinFormat(): Boolean {
        return requireView().pin.isIncomplete
    }

    fun hideKeyboard() {
        activity?.currentFocus?.let { view ->
            SystemUtils.hideSoftKeyboard(view)
        }
    }

    fun showInvalidOtp() {
        dialog?.dismiss()
        enter_pin_error_label.visibility = View.VISIBLE
    }

    private fun hideInvalidOtp() {
        enter_pin_error_label.visibility = View.GONE
    }

    fun showGenericError() {
        dialog?.dismiss()
        activity?.let {
            dialog = UploadingErrorDialog(it, object : OnUploadErrorInterface {
                override fun onPositiveClicked() {
                    presenter.uploadData(requireView().pin.value)
                }

                override fun onNegativeClicked() {
                    dialog?.dismiss()
                }
            })
            dialog?.show()
        }
    }

    override fun onStop() {
        super.onStop()
        dialog?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        root.removeAllViews()
    }

    fun navigateToRegister() {
        val bundle = bundleOf(
                EnterNumberFragment.ENTER_NUMBER_DESTINATION_ID to R.id.action_enterPinFragment_to_uploadStepFourFragment)
        navigateTo(VerifyUploadPinFragmentDirections.actionVerifyUploadPinFragmentToEnterNumberFragment().actionId, bundle)
    }

    fun navigateToNextPage() {
        navigateTo(R.id.action_verifyUploadPinFragment_to_uploadFinishedFragment)
    }

    fun showDialogLoading() {
        dialog?.dismiss()
        dialog = UploadingDialog(requireActivity())
        dialog?.show()
    }

    fun showCheckInternetError() {
        dialog?.dismiss()
        dialog = AlertDialog.Builder(activity)
                .setMessage(R.string.generic_internet_error)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, null).show()
    }
}