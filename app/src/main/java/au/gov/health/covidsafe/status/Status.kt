package au.gov.health.covidsafe.status

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Status(
    val msg: String
) : Parcelable