package org.dataland.datalandbackend.model.datapoints.standard

import org.dataland.datalandbackend.model.datapoints.interfaces.DataPointWithSource
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.validator.Iso4217Currency
import org.dataland.datalandbackend.validator.ValueAndCurrency
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of a currency data point without restrictions on the value
 */
@ValueAndCurrency
data class CurrencyData(
    override val value: BigDecimal? = null,
    @field:Iso4217Currency
    val currency: String? = null,
    override val quality: QualityOptions? = null,
    override val comment: String? = null,
    override val dataSource: ExtendedDocumentReference? = null,
    override val applicable: Boolean? = null,
) : DataPointWithSource<BigDecimal>
