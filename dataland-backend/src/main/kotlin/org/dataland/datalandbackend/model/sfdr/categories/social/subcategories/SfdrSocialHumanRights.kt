package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import org.dataland.datalandbackend.model.DataPointOneValue
import org.dataland.datalandbackend.model.DataPointWithUnit
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Human rights" belonging to the category "Social" of the sfdr framework.
 */
data class SfdrSocialHumanRights(
    val humanRightsPolicy: DataPointOneValue<YesNo>? = null,

    val humanRightsDueDiligence: DataPointOneValue<YesNo>? = null,

    val traffickingInHumanBeingsPolicy: DataPointOneValue<YesNo>? = null,

    val reportedChildLabourIncidents: DataPointOneValue<YesNo>? = null,

    val reportedForcedOrCompulsoryLabourIncidents: DataPointOneValue<YesNo>? = null,

    val reportedIncidentsOfHumanRights: DataPointWithUnit<BigDecimal>? = null,
)
