package org.dataland.datalandbackend.model.lksg.categories.general.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Production-specific - Own operations"
 */
data class LksgProductionspecificOwnOperations(
    val mostImportantProducts: List<String>?,

    val productionSteps: List<String>?,

    val relatedCorporateSupplyChain: List<String>?,

    val productCategories: List<String>?,

    val definitionProductTypeService: List<String>?,

    val sourcingCountryPerCategory: List<String>?,

    val numberOfDirectSuppliers: BigDecimal?,

    val orderVolumePerProcurement: BigDecimal?,
)
