package org.dataland.datalandbackend.model

/**
 * DTO for uploading general data sets for a specific company
 * @param data to be uploaded of general type T
 * @param companyId identifier of the company the data belongs to
 */
data class CompanyAssociatedData<T> (val companyId: String, val data: T)
