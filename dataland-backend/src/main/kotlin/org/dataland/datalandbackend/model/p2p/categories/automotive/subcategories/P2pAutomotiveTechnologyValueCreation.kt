package org.dataland.datalandbackend.model.p2p.categories.automotive.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding the value creation of the automotive sector
 */
data class P2pAutomotiveTechnologyValueCreation(
    val driveMixInPercent: BigDecimal? = null,

    val icAndHybridEnginePhaseOutDate: LocalDate? = null,

    val futureValueCreationStrategy: YesNo? = null,
)
