// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model

import jakarta.validation.Valid
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.capex.EutaxonomyNonFinancialsCapex
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.general.EutaxonomyNonFinancialsGeneral
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.opex.EutaxonomyNonFinancialsOpex
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.revenue.EutaxonomyNonFinancialsRevenue
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.model.validator.ReferencedReportsListValidator

/**
 * The root data-model for the Eutaxonomy-non-financials Framework
 */
@Suppress("MagicNumber", "MaxLineLength")
@DataType("eutaxonomy-non-financials", 3)
@ReferencedReportsListValidator()
data class EutaxonomyNonFinancialsData(
    @field:Valid()
    val general: EutaxonomyNonFinancialsGeneral? = null,
    @field:Valid()
    val revenue: EutaxonomyNonFinancialsRevenue? = null,
    @field:Valid()
    val capex: EutaxonomyNonFinancialsCapex? = null,
    @field:Valid()
    val opex: EutaxonomyNonFinancialsOpex? = null,
)
