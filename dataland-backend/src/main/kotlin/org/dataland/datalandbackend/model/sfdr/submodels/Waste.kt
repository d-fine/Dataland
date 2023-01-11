package org.dataland.datalandbackend.model.sfdr.submodels

import java.math.BigDecimal
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Waste"
 */
data class Waste(
    val hazardousWaste: DataPoint<BigDecimal>?,

    val manufactureOfAgrochemicalPesticidesProducts: DataPoint<YesNo>?,

    val landDegradationDesertificationSoilSealingExposure: DataPoint<YesNo>?,

    val sustainableAgriculturePolicy: DataPoint<YesNo>?,

    val sustainableOceansAndSeasPolicy: DataPoint<YesNo>?,

    val wasteNonRecycled: DataPoint<BigDecimal>?,

    val threatenedSpeciesExposure: DataPoint<YesNo>?,

    val biodiversityProtectionPolicy: DataPoint<YesNo>?,

    val deforestationPolicy: DataPoint<YesNo>?,
)
