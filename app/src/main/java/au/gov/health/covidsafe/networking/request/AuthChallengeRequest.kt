package au.gov.health.covidsafe.networking.request

import androidx.annotation.Keep

@Keep
data class AuthChallengeRequest(val session: String?, val code: String?)