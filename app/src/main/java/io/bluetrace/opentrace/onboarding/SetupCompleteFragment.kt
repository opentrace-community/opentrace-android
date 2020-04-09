package io.bluetrace.opentrace.onboarding

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.analytics.FirebaseAnalytics
import io.bluetrace.opentrace.MainActivity
import io.bluetrace.opentrace.Preference
import io.bluetrace.opentrace.R
import io.bluetrace.opentrace.logging.CentralLog

class SetupCompleteFragment : OnboardingFragmentInterface() {
    private var listener: OnFragmentInteractionListener? = null
    private val TAG: String = "SetupCompleteFragment"
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var mainContext: Context

    override fun getButtonText(): String = "Continue"

    override fun becomesVisible() {}

    override fun onButtonClick(view: View) {
        CentralLog.d(TAG, "OnButtonClick 2")
        Preference.putCheckpoint(view.context, 0)
        Preference.putIsOnBoarded(view.context, true)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "P1234")
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Onboard Completed for Android Device")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
        var intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context?.startActivity(intent)
        (context as OnboardingActivity?)?.finish()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        firebaseAnalytics = FirebaseAnalytics.getInstance(mainContext)
        return inflater.inflate(R.layout.fragment_setup_complete, container, false)
    }

    override fun getProgressValue(): Int = 100

    override fun onUpdatePhoneNumber(num: String) {}

    override fun onError(error: String) {}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainContext = context;
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
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
