package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyAlignedActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.RelativeAndAbsoluteFinancialShare
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials202673.custom.EuTaxonomyEligibleOrAlignedActivity
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackend.model.generics.AmountWithCurrency
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.reflect.full.memberProperties
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint as ExtendedDataPointInterface

private const val CALCULATION_SCALE = 10
private val CALCULATION_ROUNDING_MODE = RoundingMode.HALF_UP

private const val NON_ALIGNED_ACTIVITIES_BASE_TYPE = "extendedEuTaxonomyNonAlignedActivitiesComponent"
private const val ALIGNED_ACTIVITIES_BASE_TYPE = "extendedEuTaxonomyAlignedActivitiesComponent"

internal data class SumOperands<T : ExtendedDataPointInterface<BigDecimal>>(
    val dataPoints: List<T>,
    val values: List<BigDecimal>,
)

internal inline fun <reified T : ExtendedDataPointInterface<BigDecimal>> extractSumOperands(
    inputs: Collection<UploadedDataPoint>,
): SumOperands<T> {
    val dataPoints = inputs.map { defaultObjectMapper.readValue<T>(it.dataPoint) }
    require(dataPoints.size >= 2) { "At least two data points must be provided for summation." }
    val values =
        dataPoints.map {
            requireNotNull(it.value) { "Data points for summation must not have null value fields." }
        }
    return SumOperands(dataPoints = dataPoints, values = values)
}

internal inline fun <reified T : ExtendedDataPointInterface<*>> extractIdentityOperand(inputs: Collection<UploadedDataPoint>): T {
    val dataPoints = inputs.map { defaultObjectMapper.readValue<T>(it.dataPoint) }
    require(dataPoints.size == 1) { "Exactly one data point must be provided for the identity rule." }
    return dataPoints.single()
}

internal data class DivisionOperands<N : ExtendedDataPointInterface<BigDecimal>, D : ExtendedDataPointInterface<BigDecimal>>(
    val numerator: N,
    val denominator: D,
    val numeratorValue: BigDecimal,
    val denominatorValue: BigDecimal,
) {
    fun calculateValue(multiplier: BigDecimal): BigDecimal =
        numeratorValue.multiply(multiplier).divide(
            denominatorValue,
            CALCULATION_SCALE,
            CALCULATION_ROUNDING_MODE,
        )
}

internal inline fun <
    reified N : ExtendedDataPointInterface<BigDecimal>,
    reified D : ExtendedDataPointInterface<BigDecimal>,
> extractDivisionOperands(
    inputs: Collection<UploadedDataPoint>,
    operationName: String,
): DivisionOperands<N, D> {
    val nullValueErrorMessage = "Data points for $operationName must not have null value fields."
    require(inputs.size == 2) { "Exactly two data points must be provided for $operationName." }
    val numerator = defaultObjectMapper.readValue<N>(inputs.elementAt(0).dataPoint)
    val denominator = defaultObjectMapper.readValue<D>(inputs.elementAt(1).dataPoint)
    val numeratorValue = requireNotNull(numerator.value) { nullValueErrorMessage }
    val denominatorValue = requireNotNull(denominator.value) { nullValueErrorMessage }
    require(denominatorValue.signum() != 0) { "The divisor in $operationName must not be zero." }
    return DivisionOperands(
        numerator = numerator,
        denominator = denominator,
        numeratorValue = numeratorValue,
        denominatorValue = denominatorValue,
    )
}

internal data class EuTaxonomyActivityOperands<
    N : ExtendedDataPointInterface<Iterable<EuTaxonomyActivity>?>?,
    A : ExtendedDataPointInterface<Iterable<EuTaxonomyAlignedActivity>?>?,
