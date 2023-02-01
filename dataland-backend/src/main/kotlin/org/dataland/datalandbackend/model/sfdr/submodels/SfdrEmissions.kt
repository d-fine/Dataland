package org.dataland.datalandbackend.model.sfdr.submodels

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Emissions"
 */
data class SfdrEmissions(
    val inorganicPollutants: DataPoint<BigDecimal>?,

    val airPollutants: DataPoint<BigDecimal>?,

    val ozoneDepletionSubstances: DataPoint<BigDecimal>?,

    val carbonReductionInitiatives: DataPoint<YesNo>?,
)
