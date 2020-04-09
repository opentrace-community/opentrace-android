package io.bluetrace.opentrace.onboarding

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_tou.*
import io.bluetrace.opentrace.R
import io.bluetrace.opentrace.logging.CentralLog

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TOUFragment : OnboardingFragmentInterface() {
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private val TAG: String = "TOUFragment"
    private lateinit var privacyTextView: TextView
    private lateinit var mainContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getButtonText(): String = "I agree"

    override fun onUpdatePhoneNumber(num: String) {}

    override fun onError(error: String) {}

    override fun becomesVisible() {}

    override fun onButtonClick(buttonView: View) {
        CentralLog.d(TAG, "OnButtonClick 4")
        val onboardActivity = context as OnboardingActivity
        onboardActivity.navigateToNextPage()
    }

    override fun getProgressValue(): Int = 60

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tou, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        privacy.setOnClickListener {
            CentralLog.d(TAG, "clicked the privacy safeguards")
            val intent = Intent(mainContext, WebViewActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainContext = context
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}
