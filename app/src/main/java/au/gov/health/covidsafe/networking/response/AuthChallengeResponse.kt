package au.gov.health.covidsafe.networking.response

import androidx.annotation.Keep

@Keep
data class AuthChallengeResponse(val token: String, val uuid: String, val token_expiry: String, val pin: String)