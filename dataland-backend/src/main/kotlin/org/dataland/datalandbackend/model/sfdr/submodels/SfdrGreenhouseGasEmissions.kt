package org.dataland.datalandbackend.model.sfdr.submodels

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Greenhouse Gas Emissions"
 */
data class SfdrGreenhouseGasEmissions(
    val scope1: DataPoint<BigDecimal>?,

    val scope2: DataPoint<BigDecimal>?,

    val scope3: DataPoint<BigDecimal>?,

    val enterpriseValue: DataPoint<BigDecimal>?,

    val totalRevenue: DataPoint<BigDecimal>?,

    val fossilFuelSectorExposure: DataPoint<YesNo>?,
)
