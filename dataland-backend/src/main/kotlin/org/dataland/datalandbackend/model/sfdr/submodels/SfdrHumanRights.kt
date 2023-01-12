package org.dataland.datalandbackend.model.sfdr.submodels

import java.math.BigDecimal
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Human rights"
 */
data class SfdrHumanRights(
    val humanRightsPolicy: DataPoint<YesNo>? = null,

    val humanRightsDueDiligence: DataPoint<YesNo>? = null,

    val traffickingInHumanBeingsPolicy: DataPoint<YesNo>? = null,

    val reportedChildLabourIncidents: DataPoint<YesNo>? = null,

    val reportedForcedOrCompulsoryLabourIncidents: DataPoint<YesNo>? = null,

    val reportedIncidentsOfHumanRights: DataPoint<BigDecimal>? = null,
)
