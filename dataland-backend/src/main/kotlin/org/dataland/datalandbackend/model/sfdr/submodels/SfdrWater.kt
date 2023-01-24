package org.dataland.datalandbackend.model.sfdr.submodels

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Water"
 */
data class SfdrWater(
    val emissionsToWater: DataPoint<BigDecimal>?,

    val waterConsumption: DataPoint<BigDecimal>?,

    val waterReused: DataPoint<BigDecimal>?,

    val waterManagementPolicy: DataPoint<YesNo>?,

    val waterStressAreaExposure: DataPoint<YesNo>?,
)
