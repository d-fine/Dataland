package org.dataland.datalandbackend.model.sfdr

import java.math.BigDecimal
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Emissions"
 */
data class Emissions(
    val inorganicPollutants: DataPoint<BigDecimal>?,

    val airPollutants: DataPoint<BigDecimal>?,

    val ozoneDepletionSubstances: DataPoint<BigDecimal>?,

    val carbonReductionInitiatives: DataPoint<YesNo>?,
)
