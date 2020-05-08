package au.gov.health.covidsafe.ui.upload.presentation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import au.gov.health.covidsafe.BuildConfig
import au.gov.health.covidsafe.Preference
import au.gov.health.covidsafe.extensions.isInternetAvailable
import au.gov.health.covidsafe.factory.NetworkFactory
import au.gov.health.covidsafe.interactor.usecase.UploadData
import au.gov.health.covidsafe.interactor.usecase.UploadDataException
import au.gov.health.covidsafe.streetpass.persistence.StreetPassRecordStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class VerifyUploadPinPresenter(private val fragment: VerifyUploadPinFragment) : LifecycleObserver {

    private val TAG = this.javaClass.simpleName

    private var awsClient = NetworkFactory.awsClient
    private lateinit var uploadData: UploadData

    private lateinit var recordStorage: StreetPassRecordStorage

    init {
        fragment.lifecycle.addObserver(this)
        fragment.context?.let { context ->
            recordStorage = StreetPassRecordStorage(context)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        uploadData = UploadData(awsClient, NetworkFactory.okHttpClient, fragment.context, fragment.lifecycle)
    }

    internal fun uploadData(otp: String) {
        if (fragment.activity?.isInternetAvailable() == false) {
            fragment.showCheckInternetError()
        } else {
            fragment.disableContinueButton()
            fragment.showDialogLoading()
            uploadData.invoke(otp,
                    onSuccess = {
                        if (!BuildConfig.DEBUG) {
                            GlobalScope.launch { recordStorage.nukeDbAsync() }
                        }
                        fragment.context?.let { context ->
                            Preference.setDataIsUploaded(context, true)
                        }
                        fragment.navigateToNextPage()
                    },
                    onFailure = {
                        when (it) {
                            is UploadDataException.UploadDataIncorrectPinException -> {
                                fragment.showInvalidOtp()
                            }
                            is UploadDataException.UploadDataJwtExpiredException -> {
                                fragment.navigateToRegister()
                            }
                            else -> {
                                fragment.showGenericError()
                            }
                        }
                        fragment.enableContinueButton()
                        fragment.hideKeyboard()
                        fragment.hideLoading()
                    }
            )
        }
    }
}

