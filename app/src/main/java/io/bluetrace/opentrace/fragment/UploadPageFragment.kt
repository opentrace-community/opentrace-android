package io.bluetrace.opentrace.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.fragment_upload_page.*
import io.bluetrace.opentrace.BuildConfig
import io.bluetrace.opentrace.MainActivity
import io.bluetrace.opentrace.R
import io.bluetrace.opentrace.status.persistence.StatusRecord
import io.bluetrace.opentrace.streetpass.persistence.StreetPassRecord


data class ExportData(val recordList: List<StreetPassRecord>, val statusList: List<StatusRecord>)

class UploadPageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var uuidString = BuildConfig.BLE_SSID

        uuidString = uuidString.substring(uuidString.length - 4)
        fragment_buildno.text = "${BuildConfig.GITHASH}-${uuidString}"
        val childFragMan: FragmentManager = childFragmentManager
        val childFragTrans: FragmentTransaction = childFragMan.beginTransaction()
        val fragB = VerifyCallerFragment()
        childFragTrans.add(R.id.fragment_placeholder, fragB)
        childFragTrans.addToBackStack("VerifyCaller")
        childFragTrans.commit()
    }

    fun turnOnLoadingProgress() {
        uploadPageFragmentLoadingProgressBarFrame.visibility = View.VISIBLE
    }

    fun turnOffLoadingProgress() {
        uploadPageFragmentLoadingProgressBarFrame.visibility = View.INVISIBLE
    }

    fun navigateToUploadPin() {
        val childFragMan: FragmentManager = childFragmentManager
        val childFragTrans: FragmentTransaction = childFragMan.beginTransaction()
        val fragB = EnterPinFragment()
        childFragTrans.add(R.id.fragment_placeholder, fragB)
        childFragTrans.addToBackStack("C")
        childFragTrans.commit()
    }

    fun goBackToHome() {
        var parentActivity = activity as MainActivity
        parentActivity.goToHome()
    }

    fun navigateToUploadComplete() {
        val childFragMan: FragmentManager = childFragmentManager
        val childFragTrans: FragmentTransaction = childFragMan.beginTransaction()
        val fragB = UploadCompleteFragment()
        childFragTrans.add(R.id.fragment_placeholder, fragB)
        childFragTrans.addToBackStack("UploadComplete")
        childFragTrans.commit()
    }

    fun popStack() {
        val childFragMan: FragmentManager = childFragmentManager
        childFragMan.popBackStack()
    }
}
