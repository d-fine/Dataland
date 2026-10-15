package org.dataland.datalandbackend.model.export

/**
 * Data class defining the data to be exported for a single data dimension.
 *
 * @param companyName the name of the company
 * @param companyLei the LEI of the company
 * @param reportingPeriod the reporting period
 * @param availability whether a dataset was found for this dimension or it is confirmed as non-sourceable
 * @param proxied comma-separated names of the companies proxied by this company (empty if none)
 * @param data the corresponding dataset, or null if no dataset is available (e.g. for non-sourceable dimensions)
 */
data class SingleCompanyExportData<T>(
    val companyName: String,
    val companyLei: String,
    val reportingPeriod: String,
    val availability: ExportAvailability,
    val proxied: String = "",
    val data: T?,
)
