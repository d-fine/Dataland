// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandqaservice.frameworks.nuclearandgas.model.general.taxonomyEligibleButNotAligned

import org.dataland.datalandbackend.openApiClient.model.ExtendedDataPointNuclearAndGasEligibleButNotAligned
import org.dataland.datalandqaservice.model.reports.QaReportDataPoint

/**
 * The QA-model for the TaxonomyEligibleButNotAligned section
 */
data class NuclearAndGasGeneralTaxonomyEligibleButNotAligned(
    val nuclearAndGasTaxonomyEligibleButNotAlignedRevenue: QaReportDataPoint<ExtendedDataPointNuclearAndGasEligibleButNotAligned?>? = null,
    val nuclearAndGasTaxonomyEligibleButNotAlignedCapex: QaReportDataPoint<ExtendedDataPointNuclearAndGasEligibleButNotAligned?>? = null,
)
