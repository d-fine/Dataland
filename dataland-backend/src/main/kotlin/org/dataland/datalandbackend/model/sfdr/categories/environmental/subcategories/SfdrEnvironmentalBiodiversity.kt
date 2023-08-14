package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.DataPointOneValue
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Biodiversity" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalBiodiversity(
    val primaryForestAndWoodedLandOfNativeSpeciesExposure: DataPointOneValue<YesNo>? = null,

    val protectedAreasExposure: DataPointOneValue<YesNo>? = null,

    val rareOrEndangeredEcosystemsExposure: DataPointOneValue<YesNo>? = null,
)
