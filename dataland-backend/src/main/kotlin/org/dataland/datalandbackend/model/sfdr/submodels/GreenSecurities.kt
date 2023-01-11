package org.dataland.datalandbackend.model.sfdr.submodels

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Green securities"
 */
data class GreenSecurities(
    val securitiesNotCertifiedAsGreen: DataPoint<YesNo>?,
)
