package org.dataland.datalandbackend.model.datapoints

import jakarta.validation.constraints.Min
import org.dataland.datalandbackend.validator.Iso4217Currency
import java.math.BigDecimal

data class AnotherActualDataPoint(
    @field:Min(0)
    override val value: BigDecimal? = null,
    @field:Iso4217Currency
    val currency: String? = null,
    override val comment: String? = null,
    // override val dataSource: ExtendedDocumentReference? = null,
    // override val quality: QualityOptions? = null,
    // override val applicable: Boolean = true,
) : AbstractGenericDatapoint<BigDecimal>()
