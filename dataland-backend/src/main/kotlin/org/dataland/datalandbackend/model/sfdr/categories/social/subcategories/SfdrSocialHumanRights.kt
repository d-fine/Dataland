package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.validator.DataPointMinimumValue

/**
 * --- API model ---
 * Fields of the subcategory "Human rights" belonging to the category "Social" of the sfdr framework.
*/
data class SfdrSocialHumanRights(
    @field:Valid
    val humanRightsPolicy: BaseDataPoint<YesNo>? = null,

    @field:Valid
    val humanRightsDueDiligence: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val traffickingInHumanBeingsPolicy: BaseDataPoint<YesNo>? = null,

    @field:Valid
    val reportedChildLabourIncidents: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val reportedForcedOrCompulsoryLabourIncidents: ExtendedDataPoint<YesNo>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val numberOfReportedIncidentsOfHumanRightsViolations: ExtendedDataPoint<Long>? = null,
)
