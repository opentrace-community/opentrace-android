package au.gov.health.covidsafe.networking.response

import androidx.annotation.Keep

@Keep
data class OTPChallengeResponse(val session: String, val challengeName: String)
