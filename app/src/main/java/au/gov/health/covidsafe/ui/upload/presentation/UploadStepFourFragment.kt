package au.gov.health.covidsafe.ui.upload.presentation

import android.app.AlertDialog
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.ui.PagerChildFragment
import au.gov.health.covidsafe.ui.UploadButtonLayout
import kotlinx.android.synthetic.main.fragment_upload_page_4.*

class UploadStepFourFragment : PagerChildFragment() {

    private var alertDialog: AlertDialog? = null
    override var stepProgress: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_upload_page_4, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subHeader.movementMethod = LinkMovementMethod.getInstance()
    }


    override fun onResume() {
        super.onResume()
        upload_consent_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            updateButtonState()
        }
    }
    override fun updateButtonState() {
        if (upload_consent_checkbox.isChecked) {
            enableContinueButton()
        } else {
            disableContinueButton()
        }
    }

    override val navigationIcon: Int? = R.drawable.ic_up

    override fun getUploadButtonLayout() = UploadButtonLayout.ContinueLayout(
            R.string.action_agree) {
        navigateToVerifyUploadPin()
    }

    private fun navigateToVerifyUploadPin() {
        navigateTo(R.id.action_uploadStepFourFragment_to_verifyUploadPinFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        alertDialog?.dismiss()
        root.removeAllViews()
    }

}