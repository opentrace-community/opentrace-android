package au.gov.health.covidsafe.networking.service

import au.gov.health.covidsafe.BuildConfig
import au.gov.health.covidsafe.networking.request.AuthChallengeRequest
import au.gov.health.covidsafe.networking.request.OTPChallengeRequest
import au.gov.health.covidsafe.networking.response.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AwsClient {

    @POST(BuildConfig.END_POINT_PREFIX + "/initiateAuth")
    fun initiateAuth(@Body body : OTPChallengeRequest) : Call<OTPChallengeResponse>

    @POST(BuildConfig.END_POINT_PREFIX + "/respondToAuthChallenge")
    fun respondToAuthChallenge(@Body body : AuthChallengeRequest) : Call<AuthChallengeResponse>

    @GET(BuildConfig.END_POINT_PREFIX + "/getTempId")
    fun getTempId(@Header("Authorization") jwtToken: String?) : Call<BroadcastMessageResponse>

    @GET(BuildConfig.END_POINT_PREFIX + "/initiateDataUpload")
    fun initiateUpload(@Header("Authorization") jwtToken: String?,@Header("pin") pin : String) : Call<InitiateUploadResponse>

    @GET(BuildConfig.END_POINT_PREFIX + "/initiateDataUpload")
    fun initiateReUpload(@Header("Authorization") jwtToken: String?): Call<InitiateUploadResponse>

    @GET(BuildConfig.END_POINT_PREFIX + "/requestUploadOtp")
    fun requestUploadOtp(@Header("Authorization") jwtToken : String?) : Call<UploadOTPResponse>

}