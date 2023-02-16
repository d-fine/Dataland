package org.dataland.datalandbackend.model.sfdr.submodels

import org.dataland.datalandbackend.model.DataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Anti-corruption and anti-bribery"
 */
data class SfdrAnticorruptionAndAntibribery(
    val reportedCasesOfBriberyCorruption: DataPoint<BigDecimal>?,

    val reportedConvictionsOfBriberyCorruption: DataPoint<BigDecimal>?,

    val reportedFinesOfBriberyCorruption: DataPoint<BigDecimal>?,
)
