package au.gov.health.covidsafe.interactor.usecase

import androidx.lifecycle.Lifecycle
import au.gov.health.covidsafe.interactor.Either
import au.gov.health.covidsafe.interactor.Failure
import au.gov.health.covidsafe.interactor.Success
import au.gov.health.covidsafe.interactor.UseCase
import au.gov.health.covidsafe.logging.CentralLog
import au.gov.health.covidsafe.networking.response.UploadOTPResponse
import au.gov.health.covidsafe.networking.service.AwsClient

class GetUploadOtp(private val awsClient: AwsClient, lifecycle: Lifecycle)
    : UseCase<UploadOTPResponse?, String>(lifecycle) {

    private val TAG = this.javaClass.simpleName

    override suspend fun run(params: String): Either<Exception, UploadOTPResponse?> {
        return try {
            val response = awsClient.requestUploadOtp("Bearer $params").execute()
            return if (response.code() == 200) {
                CentralLog.d(TAG, "onCodeUpload")
                Success(response.body())
            } else {
                Failure(GetUploadOtpException.GetUploadOtpServiceException(response.code()))
            }
        } catch (e: Exception) {
            Failure(e)
        }
    }
}

sealed class GetUploadOtpException : Exception() {
    class GetUploadOtpServiceException(val code: Int?) : GetUploadOtpException()
}