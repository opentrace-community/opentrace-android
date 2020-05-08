package au.gov.health.covidsafe.networking.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class InitiateUploadResponse(@SerializedName("UploadLink") val uploadLink: String,
                                  @SerializedName("ExpiresIn") val expiresIn: String,
                                  @SerializedName("UploadPrefix") val uploadPrefix: String)