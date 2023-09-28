package org.dataland.datalandbackend.model.eutaxonomy

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.BaseDataPointInterface
import org.dataland.datalandbackend.model.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.eutaxonomy.AssuranceOptions

/**
 * --- API model ---
 * Level of assurance for the reported data and information about the assurance provider
 */
data class AssuranceDataPoint(
    @field:JsonProperty(required = true)
    override val value: AssuranceOptions?,

    override val dataSource: ExtendedDocumentReference? = null,

    val provider: String? = null,
) : BaseDataPointInterface<AssuranceOptions>
