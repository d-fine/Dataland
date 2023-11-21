package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import jakarta.validation.constraints.Min
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Human rights" belonging to the category "Social" of the sfdr framework.
 */
data class SfdrSocialHumanRights(
    val humanRightsPolicy: BaseDataPoint<YesNo>? = null,

    val humanRightsDueDiligence: ExtendedDataPoint<YesNo>? = null,

    val traffickingInHumanBeingsPolicy: BaseDataPoint<YesNo>? = null,

    @Min(0)
    val reportedChildLabourIncidents: ExtendedDataPoint<YesNo>? = null,

    @Min(0)
    val reportedForcedOrCompulsoryLabourIncidents: ExtendedDataPoint<YesNo>? = null,

    @Min(0)
    val numberOfReportedIncidentsOfHumanRightsViolations: ExtendedDataPoint<Long>? = null,
)
