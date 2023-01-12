package org.dataland.datalandbackend.model.sfdr.submodels

import java.math.BigDecimal
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Emissions"
 */
data class Emissions(
    val inorganicPollutants: DataPoint<BigDecimal>? = null,

    val airPollutants: DataPoint<BigDecimal>? = null,

    val ozoneDepletionSubstances: DataPoint<BigDecimal>? = null,

    val carbonReductionInitiatives: DataPoint<YesNo>? = null,
)
