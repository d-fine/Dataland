package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.ExtendedDataPoint
import org.dataland.datalandbackend.model.DataPointWithUnit
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Waste" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalWaste(
    val hazardousWaste: DataPointWithUnit<BigDecimal>? = null,

    val manufactureOfAgrochemicalPesticidesProducts: ExtendedDataPoint<YesNo>? = null,

    val landDegradationDesertificationSoilSealingExposure: ExtendedDataPoint<YesNo>? = null,

    val sustainableAgriculturePolicy: ExtendedDataPoint<YesNo>? = null,

    val sustainableOceansAndSeasPolicy: ExtendedDataPoint<YesNo>? = null,

    val wasteNonRecycled: DataPointWithUnit<BigDecimal>? = null,

    val threatenedSpeciesExposure: ExtendedDataPoint<YesNo>? = null,

    val biodiversityProtectionPolicy: ExtendedDataPoint<YesNo>? = null,

    val deforestationPolicy: ExtendedDataPoint<YesNo>? = null,
)
