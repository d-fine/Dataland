package org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity

/**
 * --- API model ---
 * This class represents an activity related to the EU taxonomy framework
 */
data class EuTaxonomyActivity(
    @JsonProperty(required = true)
    val activityName: Activity,
    val naceCodes: List<String>?,
    val share: RelativeAndAbsoluteFinancialShare?,
)
