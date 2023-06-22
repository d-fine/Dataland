package org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production
/**
 * --- API model ---
 * Fields of the LKSG questionnaire describing the number of suppliers a company has per country
 */
data class LksgCountryAssociatedSuppliers(
    val country: String?, // TODO should be required. Is not in data dictionary.
    val numberOfSuppliers: Int?,
)
