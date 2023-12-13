package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Biodiversity" belonging to the category "Environmental" of the sfdr framework.
*/
data class SfdrEnvironmentalBiodiversity(
    @field:Valid
    val primaryForestAndWoodedLandOfNativeSpeciesExposure: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val protectedAreasExposure: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val rareOrEndangeredEcosystemsExposure: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val highlyBiodiverseGrasslandExposure: ExtendedDataPoint<YesNo>? = null,
)
