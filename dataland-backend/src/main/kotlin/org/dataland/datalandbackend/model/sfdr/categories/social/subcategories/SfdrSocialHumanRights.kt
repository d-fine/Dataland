package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Human rights" belonging to the category "Social" of the sfdr framework.
 */
data class SfdrSocialHumanRights(
    val humanRightsPolicy: BaseDataPoint<YesNo>? = null,

    val humanRightsDueDiligence: ExtendedDataPoint<YesNo>? = null,

    val traffickingInHumanBeingsPolicy: BaseDataPoint<YesNo>? = null,

    val reportedChildLabourIncidents: ExtendedDataPoint<YesNo>? = null,

    val reportedForcedOrCompulsoryLabourIncidents: ExtendedDataPoint<YesNo>? = null,

    val numberOfReportedIncidentsOfHumanRightsViolations: ExtendedDataPoint<BigDecimal>? = null,
)
