package io.bluetrace.opentrace.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_upload_uploadcomplete.*
import io.bluetrace.opentrace.R

class UploadCompleteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_uploadcomplete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uploadCompleteFragmentActionButton.setOnClickListener {
            var myParentFragment: UploadPageFragment = (parentFragment as UploadPageFragment)
            myParentFragment.goBackToHome()
        }
    }
}
