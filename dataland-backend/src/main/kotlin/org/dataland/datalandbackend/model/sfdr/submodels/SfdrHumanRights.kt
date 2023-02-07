package org.dataland.datalandbackend.model.sfdr.submodels

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Human rights"
 */
data class SfdrHumanRights(
    val humanRightsPolicy: DataPoint<YesNo>?,

    val humanRightsDueDiligence: DataPoint<YesNo>?,

    val traffickingInHumanBeingsPolicy: DataPoint<YesNo>?,

    val reportedChildLabourIncidents: DataPoint<YesNo>?,

    val reportedForcedOrCompulsoryLabourIncidents: DataPoint<YesNo>?,

    val reportedIncidentsOfHumanRights: DataPoint<BigDecimal>?,
)
