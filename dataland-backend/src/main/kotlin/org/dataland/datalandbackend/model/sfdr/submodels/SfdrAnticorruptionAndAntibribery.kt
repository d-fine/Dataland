package org.dataland.datalandbackend.model.sfdr.submodels

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Anti-corruption and anti-bribery"
 */
data class SfdrAnticorruptionAndAntibribery(
    val reportedCasesOfBriberyCorruption: DataPoint<YesNo>?,

    val reportedConvictionsOfBriberyCorruption: DataPoint<BigDecimal>?,

    val reportedFinesOfBriberyCorruption: DataPoint<BigDecimal>?,
)
