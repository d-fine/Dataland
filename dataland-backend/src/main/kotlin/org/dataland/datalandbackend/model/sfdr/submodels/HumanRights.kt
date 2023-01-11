package org.dataland.datalandbackend.model.sfdr.submodels

import java.math.BigDecimal
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Human rights"
 */
data class HumanRights(
    val humanRightsPolicy: DataPoint<YesNo>?,

    val humanRightsDueDiligence: DataPoint<YesNo>?,

    val traffickingInHumanBeingsPolicy: DataPoint<YesNo>?,

    val reportedChildLabourIncidents: DataPoint<YesNo>?,

    val reportedForcedOrCompulsoryLabourIncidents: DataPoint<YesNo>?,

    val reportedIncidentsOfHumanRights: DataPoint<BigDecimal>?,
)
