package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- Generic API model ---
 * DTO for uploading general data sets for a specific company
 * @param companyId identifier of the company the data belongs to
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param data to be uploaded of general type T
 */
data class CompanyAssociatedData<T> (
    // TODO these three are anyways not user controlled in the frontend but should be there in a backend request
    @field:JsonProperty(required = true)
    val companyId: String,

    @field:JsonProperty(required = true)
    val reportingPeriod: String,

    @field:JsonProperty(required = true)
    val data: T,
)
