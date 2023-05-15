package org.dataland.datalandbackend.model

/**
 * --- API model ---
 * Fields of a generic data point and its source
 */
data class BaseDataPoint <T>(
    val value: T? = null,

    val dataSource: CompanyReportReference? = null,
)