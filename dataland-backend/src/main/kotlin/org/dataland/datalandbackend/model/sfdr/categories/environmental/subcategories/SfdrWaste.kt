package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.DataPoint
import java.math.BigDecimal
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Waste" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalWaste(
    val hazardousWaste: DataPoint<BigDecimal>? = null,

    val manufactureOfAgrochemicalPesticidesProducts: DataPoint<YesNo>? = null,

    val landDegradationDesertificationSoilSealingExposure: DataPoint<YesNo>? = null,

    val sustainableAgriculturePolicy: DataPoint<YesNo>? = null,

    val sustainableOceansAndSeasPolicy: DataPoint<YesNo>? = null,

    val wasteNonRecycled: DataPoint<BigDecimal>? = null,

    val threatenedSpeciesExposure: DataPoint<YesNo>? = null,

    val biodiversityProtectionPolicy: DataPoint<YesNo>? = null,

    val deforestationPolicy: DataPoint<YesNo>? = null,
)
