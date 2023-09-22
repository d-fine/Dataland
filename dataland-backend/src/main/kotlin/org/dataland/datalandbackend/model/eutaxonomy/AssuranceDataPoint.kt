package org.dataland.datalandbackend.model.eutaxonomy

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.BaseDataPointInterface
import org.dataland.datalandbackend.model.ExtendedDocumentReference

/**
 * --- API model ---
 * Level of assurance for the reported data and information about the assurance provider
 */
data class AssuranceDataPoint<T>(
    @field:JsonProperty(required = true)
    override val value: T,

    val dataSource: ExtendedDocumentReference? = null,

    val provider: String? = null,
) : BaseDataPointInterface<T>
