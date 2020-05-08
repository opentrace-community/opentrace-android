package au.gov.health.covidsafe.networking.request

import androidx.annotation.Keep

@Keep
data class OTPChallengeRequest(val phone_number: String,
                               val device_id: String,
                               val postcode: String?,
                               val age: String?,
                               val name: String?)