package org.dataland.datalandbackend.model.sme.categories.insurances.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.sme.NaturalHazard
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Natural Hazards" belonging to the category "Insurances" of the sme framework.
*/
data class SmeInsurancesNaturalHazards(
    val insuranceAgainstNaturalHazards: YesNo? = null,

    val amountCoveredByInsuranceAgainstNaturalHazards: BigDecimal? = null,

    val naturalHazardsCovered: NaturalHazard? = null,
)
