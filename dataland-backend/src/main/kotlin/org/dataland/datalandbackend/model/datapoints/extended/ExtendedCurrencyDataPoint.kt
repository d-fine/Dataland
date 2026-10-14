package org.dataland.datalandbackend.model.datapoints.extended

import jakarta.validation.Valid
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.validator.ValidCurrencyCode
import org.dataland.datalandbackend.validator.ValidCurrencyDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of a currency data point without restrictions on the value
 */
@ValidCurrencyDataPoint
data class ExtendedCurrencyDataPoint(
    override val value: BigDecimal? = null,
    @field:ValidCurrencyCode
    val currency: String? = null,
    override val quality: QualityOptions? = null,
    override val comment: String? = null,
    @field:Valid
    override val dataSource: ExtendedDocumentReference? = null,
) : ExtendedDataPoint<BigDecimal>
