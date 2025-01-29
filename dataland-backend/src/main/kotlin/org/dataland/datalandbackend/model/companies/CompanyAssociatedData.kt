package org.dataland.datalandbackend.model.companies

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid

/**
 * --- Generic API model ---
 * DTO for uploading general datasets for a specific company
 * @param companyId identifier of the company the data belongs to
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param data to be uploaded of general type T
 */
data class CompanyAssociatedData<T>(
    @field:JsonProperty(required = true)
    val companyId: String,
    @field:JsonProperty(required = true)
    val reportingPeriod: String,
    @field:JsonProperty(required = true)
    @field:Valid
    val data: T,
)
