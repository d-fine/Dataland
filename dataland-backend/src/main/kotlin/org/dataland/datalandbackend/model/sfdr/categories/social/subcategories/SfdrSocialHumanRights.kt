package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Human rights" belonging to the category "Social" of the sfdr framework.
 */
data class SfdrSocialHumanRights(
    val humanRightsPolicy: ExtendedDataPoint<YesNo>? = null,

    val humanRightsDueDiligence: ExtendedDataPoint<YesNo>? = null,

    val traffickingInHumanBeingsPolicy: ExtendedDataPoint<YesNo>? = null,

    val reportedChildLabourIncidents: ExtendedDataPoint<YesNo>? = null,

    val reportedForcedOrCompulsoryLabourIncidents: ExtendedDataPoint<YesNo>? = null,

    val reportedIncidentsOfHumanRights: ExtendedDataPoint<BigDecimal>? = null,
)
