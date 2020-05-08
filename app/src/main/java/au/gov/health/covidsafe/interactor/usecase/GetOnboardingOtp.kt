package au.gov.health.covidsafe.interactor.usecase

import androidx.lifecycle.Lifecycle
import au.gov.health.covidsafe.interactor.Either
import au.gov.health.covidsafe.interactor.Failure
import au.gov.health.covidsafe.interactor.Success
import au.gov.health.covidsafe.interactor.UseCase
import au.gov.health.covidsafe.logging.CentralLog
import au.gov.health.covidsafe.networking.request.OTPChallengeRequest
import au.gov.health.covidsafe.networking.response.OTPChallengeResponse
import au.gov.health.covidsafe.networking.service.AwsClient

class GetOnboardingOtp(private val awsClient: AwsClient, lifecycle: Lifecycle) : UseCase<OTPChallengeResponse, GetOtpParams>(lifecycle) {

    private val TAG = this.javaClass.simpleName

    override suspend fun run(params: GetOtpParams): Either<Exception, OTPChallengeResponse> {
        return try {
            val response = awsClient.initiateAuth(
                    OTPChallengeRequest(params.phoneNumber,
                            params.deviceId,
                            params.postCode,
                            params.age,
                            params.name)).execute()
            when {
                response.code() == 200 -> {
                    response.body()?.let { body ->
                        CentralLog.d(TAG, "onCodeSent: ${response.body()?.challengeName}")
                        Success(body)
                    } ?: run {
                        CentralLog.d(TAG, "AWSAuthInvalidBody")
                        Failure(GetOnboardingOtpException.GetOtpServiceException(response.code()))
                    }
                }
                response.code() == 400 -> {
                    CentralLog.d(TAG, "AWSAuthInvalidNumber")
                    Failure(GetOnboardingOtpException.GetOtpInvalidNumberException)
                }
                else -> {
                    CentralLog.d(TAG, "AWSAuthServiceError")
                    Failure(GetOnboardingOtpException.GetOtpServiceException(response.code()))
                }
            }
        } catch (e: Exception) {
            CentralLog.d(TAG, "AWSAuthInvalidChallengeRequest", e)
            Failure(GetOnboardingOtpException.GetOtpServiceException())
        }
    }
}

data class GetOtpParams(internal val phoneNumber: String,
                        internal val deviceId: String,
                        internal val postCode: String?,
                        internal val age: String?,
                        internal val name: String?)

sealed class GetOnboardingOtpException : Exception() {
    class GetOtpServiceException(val code: Int? = null) : GetOnboardingOtpException()
    object GetOtpInvalidNumberException : GetOnboardingOtpException()
}