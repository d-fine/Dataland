package org.dataland.datalandbackend.model.export

/**
 * Data class defining the data to be exported for a single data dimension.
 *
 * @param companyName the name of the company
 * @param companyLei the LEI of the company
 * @param reportingPeriod the reporting period
 * @param data the corresponding dataset
 */
data class SingleCompanyDataExport<T>(
    val companyName: String,
    val companyLei: String,
    val reportingPeriod: String,
    val data: T,
)
