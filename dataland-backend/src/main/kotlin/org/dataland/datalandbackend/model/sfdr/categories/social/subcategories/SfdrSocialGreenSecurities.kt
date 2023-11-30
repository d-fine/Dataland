package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Green securities" belonging to the category "Social" of the sfdr framework.
*/
data class SfdrSocialGreenSecurities(
    @field:Valid
    val securitiesNotCertifiedAsGreen: ExtendedDataPoint<YesNo>? = null,
)
