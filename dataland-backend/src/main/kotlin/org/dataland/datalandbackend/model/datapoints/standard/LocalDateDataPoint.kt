package org.dataland.datalandbackend.model.datapoints.standard

import jakarta.validation.Valid
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of a data point containing only a date
 */
data class LocalDateDataPoint(
    override val value: LocalDate? = null,
    override val quality: QualityOptions? = null,
    override val comment: String? = null,
    @field:Valid
    override val dataSource: ExtendedDocumentReference? = null,
) : ExtendedDataPoint<LocalDate>