>(
    val nonAlignedActivities: N?,
    val alignedActivities: A?,
    val nonAlignedActivitiesValue: Iterable<EuTaxonomyActivity>?,
    val alignedActivitiesValue: Iterable<EuTaxonomyAlignedActivity>?,
) {
    /**
     * Merges the non-aligned and aligned activity lists into a single list of eligible-or-aligned activities,
     * combining entries that share the same activity name, NACE codes, and currency.
     *
     * @return a [Pair] of the merged activities (or `null` if neither list contains any entries) and the names of the
     *   activities for which conflicting substantial contributions were detected and removed
     */
    fun mergeLists(): Pair<List<EuTaxonomyEligibleOrAlignedActivity>?, List<Activity>> {
        val nonAlignedActivitiesMap = groupActivitiesByIdentifier(nonAlignedActivitiesValue) { activityIdentifier(it) }
        val alignedActivitiesMap = groupActivitiesByIdentifier(alignedActivitiesValue) { activityIdentifier(it) }
        val identifiers = nonAlignedActivitiesMap?.keys.orEmpty() + alignedActivitiesMap?.keys.orEmpty()

        val eligibleOrAlignedActivities: MutableList<EuTaxonomyEligibleOrAlignedActivity> = mutableListOf()
        val activitiesWithConflictingSubstantialContributions: MutableList<Activity> = mutableListOf()
        for (identifier in identifiers) {
            val mergedActivity =
                buildMergedActivity(
                    identifier = identifier,
                    alignedActivities = alignedActivitiesMap?.get(identifier),
                    nonAlignedActivities = nonAlignedActivitiesMap?.get(identifier),
                ) ?: continue
            val (adjustedActivity, hadConflict) = mergedActivity
            if (hadConflict) {
                activitiesWithConflictingSubstantialContributions.add(adjustedActivity.activityName)
            }
            eligibleOrAlignedActivities.add(adjustedActivity)
        }
        return Pair(eligibleOrAlignedActivities.takeIf { it.isNotEmpty() }, activitiesWithConflictingSubstantialContributions)
    }
}

/**
 * Groups [activities] by the identifier derived from each entry via [identifierOf], mirroring the grouping used to
 * match non-aligned and aligned activities sharing the same activity name, NACE codes, and currency.
 */
private fun <T> groupActivitiesByIdentifier(
    activities: Iterable<T>?,
    identifierOf: (T) -> Triple<Activity, Set<String>?, String?>,
): Map<Triple<Activity, Set<String>?, String?>, List<T>>? = activities?.groupBy(identifierOf)

private fun activityIdentifier(activity: EuTaxonomyActivity): Triple<Activity, Set<String>?, String?> =
    Triple(activity.activityName, activity.naceCodes?.toSet(), activity.share?.absoluteShare?.currency)

private fun activityIdentifier(activity: EuTaxonomyAlignedActivity): Triple<Activity, Set<String>?, String?> =
    Triple(activity.activityName, activity.naceCodes?.toSet(), activity.share?.absoluteShare?.currency)

/**
 * Builds the merged [EuTaxonomyEligibleOrAlignedActivity] for a single activity [identifier] from the aligned and
 * non-aligned activities sharing that identifier, resolving any substantial contribution conflict.
 *
 * @param identifier the activity name, NACE codes, and currency shared by the merged activities
 * @param alignedActivities the aligned activities sharing [identifier], or `null` if there are none
 * @param nonAlignedActivities the non-aligned activities sharing [identifier], or `null` if there are none
 * @return a pair of the merged activity and whether a substantial contribution conflict was resolved for it, or
 *   `null` if the merged activity is not relevant (see [isRelevant])
 */
private fun buildMergedActivity(
    identifier: Triple<Activity, Set<String>?, String?>,
    alignedActivities: List<EuTaxonomyAlignedActivity>?,
    nonAlignedActivities: List<EuTaxonomyActivity>?,
): Pair<EuTaxonomyEligibleOrAlignedActivity, Boolean>? {
    val alignedAbsoluteShare =
        determineAlignedAbsoluteShare(
            alignedActivities = alignedActivities,
            currency = identifier.third,
        )
    val alignedRelativeShare = determineAlignedRelativeShare(alignedActivities)
    val nonAlignedRelativeShare = determineNonAlignedRelativeShare(nonAlignedActivities)
    val relativeEligibleShareInPercent =
        if (alignedRelativeShare == null && nonAlignedRelativeShare == null) {
            null
        } else {
            (alignedRelativeShare ?: BigDecimal.ZERO) + (nonAlignedRelativeShare ?: BigDecimal.ZERO)
        }
    val activity =
        createEuTaxonomyEligibleOrAlignedActivity(
            identifier,
            alignedAbsoluteShare,
            alignedRelativeShare,
            relativeEligibleShareInPercent,
            alignedActivities,
        )
    val (adjustedActivity, hadConflict) = removeConflictingSubstantialContribution(activity)
    return if (isRelevant(adjustedActivity)) adjustedActivity to hadConflict else null
}

