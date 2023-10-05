package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.DataPointOneValue
import org.dataland.datalandbackend.model.DataPointWithUnit
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Waste" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalWaste(
    val hazardousAndRadioactiveWasteInTonnes: DataPointWithUnit<BigDecimal>? = null,

    val manufactureOfAgrochemicalPesticidesProducts: DataPointOneValue<YesNo>? = null,

    val landDegradationDesertificationSoilSealingExposure: DataPointOneValue<YesNo>? = null,

    val sustainableAgriculturePolicy: DataPointOneValue<YesNo>? = null,

    val sustainableOceansAndSeasPolicy: DataPointOneValue<YesNo>? = null,

    val nonRecycledWasteInTonnes: DataPointWithUnit<BigDecimal>? = null,

    val threatenedSpeciesExposure: DataPointOneValue<YesNo>? = null,

    val biodiversityProtectionPolicy: DataPointOneValue<YesNo>? = null,

    val deforestationPolicy: DataPointOneValue<YesNo>? = null,
)
