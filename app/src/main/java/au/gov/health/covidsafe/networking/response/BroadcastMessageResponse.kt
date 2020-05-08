package au.gov.health.covidsafe.networking.response

import androidx.annotation.Keep

@Keep
data class BroadcastMessageResponse(val tempId: String?, val expiryTime: String?, val refreshTime: String?)