package org.dataland.datalandbackend.model

/**
 * DTO for uploading general data sets for a specific company
 * @param dataSet data to be uploaded of general type T
 * @param companyId identifier of the company the data belongs to
 */
data class CompanyAssociatedDataSet<T> (val dataSet: T, val companyId: String)
