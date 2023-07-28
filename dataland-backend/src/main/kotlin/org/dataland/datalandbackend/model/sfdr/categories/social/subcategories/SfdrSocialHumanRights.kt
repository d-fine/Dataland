package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.DataPointWithUnit
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Human rights" belonging to the category "Social" of the sfdr framework.
 */
data class SfdrSocialHumanRights(
    val humanRightsPolicy: DataPoint<YesNo>? = null,

    val humanRightsDueDiligence: DataPoint<YesNo>? = null,

    val traffickingInHumanBeingsPolicy: DataPoint<YesNo>? = null,

    val reportedChildLabourIncidents: DataPoint<YesNo>? = null,

    val reportedForcedOrCompulsoryLabourIncidents: DataPoint<YesNo>? = null,

    val reportedIncidentsOfHumanRights: DataPointWithUnit<BigDecimal>? = null,
)
