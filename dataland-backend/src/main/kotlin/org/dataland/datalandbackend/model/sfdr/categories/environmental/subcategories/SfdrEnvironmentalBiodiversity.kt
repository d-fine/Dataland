package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Biodiversity" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalBiodiversity(
    val primaryForestAndWoodedLandOfNativeSpeciesExposure: DataPoint<YesNo>? = null,

    val protectedAreasExposure: DataPoint<YesNo>? = null,

    val rareOrEndangeredEcosystemsExposure: DataPoint<YesNo>? = null,
)
