package org.dataland.datalandbackend.model.lksg.categories.general.subcategories

import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Grievance mechanism - Own operations"
 */
data class LksgProductionSpecificOwnOperations(
    val mostImportantProducts: String?,

    val productionSteps: String?,

    val relatedCorporateSupplyChain: String?,

    val productCategories: String?,

    val definitionProductTypeService: String?,

    val sourcingCountryPerCategory: String?,

    val numberOfDirectSuppliers: BigDecimal?,

    val orderVolumePerProcurement: BigDecimal?,
)
