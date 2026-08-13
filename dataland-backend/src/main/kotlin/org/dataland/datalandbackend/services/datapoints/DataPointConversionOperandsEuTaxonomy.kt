package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyAlignedActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials202673.custom.EuTaxonomyEligibleOrAlignedActivity
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import java.math.BigDecimal
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint as ExtendedDataPointInterface

internal data class MergedListWithCommentData(
    val mergedList: List<EuTaxonomyEligibleOrAlignedActivity>,
    val conflictedSubstantialContributions: List<Activity>,
    val nullShares: List<Activity>,
)

/**
 * Computes the EU taxonomy share described by [rule] from the non-aligned and aligned activity lists of the EU
 * taxonomy 2020/852 framework.
 *
 * The eligible share is the sum of the non-aligned and the aligned relative shares of the activities in
 * [rule]; the aligned share is the sum of the aligned relative shares only.
 *
 * @param rule the rule describing the activity group and whether the aligned or the eligible share is requested
 * @param nonAlignedActivities the reported non-aligned activities, or `null` if none were reported
 * @param alignedActivities the reported aligned activities, or `null` if none were reported
 * @return the requested share
 */
internal fun calculateEuTaxonomy2020Share(
    rule: EuTaxonomyShareRule,
    nonAlignedActivities: Iterable<EuTaxonomyActivity>?,
    alignedActivities: Iterable<EuTaxonomyAlignedActivity>?,
): BigDecimal {
    val alignedShare =
        alignedActivities
            ?.filter { rule.activities.contains(it.activityName) }
            ?.sumOf { it.share?.relativeShareInPercent ?: BigDecimal.ZERO } ?: BigDecimal.ZERO
    if (rule.isAligned) return alignedShare
    val nonAlignedShare =
        nonAlignedActivities
            ?.filter { rule.activities.contains(it.activityName) }
            ?.sumOf { it.share?.relativeShareInPercent ?: BigDecimal.ZERO } ?: BigDecimal.ZERO
    return alignedShare + nonAlignedShare
}

/**
 * Computes the EU taxonomy share described by [rule] from the eligible-or-aligned activity list of the EU
 * taxonomy 2026/73 framework.
 *
 * @param rule the rule describing the activity group and whether the aligned or the eligible share is requested
 * @param activities the reported eligible-or-aligned activities, or `null` if none were reported
 * @return the requested share
 */
internal fun calculateEuTaxonomy2026Share(
    rule: EuTaxonomyShareRule,
    activities: Iterable<EuTaxonomyEligibleOrAlignedActivity>?,
): BigDecimal =
    if (rule.isAligned) {
        activities
            ?.filter { rule.activities.contains(it.activityName) }
            ?.sumOf { it.share?.relativeShareInPercent ?: BigDecimal.ZERO } ?: BigDecimal.ZERO
    } else {
        activities
            ?.filter { rule.activities.contains(it.activityName) }
            ?.sumOf { it.relativeEligibleShareInPercent ?: BigDecimal.ZERO } ?: BigDecimal.ZERO
    }

/**
 * Extracts the EU taxonomy activity list(s) referenced by [inputs] and computes the share described by [rule].
 * A single input is read as an eligible-or-aligned activity list of the EU taxonomy 2026/73 framework, two inputs
 * are read as the non-aligned and aligned activity lists of the EU taxonomy 2020/852 framework.
 *
 * @param inputs the source data points holding the activity lists
 * @param specs the data point type specifications used to resolve each input's role
 * @param rule the rule describing the activity group and whether the aligned or the eligible share is requested
 * @return the source data points the share was derived from, and the computed share
 * @throws IllegalArgumentException if neither one nor two inputs are provided
 */
internal fun extractEuTaxonomyShare(
    inputs: Collection<UploadedDataPoint>,
    specs: Map<DataPointType, DataPointTypeSpecification>,
    rule: EuTaxonomyShareRule,
): Pair<List<ExtendedDataPointInterface<out Iterable<*>?>?>, BigDecimal> =
    when (inputs.size) {
        1 -> {
            val eligibleOrAlignedInput =
                extractEuTaxonomy2026ActivityInput<
                    ExtendedDataPoint<Iterable<EuTaxonomyEligibleOrAlignedActivity>?>?,
                >(inputs, specs)
            listOf(eligibleOrAlignedInput) to calculateEuTaxonomy2026Share(rule, eligibleOrAlignedInput?.value)
        }

        2 -> {
            val activityListsOperands =
                extractEuTaxonomy2020ActivityLists<
                    ExtendedDataPoint<Iterable<EuTaxonomyActivity>?>?,
                    ExtendedDataPoint<Iterable<EuTaxonomyAlignedActivity>?>?,
                >(inputs, specs)
            listOf(activityListsOperands.alignedActivities, activityListsOperands.nonAlignedActivities) to
                calculateEuTaxonomy2020Share(
                    rule,
                    activityListsOperands.nonAlignedActivitiesValue,
                    activityListsOperands.alignedActivitiesValue,
                )
        }

        else -> throw IllegalArgumentException("The EuTaxonomyShare conversion only supports one or two inputs.")
    }