/**
 * Checks whether [activity] carries no meaningful data beyond identification, i.e. it returns true if there is a property except
 * `activityName` and `naceCodes` that is not `null`.
 */
private fun isRelevant(activity: EuTaxonomyEligibleOrAlignedActivity): Boolean =
    EuTaxonomyEligibleOrAlignedActivity::class
        .memberProperties
        .filter { it.name != "activityName" && it.name != "naceCodes" }
        .any { property -> property.get(activity) != null }

/**
 * Checks the substantial contribution fields (the `BigDecimal?` fields named `substantialContributionTo...InPercent`) of
 * [activity]. If more than one of them is non-null, returns a copy of [activity] with all of them set to `null` together
 * with `true` to signal that a conflict was resolved; otherwise returns [activity] unchanged together with `false`.
 */
private fun removeConflictingSubstantialContribution(
    activity: EuTaxonomyEligibleOrAlignedActivity,
): Pair<EuTaxonomyEligibleOrAlignedActivity, Boolean> {
    val substantialContributionProperties =
        EuTaxonomyEligibleOrAlignedActivity::class
            .memberProperties
            .filter { it.name.startsWith("substantialContributionTo") }
    val nonNullCount = substantialContributionProperties.count { property -> property.get(activity) != null }
    return if (nonNullCount > 1) {
        activity.copy(
            substantialContributionToClimateChangeMitigationInPercent = null,
            substantialContributionToClimateChangeAdaptationInPercent = null,
            substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent = null,
            substantialContributionToTransitionToACircularEconomyInPercent = null,
            substantialContributionToPollutionPreventionAndControlInPercent = null,
            substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent = null,
        ) to true
    } else {
        activity to false
    }
}

/**
 * Computes the combined absolute share for aligned activities, or `null` if none report one.
 */
private fun determineAlignedAbsoluteShare(
    alignedActivities: List<EuTaxonomyAlignedActivity>?,
    currency: String?,
): AmountWithCurrency? =
    if (alignedActivities == null ||
        alignedActivities.all { it.share?.absoluteShare == null } ||
        alignedActivities.all { it.share?.absoluteShare?.amount == null }
    ) {
        null
    } else {
        AmountWithCurrency(
            // When no aligned activity with identifier exist or all share.absoluteShare.amount are null, return null
            amount = alignedActivities.sumOf { it.share?.absoluteShare?.amount ?: BigDecimal.ZERO },
            currency = currency,
        )
    }

/**
 * Sums the relative share in percent across the aligned activities sharing an identifier.
 * See also [determineNonAlignedRelativeShare] for non-aligned activities.

 */
private fun determineAlignedRelativeShare(alignedActivities: List<EuTaxonomyAlignedActivity>?): BigDecimal? =
    alignedActivities?.mapNotNull { it.share?.relativeShareInPercent }?.takeIf { it.isNotEmpty() }?.sumOf { it }

/**
 * Sums the relative share in percent across the non-aligned activities sharing an identifier.
 * See also [determineAlignedRelativeShare] for aligned activities.
 */
private fun determineNonAlignedRelativeShare(nonAlignedActivities: List<EuTaxonomyActivity>?): BigDecimal? =
    nonAlignedActivities?.mapNotNull { it.share?.relativeShareInPercent }?.takeIf { it.isNotEmpty() }?.sumOf { it }

