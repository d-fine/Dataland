package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.ExtendedDataPointInterface
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of a generic data point with unit and its source
 */
data class CurrencyDataPoint(
    override val value: BigDecimal? = null,
    @field:JsonProperty(required = true)
    override val quality: QualityOptions,
    override val comment: String? = null,
    override val dataSource: ExtendedDocumentReference? = null,
    val currency: String? = null,
) : ExtendedDataPointInterface<BigDecimal>
