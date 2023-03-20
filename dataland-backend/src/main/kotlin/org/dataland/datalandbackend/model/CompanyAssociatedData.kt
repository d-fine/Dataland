package org.dataland.datalandbackend.model

/**
 * --- Generic API model ---
 * DTO for uploading general data sets for a specific company
 * @param companyId identifier of the company the data belongs to
 * @param reportingPeriod marks a period - e.g. a year or a specific quarter in a year - for which the data is valid
 * @param data to be uploaded of general type T
 */
data class CompanyAssociatedData<T> (val companyId: String, val reportingPeriod: String, val data: T)