/**
 * Builds a single merged [EuTaxonomyEligibleOrAlignedActivity] for one activity [identifier], combining the
 * pre-computed shares with the per-criterion substantial contributions and flags of the aligned activities
 * sharing that identifier.
 *
 * @param identifier the activity name, NACE codes, and currency shared by the merged activities
 * @param alignedAbsoluteShare the combined absolute share reported by the aligned activities, or `null` if none
 * @param alignedRelativeShare the combined relative share in percent reported by the aligned activities, or `null` if none
 * @param relativeEligibleShareInPercent the combined eligible share in percent across aligned and non-aligned activities
 * @param alignedActivities the aligned activities sharing [identifier], used to derive the substantial
 *   contributions and yes/no flags
 * @return the merged eligible-or-aligned activity
 */
private fun createEuTaxonomyEligibleOrAlignedActivity(
    identifier: Triple<Activity, Set<String>?, String?>,
    alignedAbsoluteShare: AmountWithCurrency?,
    alignedRelativeShare: BigDecimal?,
    relativeEligibleShareInPercent: BigDecimal?,
    alignedActivities: List<EuTaxonomyAlignedActivity>?,
): EuTaxonomyEligibleOrAlignedActivity =
    EuTaxonomyEligibleOrAlignedActivity(
        activityName = identifier.first,
        naceCodes = identifier.second?.toList(),
        relativeEligibleShareInPercent = relativeEligibleShareInPercent,
        share =
            if (alignedActivities == null || alignedActivities.all { it.share == null }) {
                null
            } else {
                RelativeAndAbsoluteFinancialShare(
                    absoluteShare = alignedAbsoluteShare,
                    relativeShareInPercent = alignedRelativeShare,
                )
            },
        substantialContributionToClimateChangeMitigationInPercent =
            determineSubstantialContributions(
                alignedActivities?.map { it.substantialContributionToClimateChangeMitigationInPercent },
                alignedRelativeShare,
            ),
        substantialContributionToClimateChangeAdaptationInPercent =
            determineSubstantialContributions(
                alignedActivities?.map { it.substantialContributionToClimateChangeAdaptationInPercent },
                alignedRelativeShare,
            ),
        substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent =
            determineSubstantialContributions(
                alignedActivities?.map {
                    it.substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent
                },
                alignedRelativeShare,
            ),
        substantialContributionToTransitionToACircularEconomyInPercent =
            determineSubstantialContributions(
                alignedActivities?.map { it.substantialContributionToTransitionToACircularEconomyInPercent },
                alignedRelativeShare,
            ),
        substantialContributionToPollutionPreventionAndControlInPercent =
            determineSubstantialContributions(
                alignedActivities?.map { it.substantialContributionToPollutionPreventionAndControlInPercent },
                alignedRelativeShare,
            ),
        substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent =
            determineSubstantialContributions(
                alignedActivities?.map {
                    it.substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent
                },
                alignedRelativeShare,
            ),
        enablingActivity = determineYesNoActivity(alignedActivities?.map { it.enablingActivity }),
        transitionalActivity = determineYesNoActivity(alignedActivities?.map { it.transitionalActivity }),
    )

/**
 * Determines the merged substantial contribution for a single criterion from the aligned activities sharing
 * an identifier, using the highest reported value (`null` is treated as lower than any value).
 * This implements the rules for the mapping of substantial contribution criteria as defined
 * [here](https://github.com/d-fine/Dataland/wiki/EU-Taxonomy-mapping-for-non%E2%80%90financial-undertakings:-old-template-%E2%86%92-new-template#mapping-to-eligible-or-aligned-activities-not-yet-implemented-for-eu-taxonomy-non-financials-202673---expected-implementation-date-mid-august)
 *
 * @param substantialContributions the per-aligned-activity contribution values for this criterion
 * @param alignedRelativeShare the value to return when the highest contribution is strictly positive
 * @return `null` if there is no aligned activity or all contributions are `null`; `0` if the highest
 *   contribution is `0`; otherwise [alignedRelativeShare]
 * @throws IllegalArgumentException if the highest contribution is negative
 */
