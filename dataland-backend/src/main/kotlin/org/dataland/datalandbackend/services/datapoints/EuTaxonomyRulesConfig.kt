package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackend.services.datapoints.EuTaxonomyRulesConfig.ALIGNED_DATA_POINT_TYPES
import org.dataland.datalandbackend.services.datapoints.EuTaxonomyRulesConfig.ELIGIBLE_DATA_POINT_TYPES
import org.dataland.datalandbackend.services.datapoints.EuTaxonomyRulesConfig.FOSSIL_GAS_ACTIVITIES
import org.dataland.datalandbackend.services.datapoints.EuTaxonomyRulesConfig.FOSSIL_GAS_DATA_POINT_TYPES
import org.dataland.datalandbackend.services.datapoints.EuTaxonomyRulesConfig.NUCLEAR_ACTIVITIES
import org.dataland.datalandbackend.services.datapoints.EuTaxonomyRulesConfig.NUCLEAR_DATA_POINT_TYPES
import org.dataland.datalandbackendutils.model.DataPointType

/**
 * Configuration of the EU taxonomy activity groups that are relevant for the EU taxonomy share conversions.
 */
internal object EuTaxonomyRulesConfig {
    /**
     * The data point base type identifying the non-aligned activities input of an EU taxonomy rules.
     */
    const val NON_ALIGNED_ACTIVITIES_BASE_TYPE = "extendedEuTaxonomyNonAlignedActivitiesComponent"

    /**
     * The data point base type identifying the aligned activities input of an EU taxonomy rules.
     */
    const val ALIGNED_ACTIVITIES_BASE_TYPE = "extendedEuTaxonomyAlignedActivitiesComponent"

    /**
     * The data point base type identifying the eligible or aligned activities input of an EU taxonomy rules.
     */
    const val ELIGIBLE_OR_ALIGNED_ACTIVITIES_BASE_TYPE = "extendedEuTaxonomyEligibleOrAlignedActivitiesComponent"

    val FOSSIL_GAS_DATA_POINT_TYPES =
        listOf(
            "relativeShareOfFossilGasActivitiesInPercentEligible",
            "relativeShareOfFossilGasActivitiesInPercentAligned",
        )

    val NUCLEAR_DATA_POINT_TYPES =
        listOf(
            "relativeShareOfNuclearActivitiesInPercentEligible",
            "relativeShareOfNuclearActivitiesInPercentAligned",
        )

    val ALIGNED_DATA_POINT_TYPES =
        listOf(
            "relativeShareOfFossilGasActivitiesInPercentAligned",
            "relativeShareOfNuclearActivitiesInPercentAligned",
        )

    val ELIGIBLE_DATA_POINT_TYPES =
        listOf(
            "relativeShareOfFossilGasActivitiesInPercentEligible",
            "relativeShareOfNuclearActivitiesInPercentEligible",
        )

    /**
     * Nuclear energy activities as defined by the EU taxonomy complementary climate delegated act.
     */
    @Suppress("ktlint:standard:max-line-length", "MaxLineLength", "kotlin:S103")
    val NUCLEAR_ACTIVITIES: List<Activity> =
        listOf(
            Activity
                .PreCommercialStagesOfAdvancedTechnologiesToProduceEnergyFromNuclearProcessesWithMinimalWasteFromTheFuelCycle,
            Activity
                .ConstructionAndSafeOperationOfNewNuclearPowerPlantsForTheGenerationOfElectricityAndOrHeatIncludingForHydrogenProductionUsingBestAvailableTechnologies,
            Activity.ElectricityGenerationFromNuclearEnergyInExistingInstallations,
        )

    /**
     * Fossil gas activities as defined by the EU taxonomy complementary climate delegated act.
     */
    val FOSSIL_GAS_ACTIVITIES: List<Activity> =
        listOf(
            Activity.ElectricityGenerationFromFossilGaseousFuels,
            Activity.HighEfficiencyCoGenerationOfHeatCoolAndPowerFromFossilGaseousFuels,
            Activity.ProductionOfHeatCoolFromFossilGaseousFuelsInAnEfficientDistrictHeatingAndCoolingSystem,
        )
}

/**
 * Describes which share has to be computed for a given target data point type.
 *
 * @param activities the EU taxonomy activities the share is restricted to
 * @param isAligned true if the aligned share has to be computed, false for the eligible share
 */
internal data class EuTaxonomyShareRule(
    val activities: List<Activity>,
    val isAligned: Boolean,
)

/**
 * Resolves the share rule that applies to [targetType].
 *
 * @param targetType the data point type the conversion produces
 * @return the rule describing the activity group and whether the aligned or the eligible share is requested
 */
internal fun resolveEuTaxonomyShareRule(targetType: DataPointType): EuTaxonomyShareRule =
    EuTaxonomyShareRule(
        when {
            FOSSIL_GAS_DATA_POINT_TYPES.any { targetType.contains(it, true) } -> FOSSIL_GAS_ACTIVITIES
            NUCLEAR_DATA_POINT_TYPES.any { targetType.contains(it, true) } -> NUCLEAR_ACTIVITIES
            else -> throw IllegalArgumentException("$targetType is not supported")
        },
        when {
            ALIGNED_DATA_POINT_TYPES.any { targetType.contains(it, true) } -> true
            ELIGIBLE_DATA_POINT_TYPES.any { targetType.contains(it, true) } -> false
            else -> throw IllegalArgumentException("$targetType is not supported")
        },
    )
