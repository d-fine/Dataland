package org.dataland.datalandbackend.model.datapoints.specialised

import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation

/**
 * --- API model ---
 * Fields of data point containing a fiscal year deviation information
 */
data class FiscalYearDeviationData(
    val value: FiscalYearDeviation,
)