@Suppress("ktlint:standard:max-line-length", "MaxLineLength", "kotlin:S103")
private fun determineSubstantialContributions(
    substantialContributions: List<BigDecimal?>?,
    alignedRelativeShare: BigDecimal?,
): BigDecimal? {
    val maxSubstantialContribution = substantialContributions?.maxWithOrNull(nullsFirst())
    return when {
        maxSubstantialContribution == null -> null

        maxSubstantialContribution > BigDecimal.ZERO -> alignedRelativeShare

        maxSubstantialContribution.compareTo(BigDecimal.ZERO) == 0 -> BigDecimal.ZERO

        // comparesTo only checks for equality in value.
        else -> throw IllegalArgumentException("A substantial contribution must not be negative.")
    }
}

/**
 * Determines the merged yes/no flag for a set of aligned activities sharing an identifier, using the
 * strictest reported value under the order `null < No < Yes`.
 *
 * @param activities the per-aligned-activity flag values
 * @return the highest-ranked value in [activities] according to `null < No < Yes`, or `null` if [activities]
 *   is `null` or empty
 */
private fun determineYesNoActivity(activities: List<YesNo?>?): YesNo? {
    val yesNoOrder =
        listOf(null, YesNo.No, YesNo.Yes)
    return activities?.maxByOrNull { yesNoOrder.indexOf(it) }
}

/**
 * Resolves the dataPointBaseType id of [dataPointType] using [specs].
 *
 * @param dataPointType the data point type to inspect
 * @param specs the data point type specifications keyed by type
 * @return the id of the data point base type, or null if unknown
 */
internal fun getDataPointBaseTypeId(
    dataPointType: DataPointType,
    specs: Map<DataPointType, DataPointTypeSpecification>,
): String? = specs[dataPointType]?.dataPointBaseType?.id

/**
 * Extracts the non-aligned and aligned activity lists from [inputs] for the EU taxonomy activity merge.
 *
 * The two inputs are distinguished by their [UploadedDataPoint.dataPointType]'s data point base type, resolved via
 * [specs], rather than by their position in [inputs] or by their JSON content. Content-based discrimination (e.g.
 * trying to deserialize into the narrower non-aligned type and checking for failure) is not reliable, since a JSON
 * serializer producing the uploaded data may omit null fields entirely, making an aligned activity with all-null
 * aligned-only fields structurally indistinguishable from a non-aligned activity.
 *
 * @param inputs the two source data points to be merged
 * @param specs the data point type specifications used to resolve each input's role
 * @return an [EuTaxonomyActivityOperands] containing the extracted non-aligned and aligned activity operands
 */
internal inline fun <
    reified N : ExtendedDataPointInterface<Iterable<EuTaxonomyActivity>?>?,
    reified A : ExtendedDataPointInterface<Iterable<EuTaxonomyAlignedActivity>?>?,
> extractEuTaxonomyActivityLists(
    inputs: Collection<UploadedDataPoint>,
    specs: Map<DataPointType, DataPointTypeSpecification>,
): EuTaxonomyActivityOperands<N, A> {
    require(inputs.size == 2) { "Exactly two data points must be provided for the merged inputs." }

    val nonAlignedInput =
        inputs.singleOrNull { getDataPointBaseTypeId(it.dataPointType, specs) == NON_ALIGNED_ACTIVITIES_BASE_TYPE }
            ?: throw IllegalArgumentException(
                "Exactly one input of base type $NON_ALIGNED_ACTIVITIES_BASE_TYPE must be provided for the merge.",
            )
    val alignedInput =
        inputs.singleOrNull { getDataPointBaseTypeId(it.dataPointType, specs) == ALIGNED_ACTIVITIES_BASE_TYPE }
            ?: throw IllegalArgumentException(
                "Exactly one input of base type $ALIGNED_ACTIVITIES_BASE_TYPE must be provided for the merge.",
            )

    val nonAlignedActivities = defaultObjectMapper.readValue<N>(nonAlignedInput.dataPoint)
    val alignedActivities = defaultObjectMapper.readValue<A>(alignedInput.dataPoint)
    return EuTaxonomyActivityOperands(
        nonAlignedActivities = nonAlignedActivities,
        alignedActivities = alignedActivities,
        nonAlignedActivitiesValue = nonAlignedActivities?.value,
        alignedActivitiesValue = alignedActivities?.value,
    )
}
