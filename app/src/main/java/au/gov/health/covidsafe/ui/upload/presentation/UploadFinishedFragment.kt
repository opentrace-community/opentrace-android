package au.gov.health.covidsafe.ui.upload.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.ui.PagerChildFragment
import au.gov.health.covidsafe.ui.UploadButtonLayout
import kotlinx.android.synthetic.main.fragment_upload_finished.*

class UploadFinishedFragment : PagerChildFragment() {

    override val navigationIcon: Int? = null
    override var stepProgress: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_upload_finished, container, false)

    override fun getUploadButtonLayout() = UploadButtonLayout.ContinueLayout(R.string.action_upload_done) {
        activity?.onBackPressed()
    }

    override fun updateButtonState() {
        enableContinueButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        root.removeAllViews()
    }
}