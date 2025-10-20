package org.dataland.datalandbackend.model.datapoints.extended.enums

import jakarta.validation.Valid
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of data point containing a yes or no or na value
 */
class ExtendedYesNoNaDataPoint(
    override val value: YesNoNa? = null,
    override val quality: QualityOptions? = null,
    override val comment: String? = null,
    @field:Valid
    override val dataSource: ExtendedDocumentReference? = null,
) : ExtendedDataPoint<YesNoNa>
