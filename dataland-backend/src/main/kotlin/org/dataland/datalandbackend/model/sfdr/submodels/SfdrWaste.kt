package org.dataland.datalandbackend.model.sfdr.submodels

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Waste"
 */
data class SfdrWaste(
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
