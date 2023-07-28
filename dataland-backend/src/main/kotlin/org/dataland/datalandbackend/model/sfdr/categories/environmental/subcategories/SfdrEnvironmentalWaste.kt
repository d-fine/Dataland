package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.DataPointWithUnit
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Waste" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalWaste(
    val hazardousWaste: DataPointWithUnit<BigDecimal>? = null,

    val manufactureOfAgrochemicalPesticidesProducts: DataPoint<YesNo>? = null,

    val landDegradationDesertificationSoilSealingExposure: DataPoint<YesNo>? = null,

    val sustainableAgriculturePolicy: DataPoint<YesNo>? = null,

    val sustainableOceansAndSeasPolicy: DataPoint<YesNo>? = null,

    val wasteNonRecycled: DataPoint<BigDecimal>? = null,

    val threatenedSpeciesExposure: DataPoint<YesNo>? = null,

    val biodiversityProtectionPolicy: DataPoint<YesNo>? = null,

    val deforestationPolicy: DataPoint<YesNo>? = null,
)
