package org.dataland.datalandbackend.model.sfdr.submodels

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Greenhouse Gas Emissions"
 */
data class GreenhouseGasEmissions(
    val scope1: DataPoint<BigDecimal>? = null,

    val scope2: DataPoint<BigDecimal>? = null,

    val scope3: DataPoint<BigDecimal>? = null,

    val enterpriseValue: DataPoint<BigDecimal>? = null,

    val totalRevenue: DataPoint<BigDecimal>? = null,

    val fossilFuelSectorExposure: DataPoint<YesNo>? = null,
)
