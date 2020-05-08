package au.gov.health.covidsafe.ui.onboarding.fragment.enternumber


import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import au.gov.health.covidsafe.Preference
import au.gov.health.covidsafe.R
import au.gov.health.covidsafe.TracerApp
import au.gov.health.covidsafe.extensions.isInternetAvailable
import au.gov.health.covidsafe.factory.NetworkFactory
import au.gov.health.covidsafe.interactor.usecase.GetOnboardingOtp
import au.gov.health.covidsafe.interactor.usecase.GetOnboardingOtpException
import au.gov.health.covidsafe.interactor.usecase.GetOtpParams


class EnterNumberPresenter(private val enterNumberFragment: EnterNumberFragment) : LifecycleObserver {

    private val TAG = this.javaClass.simpleName

    private lateinit var phoneNumber: String
    private lateinit var getOnboardingOtp: GetOnboardingOtp

    init {
        enterNumberFragment.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        getOnboardingOtp = GetOnboardingOtp(NetworkFactory.awsClient, enterNumberFragment.lifecycle)
    }

    internal fun requestOTP(phoneNumber: String) {
        when {
            enterNumberFragment.activity?.isInternetAvailable() == false -> {
                enterNumberFragment.showCheckInternetError()
            }
            validateAuNumber(phoneNumber) -> {
                val cleansedNumber = if (phoneNumber.startsWith("0")) {
                    phoneNumber.takeLast(TracerApp.AppContext.resources.getInteger(R.integer.australian_phone_number_length))
                } else phoneNumber
                val fullNumber = "${enterNumberFragment.resources.getString(R.string.enter_number_prefix)}$cleansedNumber"
                Preference.putPhoneNumber(TracerApp.AppContext, fullNumber)
                this.phoneNumber = cleansedNumber
                makeOTPCall(cleansedNumber)
            }
            else -> {
                enterNumberFragment.showInvalidPhoneNumber()
            }
        }
    }

    /**
     * @param phoneNumber cleansed phone number, 9 digits, doesn't start with 0
     */
    private fun makeOTPCall(phoneNumber: String) {
        enterNumberFragment.activity?.let {
            enterNumberFragment.disableContinueButton()
            enterNumberFragment.showLoading()
            getOnboardingOtp.invoke(GetOtpParams(phoneNumber,
                    Preference.getDeviceID(enterNumberFragment.requireContext()),
                    Preference.getPostCode(enterNumberFragment.requireContext()),
                    Preference.getAge(enterNumberFragment.requireContext()),
                    Preference.getName(enterNumberFragment.requireContext())),
                    onSuccess = {
                        enterNumberFragment.navigateToOTPPage(
                                it.session,
                                it.challengeName,
                                phoneNumber)
                    },
                    onFailure = {
                        if (it is GetOnboardingOtpException.GetOtpInvalidNumberException) {
                            enterNumberFragment.showInvalidPhoneNumber()
                        } else {
                            enterNumberFragment.showGenericError()
                        }
                        enterNumberFragment.hideLoading()
                        enterNumberFragment.enableContinueButton()
                    })
        }
    }

    internal fun validateAuNumber(phoneNumber: String?): Boolean {
        var australianPhoneNumberLength = enterNumberFragment.resources.getInteger(R.integer.australian_phone_number_length)
        if (phoneNumber?.startsWith("0") == true) {
            australianPhoneNumberLength++
        }
        return (phoneNumber?.length ?: 0) == australianPhoneNumberLength
    }

}