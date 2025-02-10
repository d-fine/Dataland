package org.dataland.datalandbackend.model.datapoints.extended

import jakarta.validation.Valid
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import java.math.BigInteger

/**
 * --- API model ---
 * Fields of a integer data point without restrictions on the value
 */
data class ExtendedIntegerDataPoint(
    override val value: BigInteger? = null,
    override val quality: QualityOptions? = null,
    override val comment: String? = null,
    @field:Valid
    override val dataSource: ExtendedDocumentReference? = null,
) : ExtendedDataPoint<BigInteger>
