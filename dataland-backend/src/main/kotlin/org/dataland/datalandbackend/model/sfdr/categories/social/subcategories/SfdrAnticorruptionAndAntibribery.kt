package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import org.dataland.datalandbackend.model.DataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Anti-corruption and anti-bribery" belonging to the category "Social" of the sfdr framework.
 */
data class SfdrSocialAntiCorruptionAndAntiBribery(
    val reportedCasesOfBriberyCorruption: DataPoint<BigDecimal>? = null,

    val reportedConvictionsOfBriberyCorruption: DataPoint<BigDecimal>? = null,

    val reportedFinesOfBriberyCorruption: DataPoint<BigDecimal>? = null,
)
