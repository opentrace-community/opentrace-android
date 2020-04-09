package io.bluetrace.opentrace.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_upload_verifycaller.*
import io.bluetrace.opentrace.Preference
import io.bluetrace.opentrace.R

class VerifyCallerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_verifycaller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var handShakePin = Preference.getHandShakePin(view.context)
        verifyCallerFragmentVerificationCode.text = handShakePin
        verifyCallerFragmentActionButton.setOnClickListener {
            var myParentFragment: UploadPageFragment = (parentFragment as UploadPageFragment)
            myParentFragment.navigateToUploadPin()
        }
    }
}
