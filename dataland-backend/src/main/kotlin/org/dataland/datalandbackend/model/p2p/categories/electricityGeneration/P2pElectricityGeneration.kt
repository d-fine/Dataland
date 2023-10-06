package org.dataland.datalandbackend.model.p2p.categories.electricityGeneration

import org.dataland.datalandbackend.model.p2p.categories.electricityGeneration.subcategories
    .P2pElectricityGenerationTechnology

/**
 * --- API model ---
 * Fields of the category "Electricity generation" of the p2p framework.
*/
data class P2pElectricityGeneration(
    val technology: P2pElectricityGenerationTechnology? = null,
)
