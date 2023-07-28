package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import org.dataland.datalandbackend.model.DataPointWithUnit
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Anti-corruption and anti-bribery" belonging to the category "Social" of the sfdr framework.
 */
data class SfdrSocialAntiCorruptionAndAntiBribery(
    val reportedCasesOfBriberyCorruption: DataPointWithUnit<BigDecimal>? = null,

    val reportedConvictionsOfBriberyCorruption: DataPointWithUnit<BigDecimal>? = null,

    val reportedFinesOfBriberyCorruption: DataPointWithUnit<BigDecimal>? = null,
)
