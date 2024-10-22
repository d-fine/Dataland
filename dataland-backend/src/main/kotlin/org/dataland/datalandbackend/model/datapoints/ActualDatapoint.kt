package org.dataland.datalandbackend.model.datapoints

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.dataland.datalandbackend.model.datapoints.interfaces.DataPointWithSource
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import java.math.BigDecimal

class ActualDatapoint(
    @field:Min(0)
    override val value: BigDecimal? = null,
    override val quality: QualityOptions? = null,
    override val comment: String? = null,
    @field:Valid
    override val dataSource: ExtendedDocumentReference? = null,
    override val applicable: Boolean? = null,
) : DataPointWithSource<BigDecimal>
