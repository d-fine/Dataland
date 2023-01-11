package org.dataland.datalandbackend.model.sfdr

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Biodiversity"
 */
data class Biodiversity(
    val primaryForestAndWoodedLandOfNativeSpeciesExposure: DataPoint<YesNo>?,

    val protectedAreasExposure: DataPoint<YesNo>?,

    val rareOrEndangeredEcosystemsExposure: DataPoint<YesNo>?,
)
