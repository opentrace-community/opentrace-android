package au.gov.health.covidsafe.ui.onboarding.fragment.dataprivacy

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.ui.PagerChildFragment
import au.gov.health.covidsafe.ui.UploadButtonLayout
import kotlinx.android.synthetic.main.fragment_data_privacy.*
import kotlinx.android.synthetic.main.fragment_data_privacy.view.*

class DataPrivacyFragment : PagerChildFragment() {

    override val navigationIcon: Int? = R.drawable.ic_up
    override var stepProgress: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View? = inflater.inflate(R.layout.fragment_data_privacy, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.data_privacy_content.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun getUploadButtonLayout() = UploadButtonLayout.ContinueLayout(R.string.data_privacy_button) {
        navigateTo(DataPrivacyFragmentDirections.actionDataPrivacyToRegistrationConsentFragment().actionId)
    }

    override fun updateButtonState() {
        enableContinueButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        root.removeAllViews()
    }
}