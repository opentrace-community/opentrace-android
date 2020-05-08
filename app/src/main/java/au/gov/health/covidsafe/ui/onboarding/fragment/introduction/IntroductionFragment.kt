package au.gov.health.covidsafe.ui.onboarding.fragment.introduction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.ui.PagerChildFragment
import au.gov.health.covidsafe.ui.UploadButtonLayout
import kotlinx.android.synthetic.main.fragment_intro.*

class IntroductionFragment : PagerChildFragment() {

    override val navigationIcon: Int? = null
    override var stepProgress: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View? = inflater.inflate(R.layout.fragment_intro, container, false)

    override fun getUploadButtonLayout() = UploadButtonLayout.ContinueLayout(R.string.intro_button) {
        navigateTo(R.id.action_introFragment_to_howItWorksFragment)
    }

    override fun updateButtonState() {
        enableContinueButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        root.removeAllViews()
    }
}