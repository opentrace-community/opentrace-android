package au.gov.health.covidsafe.ui.upload.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.ui.PagerChildFragment
import au.gov.health.covidsafe.ui.UploadButtonLayout
import kotlinx.android.synthetic.main.fragment_upload_page_4.*

class UploadInitialFragment : PagerChildFragment() {

    override val navigationIcon: Int? = R.drawable.ic_up

    override var stepProgress: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_upload_initial, container, false)


    override fun updateButtonState() {
        enableContinueButton()
    }

    override fun getUploadButtonLayout() = UploadButtonLayout.QuestionLayout(
            buttonYesListener = {
                navigateTo(R.id.action_uploadInitial_to_uploadStepFourFragment)
            },
            buttonNoListener = {
                activity?.onBackPressed()
            })

    override fun onDestroyView() {
        super.onDestroyView()
        root.removeAllViews()
    }

}