internal data class EuTaxonomy2020ActivityOperands<
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
     * @return a [MergedListWithCommentData] of the merged activities and the names of the
     *   activities for which (a) conflicting substantial contributions were detected and removed
     *   (b) the relative aligned share was null.
     */
    fun mergeLists(): MergedListWithCommentData {
        val nonAlignedActivitiesMap = groupActivitiesByIdentifier(nonAlignedActivitiesValue) { activityIdentifier(it) }
        val alignedActivitiesMap = groupActivitiesByIdentifier(alignedActivitiesValue) { activityIdentifier(it) }
        val identifiers = nonAlignedActivitiesMap?.keys.orEmpty() + alignedActivitiesMap?.keys.orEmpty()

        val eligibleOrAlignedActivities: MutableList<EuTaxonomyEligibleOrAlignedActivity> = mutableListOf()
        val activitiesWithConflictingSubstantialContributions: MutableList<Activity> = mutableListOf()
        val activitiesWithoutAlignedShares: MutableList<Activity> = mutableListOf()
        for (identifier in identifiers) {
            val (adjustedActivity, hadConflict) =
                buildMergedActivity(
                    identifier = identifier,
                    alignedActivities = alignedActivitiesMap?.get(identifier),
                    nonAlignedActivities = nonAlignedActivitiesMap?.get(identifier),
                ) ?: continue
            if (hadConflict) {
                activitiesWithConflictingSubstantialContributions.add(adjustedActivity.activityName)
            }
            if (adjustedActivity.share?.relativeShareInPercent == null) {
                activitiesWithoutAlignedShares.add(adjustedActivity.activityName)
            }
            eligibleOrAlignedActivities.add(adjustedActivity)
        }
        return MergedListWithCommentData(
            eligibleOrAlignedActivities,
            activitiesWithConflictingSubstantialContributions,
            activitiesWithoutAlignedShares,
        )
    }
}

/**
 * Extracts the non-aligned and aligned activity lists from [inputs] for the EU taxonomy activity merge and share
 * conversions.
 *
 * The two inputs are distinguished by their [UploadedDataPoint.dataPointType]'s data point base type, resolved via
 * [specs], rather than by their position in [inputs] or by their JSON content. Content-based discrimination (e.g.
 * trying to deserialize into the narrower non-aligned type and checking for failure) is not reliable, since a JSON
 * serializer producing the uploaded data may omit null fields entirely, making an aligned activity with all-null
 * aligned-only fields structurally indistinguishable from a non-aligned activity.
 *
 * @param inputs the two source data points to be merged
 * @param specs the data point type specifications used to resolve each input's role
 * @return an [EuTaxonomy2020ActivityOperands] containing the extracted non-aligned and aligned activity operands
 */
internal inline fun <
    reified N : ExtendedDataPointInterface<Iterable<EuTaxonomyActivity>?>?,
    reified A : ExtendedDataPointInterface<Iterable<EuTaxonomyAlignedActivity>?>?,
> extractEuTaxonomy2020ActivityLists(
    inputs: Collection<UploadedDataPoint>,
    specs: Map<DataPointType, DataPointTypeSpecification>,
): EuTaxonomy2020ActivityOperands<N, A> {
    require(inputs.size == 2) { "Exactly two data points must be provided to extract." }

    val nonAlignedInput =
        inputs.singleOrNull {
            getDataPointBaseTypeId(it.dataPointType, specs) == EuTaxonomyRulesConfig.NON_ALIGNED_ACTIVITIES_BASE_TYPE
        }
    if (nonAlignedInput == null) {
        throw IllegalArgumentException(
            "Exactly one input of base type ${EuTaxonomyRulesConfig.NON_ALIGNED_ACTIVITIES_BASE_TYPE} " +
                "must be provided to extract.",
        )
    }
    val alignedInput =
        inputs.singleOrNull {
            getDataPointBaseTypeId(it.dataPointType, specs) == EuTaxonomyRulesConfig.ALIGNED_ACTIVITIES_BASE_TYPE
        }
    if (alignedInput == null) {
        throw IllegalArgumentException(
            "Exactly one input of base type ${EuTaxonomyRulesConfig.ALIGNED_ACTIVITIES_BASE_TYPE} must be provided to extract.",
        )
    }

    val nonAlignedActivities = defaultObjectMapper.readValue<N>(nonAlignedInput.dataPoint)
    val alignedActivities = defaultObjectMapper.readValue<A>(alignedInput.dataPoint)
    return EuTaxonomy2020ActivityOperands(
        nonAlignedActivities = nonAlignedActivities,
        alignedActivities = alignedActivities,
        nonAlignedActivitiesValue = nonAlignedActivities?.value,
        alignedActivitiesValue = alignedActivities?.value,
    )
}

/**
 * Extracts the eligible-or-aligned activity list input of the EU taxonomy 2026/73 framework from [inputs].
 *
 * @param inputs the single source data point to be extracted
 * @param specs the data point type specifications used to resolve the input's role
 * @return the extracted eligible-or-aligned activities data point, or `null` if it was not reported
 */
internal inline fun <
    reified E : ExtendedDataPointInterface<Iterable<EuTaxonomyEligibleOrAlignedActivity>?>?,
> extractEuTaxonomy2026ActivityInput(
    inputs: Collection<UploadedDataPoint>,
    specs: Map<DataPointType, DataPointTypeSpecification>,
): E? {
    require(inputs.size == 1) { "Exactly one data point must be provided to extract." }

    val eligibleOrAlignedInput =
        inputs.singleOrNull {
            getDataPointBaseTypeId(it.dataPointType, specs) ==
                EuTaxonomyRulesConfig.ELIGIBLE_OR_ALIGNED_ACTIVITIES_BASE_TYPE
        }
    if (eligibleOrAlignedInput == null) {
        throw IllegalArgumentException(
            "Exactly one input of base type ${EuTaxonomyRulesConfig.ELIGIBLE_OR_ALIGNED_ACTIVITIES_BASE_TYPE} " +
                "must be provided to extract.",
        )
    }

    return defaultObjectMapper.readValue<E>(eligibleOrAlignedInput.dataPoint)
}
