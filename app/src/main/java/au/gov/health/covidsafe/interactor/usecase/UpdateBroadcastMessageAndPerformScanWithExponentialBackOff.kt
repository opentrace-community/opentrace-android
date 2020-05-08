package au.gov.health.covidsafe.interactor.usecase

import android.content.Context
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.delay
import retrofit2.Response
import au.gov.health.covidsafe.Preference
import au.gov.health.covidsafe.Utils
import au.gov.health.covidsafe.interactor.Either
import au.gov.health.covidsafe.interactor.Failure
import au.gov.health.covidsafe.interactor.Success
import au.gov.health.covidsafe.interactor.UseCase
import au.gov.health.covidsafe.networking.response.BroadcastMessageResponse
import au.gov.health.covidsafe.networking.service.AwsClient
import kotlin.math.pow

class UpdateBroadcastMessageAndPerformScanWithExponentialBackOff(private val awsClient: AwsClient,
                                                                 private val context: Context,
                                                                 lifecycle: Lifecycle) : UseCase<BroadcastMessageResponse, Void?>(lifecycle) {

    private val TAG = this.javaClass.simpleName
    private val RETRIES_LIMIT = 3

    override suspend fun run(params: Void?): Either<Exception, BroadcastMessageResponse> {
        val jwtToken = Preference.getEncrypterJWTToken(context)
        return jwtToken?.let { jwtToken ->
            var response = call(jwtToken)
            var retryCount = 0
            while ((response == null || !response.isSuccessful || response.body() == null) && retryCount < RETRIES_LIMIT) {
                val interval = 2.toDouble().pow(retryCount.toDouble()).toLong() * 1000
                delay(interval)
                response = call(jwtToken)
                retryCount++
            }

            if (response != null && response.isSuccessful) {
                response.body()?.let { broadcastMessageResponse ->
                    if (broadcastMessageResponse.tempId.isNullOrEmpty()) {
                        Failure(Exception())
                    } else {
                        val expiryTime = broadcastMessageResponse.expiryTime
                        val expiry = expiryTime?.toLongOrNull() ?: 0
                        Preference.putExpiryTimeInMillis(context, expiry * 1000)
                        val refreshTime = broadcastMessageResponse.refreshTime
                        val refresh = refreshTime?.toLongOrNull() ?: 0
                        Preference.putNextFetchTimeInMillis(context, refresh * 1000)
                        Utils.storeBroadcastMessage(context, broadcastMessageResponse.tempId)
                        Success(broadcastMessageResponse)
                    }
                } ?: run {
                    Failure(Exception())
                }
            } else {
                Failure(Exception())
            }
        } ?: run {
            return Failure(Exception())
        }
    }

    private fun call(jwtToken: String): Response<BroadcastMessageResponse>? {
        return try {
            awsClient.getTempId("Bearer $jwtToken").execute()
        } catch (e: Exception) {
            null
        }
    }

}