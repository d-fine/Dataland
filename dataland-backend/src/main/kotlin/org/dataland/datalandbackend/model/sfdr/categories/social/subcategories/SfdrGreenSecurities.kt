package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Green securities"
 */
data class SfdrGreenSecurities(
    val securitiesNotCertifiedAsGreen: DataPoint<YesNo>? = null,
)
