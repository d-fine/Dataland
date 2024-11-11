package org.dataland.datalandinternalstorage.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class StorableDataPoint(
    @field:JsonProperty(required = true)
    val dataPointContent: String,
    @field:JsonProperty(required = true)
    val dataPointIdentifier: String,
    @field:JsonProperty(required = true)
    val companyId: UUID,
    @field:JsonProperty(required = true)
    val reportingPeriod: String,
)
