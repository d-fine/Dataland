package org.dataland.datalandbackend.model.datapoints

import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import java.util.UUID

/**
 * --- API model ---
 * Fields of a generic data point version 1
 * data and applicable cannot be null at the same time
 * if applicable is false, data must be null and vice versa
 */
data class StoredDataPoint<T>(
    val dataPointId: UUID,
    val data: GeneralDataPoint<T>?,
    val dataPointTypeId: UUID,
    val reportingPeriod: String,
    val companyId: UUID,
)

data class GeneralDataPoint<T>(
    val value: T?,
    val applicable: Boolean?,
    val dataSource: ExtendedDocumentReference?,
    val quality: QualityOptions?,
)
