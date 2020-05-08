package au.gov.health.covidsafe.ui.onboarding.fragment.registrationcontent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.ui.PagerChildFragment
import au.gov.health.covidsafe.ui.PagerContainer
import au.gov.health.covidsafe.ui.UploadButtonLayout
import kotlinx.android.synthetic.main.fragment_registration_consent.*

class RegistrationContentFragment : PagerChildFragment() {

    override val navigationIcon: Int? = R.drawable.ic_up
    override var stepProgress: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View? = inflater.inflate(R.layout.fragment_registration_consent, container, false)

    override fun onResume() {
        super.onResume()
        registration_consent_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            updateButtonState()
        }
    }

    override fun updateButtonState() {
        if (registration_consent_checkbox.isChecked) {
            (activity as? PagerContainer)?.enableNextButton()
        } else {
            (activity as? PagerContainer)?.disableNextButton()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        root.removeAllViews()
    }

    override fun getUploadButtonLayout() = UploadButtonLayout.ContinueLayout(R.string.registration_consent_button) {
        navigateTo(RegistrationContentFragmentDirections.actionRegistrationConsentFragmentToPersonalDetailsFragment().actionId)
    }
}