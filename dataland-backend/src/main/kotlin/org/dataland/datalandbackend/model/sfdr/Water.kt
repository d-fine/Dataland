package org.dataland.datalandbackend.model.sfdr

import java.math.BigDecimal
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Water"
 */
data class Water(
    val emissionsToWater: DataPoint<BigDecimal>?,

    val waterConsumption: DataPoint<BigDecimal>?,

    val waterReused: DataPoint<BigDecimal>?,

    val waterManagementPolicy: DataPoint<YesNo>?,

    val waterStressAreaExposure: DataPoint<YesNo>?,
)
