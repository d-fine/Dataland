package org.dataland.datalandbackend.model.datapoints.enums

import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of data point containing a fiscal year deviation information
 */
data class FiscalYearDeviationDataPoint(
    override val value: FiscalYearDeviation? = null,
    override val quality: QualityOptions? = null,
    override val comment: String? = null,
    override val dataSource: ExtendedDocumentReference? = null,
) : ExtendedDataPoint<FiscalYearDeviation>
