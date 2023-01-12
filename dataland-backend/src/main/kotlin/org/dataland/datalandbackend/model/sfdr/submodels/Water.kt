package org.dataland.datalandbackend.model.sfdr.submodels

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Water"
 */
data class Water(
    val emissionsToWater: DataPoint<BigDecimal>? = null,

    val waterConsumption: DataPoint<BigDecimal>? = null,

    val waterReused: DataPoint<BigDecimal>? = null,

    val waterManagementPolicy: DataPoint<YesNo>? = null,

    val waterStressAreaExposure: DataPoint<YesNo>? = null,
)
