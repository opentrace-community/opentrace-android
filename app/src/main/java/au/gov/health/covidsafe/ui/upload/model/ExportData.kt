package au.gov.health.covidsafe.ui.upload.model

import androidx.annotation.Keep
import au.gov.health.covidsafe.streetpass.persistence.StreetPassRecord
@Keep
class ExportData constructor(var records: List<StreetPassRecord>)