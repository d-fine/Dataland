// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandqaservice.frameworks.eutaxonomynonfinancials.model.capex

import jakarta.validation.Valid
import org.dataland.datalandbackend.openApiClient.model.CurrencyDataPoint
import org.dataland.datalandbackend.openApiClient.model.ExtendedDataPointBigDecimal
import org.dataland.datalandbackend.openApiClient.model.ExtendedDataPointListEuTaxonomyActivity
import org.dataland.datalandbackend.openApiClient.model.ExtendedDataPointListEuTaxonomyAlignedActivity
import org.dataland.datalandqaservice.frameworks.eutaxonomynonfinancials.model.capex.alignedShare
    .EutaxonomyNonFinancialsCapexAlignedShare
import org.dataland.datalandqaservice.frameworks.eutaxonomynonfinancials.model.capex.eligibleShare
    .EutaxonomyNonFinancialsCapexEligibleShare
import org.dataland.datalandqaservice.frameworks.eutaxonomynonfinancials.model.capex.nonAlignedShare
    .EutaxonomyNonFinancialsCapexNonAlignedShare
import org.dataland.datalandqaservice.frameworks.eutaxonomynonfinancials.model.capex.nonEligibleShare
    .EutaxonomyNonFinancialsCapexNonEligibleShare
import org.dataland.datalandqaservice.model.reports.QaReportDataPoint

/**
 * The QA-model for the Capex section
 */
data class EutaxonomyNonFinancialsCapex(
    val totalAmount: QaReportDataPoint<CurrencyDataPoint?>? = null,
    @field:Valid()
    val nonEligibleShare: EutaxonomyNonFinancialsCapexNonEligibleShare? = null,
    @field:Valid()
    val eligibleShare: EutaxonomyNonFinancialsCapexEligibleShare? = null,
    @field:Valid()
    val nonAlignedShare: EutaxonomyNonFinancialsCapexNonAlignedShare? = null,
    val nonAlignedActivities: QaReportDataPoint<ExtendedDataPointListEuTaxonomyActivity?>? = null,
    @field:Valid()
    val alignedShare: EutaxonomyNonFinancialsCapexAlignedShare? = null,
    @field:Valid()
    val substantialContributionToClimateChangeMitigationInPercentEligible: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToClimateChangeMitigationInPercentAligned: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToClimateChangeMitigationInPercentOfWhichUseOfProceeds:
        QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToClimateChangeMitigationInPercentEnablingShare: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToClimateChangeMitigationInPercentTransitionalShare: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToClimateChangeAdaptationInPercentEligible: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToClimateChangeAdaptationInPercentAligned: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToClimateChangeAdaptationInPercentOfWhichUseOfProceeds:
        QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToClimateChangeAdaptationInPercentEnablingShare: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercentEligible:
        QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercentAligned:
        QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercentOfWhichUseOfProceeds:
        QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercentEnablingShare:
        QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToTransitionToACircularEconomyInPercentEligible: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToTransitionToACircularEconomyInPercentAligned: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToTransitionToACircularEconomyInPercentOfWhichUseOfProceeds:
        QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToTransitionToACircularEconomyInPercentEnablingShare:
        QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToPollutionPreventionAndControlInPercentEligible: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToPollutionPreventionAndControlInPercentAligned: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToPollutionPreventionAndControlInPercentOfWhichUseOfProceeds:
        QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToPollutionPreventionAndControlInPercentEnablingShare:
        QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercentEligible:
        QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercentAligned:
        QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercentOfWhichUseOfProceeds:
        QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercentEnablingShare:
        QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    val alignedActivities: QaReportDataPoint<ExtendedDataPointListEuTaxonomyAlignedActivity?>? = null,
    @field:Valid()
    val enablingShareInPercent: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
    @field:Valid()
    val transitionalShareInPercent: QaReportDataPoint<ExtendedDataPointBigDecimal?>? = null,
)
