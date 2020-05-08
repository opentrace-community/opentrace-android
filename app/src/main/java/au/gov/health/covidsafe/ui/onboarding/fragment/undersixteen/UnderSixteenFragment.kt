package au.gov.health.covidsafe.ui.onboarding.fragment.undersixteen

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.ui.PagerChildFragment
import au.gov.health.covidsafe.ui.UploadButtonLayout
import au.gov.health.covidsafe.ui.onboarding.fragment.enternumber.EnterNumberFragment
import kotlinx.android.synthetic.main.fragment_under_sixteen.*

class UnderSixteenFragment : PagerChildFragment() {

    override val navigationIcon: Int? = R.drawable.ic_up

    override var stepProgress: Int? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View? = inflater.inflate(R.layout.fragment_under_sixteen, container, false)

    override fun onResume() {
        super.onResume()
        under_sixteen_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            updateButtonState()
        }
    }

    override fun onPause() {
        super.onPause()
        under_sixteen_checkbox.setOnCheckedChangeListener(null)
    }

    override fun getUploadButtonLayout(): UploadButtonLayout = UploadButtonLayout.ContinueLayout(R.string.under_sixteen_button) {
        val bundle = bundleOf(
                EnterNumberFragment.ENTER_NUMBER_DESTINATION_ID to R.id.action_otpFragment_to_permissionFragment,
                EnterNumberFragment.ENTER_NUMBER_PROGRESS to 2)
        navigateTo(UnderSixteenFragmentDirections.actionUnderSixteenFragmentToEnterNumberFragment().actionId, bundle)
    }

    override fun updateButtonState() {
        if (under_sixteen_checkbox.isChecked) {
            enableContinueButton()
        } else {
            disableContinueButton()
        }
    }
}