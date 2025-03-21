package org.dataland.datalandbackend.model.datapoints.custom

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.eutaxonomy.AssuranceOptions

/**
 * --- API model ---
 * Level of assurance for the reported data and information about the assurance provider
 */
data class CustomEuTaxonomyReportingAssuranceDataPoint(
    @field:JsonProperty(required = true)
    override val value: AssuranceOptions,
    @field:Valid()
    override val dataSource: ExtendedDocumentReference? = null,
    val provider: String? = null,
) : BaseDataPoint<AssuranceOptions>
