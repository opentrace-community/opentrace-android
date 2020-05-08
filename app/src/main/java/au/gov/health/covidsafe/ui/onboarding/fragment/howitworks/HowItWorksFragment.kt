package au.gov.health.covidsafe.ui.onboarding.fragment.howitworks

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.ui.PagerChildFragment
import au.gov.health.covidsafe.ui.UploadButtonLayout
import kotlinx.android.synthetic.main.fragment_how_it_works.*
import kotlinx.android.synthetic.main.fragment_how_it_works.view.*

class HowItWorksFragment : PagerChildFragment() {

    override val navigationIcon: Int? = R.drawable.ic_up
    override var stepProgress: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View? = inflater.inflate(R.layout.fragment_how_it_works, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.how_it_works_content.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun getUploadButtonLayout() = UploadButtonLayout.ContinueLayout(R.string.how_it_works_button) {
        navigateTo(R.id.action_howItWorksFragment_to_dataPrivacy)
    }

    override fun updateButtonState() {
        enableContinueButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        root.removeAllViews()
    }
}