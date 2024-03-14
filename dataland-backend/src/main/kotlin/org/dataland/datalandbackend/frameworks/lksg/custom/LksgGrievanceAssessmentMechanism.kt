package org.dataland.datalandbackend.frameworks.lksg.custom

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.util.EnumSet

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding grievance mechanisms
 */
data class LksgGrievanceAssessmentMechanism(
    @field:JsonProperty(required = true)
    val riskPositions: EnumSet<RiskPositionType>,
    val specifiedComplaint: String,
    val measuresTaken: YesNo,
    val listedMeasures: String?,
)
