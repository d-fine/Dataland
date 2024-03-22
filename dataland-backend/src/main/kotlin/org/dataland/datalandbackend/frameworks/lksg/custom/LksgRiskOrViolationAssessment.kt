package org.dataland.datalandbackend.frameworks.lksg.custom

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * This class represents a template for a risk position or a general violation in the Lksg framework
 */
data class LksgRiskOrViolationAssessment(
    @field:JsonProperty(required = true)
    val riskPosition: RiskPositionType,
    val measuresTaken: YesNo,
    val listedMeasures: String?,
)
