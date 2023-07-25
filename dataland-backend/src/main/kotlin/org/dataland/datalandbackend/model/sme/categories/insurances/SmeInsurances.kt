package org.dataland.datalandbackend.model.sme.categories.insurances

import org.dataland.datalandbackend.model.sme.categories.insurances.subcategories.SmeInsurancesNaturalHazards

/**
 * --- API model ---
 * Fields of the category "Insurances" of the sme framework.
*/
data class SmeInsurances(
    val naturalHazards: SmeInsurancesNaturalHazards? = null,
)
