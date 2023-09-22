package org.dataland.datalandbackend.model.eutaxonomy

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.ExtendedDataPointInterface
import org.dataland.datalandbackend.model.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Level of assurance for the reported data and information about the assurance provider
 */
data class AssuranceDataPoint<T>(
    @field:JsonProperty(required = true)
    override val value: T,

    override val quality: QualityOptions,

    override val comment: String? = null,

    val dataSource: ExtendedDocumentReference? = null,

    val provider: String? = null,
) : ExtendedDataPointInterface<T>
