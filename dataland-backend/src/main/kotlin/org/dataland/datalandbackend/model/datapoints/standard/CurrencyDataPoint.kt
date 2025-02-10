package org.dataland.datalandbackend.model.datapoints.standard

import jakarta.validation.Valid
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of a currency data point without restrictions on the value
 */
data class CurrencyDataPoint(
    override val value: BigDecimal? = null,
    val currency: String? = null,
    override val quality: QualityOptions? = null,
    override val comment: String? = null,
    @field:Valid
    override val dataSource: ExtendedDocumentReference? = null,
) : ExtendedDataPoint<BigDecimal>
