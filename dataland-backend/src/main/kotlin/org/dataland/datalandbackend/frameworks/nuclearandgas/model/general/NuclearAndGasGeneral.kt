// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.nuclearandgas.model.general

import jakarta.validation.Valid
import org.dataland.datalandbackend.frameworks.nuclearandgas.model.general.general.NuclearAndGasGeneralGeneral
import org.dataland.datalandbackend.frameworks.nuclearandgas.model.general.taxonomyAlignedDenominator
    .NuclearAndGasGeneralTaxonomyAlignedDenominator
import org.dataland.datalandbackend.frameworks.nuclearandgas.model.general.taxonomyAlignedNumerator
    .NuclearAndGasGeneralTaxonomyAlignedNumerator
import org.dataland.datalandbackend.frameworks.nuclearandgas.model.general.taxonomyEligibleButNotAligned
    .NuclearAndGasGeneralTaxonomyEligibleButNotAligned
import org.dataland.datalandbackend.frameworks.nuclearandgas.model.general.taxonomyNonEligible
    .NuclearAndGasGeneralTaxonomyNonEligible

/**
 * The data-model for the General section
 */
@Suppress("MaxLineLength")
data class NuclearAndGasGeneral(
    @field:Valid()
    val general: NuclearAndGasGeneralGeneral? = null,
    @field:Valid()
    val taxonomyAlignedDenominator: NuclearAndGasGeneralTaxonomyAlignedDenominator? = null,
    @field:Valid()
    val taxonomyAlignedNumerator: NuclearAndGasGeneralTaxonomyAlignedNumerator? = null,
    @field:Valid()
    val taxonomyEligibleButNotAligned: NuclearAndGasGeneralTaxonomyEligibleButNotAligned? = null,
    @field:Valid()
    val taxonomyNonEligible: NuclearAndGasGeneralTaxonomyNonEligible? = null,
)
