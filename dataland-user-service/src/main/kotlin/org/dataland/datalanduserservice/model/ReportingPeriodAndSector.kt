package org.dataland.datalanduserservice.model

/**
 * Enum representing the sector type of companies relevant for bulk data requests.
 */
enum class SectorType(
    val sectorName: String,
) {
    FINANCIALS("financials"),
    NONFINANCIALS("nonfinancials"),
    UNKNOWN("unknown"),
}

/**
 * Data class representing a combination of reporting period and sector type.
 *
 * @property reportingPeriod The reporting period for which data is requested.
 * @property sector The sector type.
 */
data class ReportingPeriodAndSector(
    val reportingPeriod: String,
    val sector: SectorType,
)
