package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Biodiversity" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalBiodiversity(
    val primaryForestAndWoodedLandOfNativeSpeciesExposure: ExtendedDataPoint<YesNo>? = null,

    val protectedAreasExposure: ExtendedDataPoint<YesNo>? = null,

    val rareOrEndangeredEcosystemsExposure: ExtendedDataPoint<YesNo>? = null,

    val highlyBiodiverseGrasslandExposure: ExtendedDataPoint<YesNo>? = null,
)
