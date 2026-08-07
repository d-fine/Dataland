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

/**
 * Common contract of the operand containers an EU taxonomy share can be derived from.
 */
internal interface EuTaxonomyActivitiesOperands {
    /**
     * The source data points the share is derived from, in the order they are reported in generated comments.
     */
    val sources: List<ExtendedDataPointInterface<out Iterable<*>?>?>

    /**
     * Computes the eligible share restricted to [activities].
     */
    fun calculateEligibleShare(activities: List<Activity>): BigDecimal

    /**
     * Computes the aligned share restricted to [activities].
     */
    fun calculateAlignedShare(activities: List<Activity>): BigDecimal
}

/**
 * Selects the aligned or the eligible share of these operands according to [rule].
 *
 * @param rule the rule describing the activity group and whether the aligned or the eligible share is requested
 * @return the requested share, or null if it cannot be determined
 */
internal fun EuTaxonomyActivitiesOperands.calculateShare(rule: EuTaxonomyShareRule): BigDecimal? =
    if (rule.isAligned) calculateAlignedShare(rule.activities) else calculateEligibleShare(rule.activities)

/**
 * Extracts the activity operands the EU taxonomy share is derived from.
 *
 * A single input is read as an eligible-or-aligned activity list of the EU taxonomy 2026/73 framework, two inputs are
 * read as the non-aligned and aligned activity lists of the EU taxonomy 2020/852 framework.
 *
 * @param inputs the source data points the share is derived from
 * @param specs the data point type specifications used to resolve each input's role
 * @return the extracted operands
 * @throws IllegalArgumentException if neither one nor two inputs are provided
 */
internal fun extractEuTaxonomyActivitiesOperands(
    inputs: Collection<UploadedDataPoint>,
    specs: Map<DataPointType, DataPointTypeSpecification>,
): EuTaxonomyActivitiesOperands =
    when (inputs.size) {
        1 ->
            extractEuTaxonomy2026ActivityList<
                ExtendedDataPoint<Iterable<EuTaxonomyEligibleOrAlignedActivity>?>?,
            >(inputs, specs)

        2 ->
            extractEuTaxonomy2020ActivityLists<
                ExtendedDataPoint<Iterable<EuTaxonomyActivity>?>?,
                ExtendedDataPoint<Iterable<EuTaxonomyAlignedActivity>?>?,
            >(inputs, specs)

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
) : EuTaxonomyActivitiesOperands {
    override val sources: List<ExtendedDataPointInterface<out Iterable<*>?>?>
        get() = listOf(alignedActivities, nonAlignedActivities)

    /**
     * Merges the non-aligned and aligned activity lists into a single list of eligible-or-aligned activities,
     * combining entries that share the same activity name, NACE codes, and currency.
     *
     * @return a [Pair] of the merged activities (or `null` if neither list contains any entries) and the names of the
     *   activities for which conflicting substantial contributions were detected and removed
     */
    fun mergeLists(): Triple<List<EuTaxonomyEligibleOrAlignedActivity>?, List<Activity>, List<Activity>> {
        val nonAlignedActivitiesMap = groupActivitiesByIdentifier(nonAlignedActivitiesValue) { activityIdentifier(it) }
        val alignedActivitiesMap = groupActivitiesByIdentifier(alignedActivitiesValue) { activityIdentifier(it) }
        val identifiers = nonAlignedActivitiesMap?.keys.orEmpty() + alignedActivitiesMap?.keys.orEmpty()

        val eligibleOrAlignedActivities: MutableList<EuTaxonomyEligibleOrAlignedActivity> = mutableListOf()
        val activitiesWithConflictingSubstantialContributions: MutableList<Activity> = mutableListOf()
        val activitiesWithoutAlignedShares: MutableList<Activity> = mutableListOf()
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
            if (adjustedActivity.share?.relativeShareInPercent == null) {
                activitiesWithoutAlignedShares.add(adjustedActivity.activityName)
            }
            eligibleOrAlignedActivities.add(adjustedActivity)
        }
        return Triple(
            eligibleOrAlignedActivities.takeIf {
                it.isNotEmpty()
            },
            activitiesWithConflictingSubstantialContributions, activitiesWithoutAlignedShares,
        )
    }

    override fun calculateEligibleShare(activities: List<Activity>): BigDecimal {
        val nonAlignedRelativeShare =
            nonAlignedActivitiesValue
                ?.filter { activity -> activities.contains(activity.activityName) }
                ?.sumOf { it.share?.relativeShareInPercent ?: BigDecimal.ZERO } ?: BigDecimal.ZERO
        val alignedRelativeShare =
            alignedActivitiesValue
                ?.filter { activity -> activities.contains(activity.activityName) }
                ?.sumOf { it.share?.relativeShareInPercent ?: BigDecimal.ZERO } ?: BigDecimal.ZERO
        return nonAlignedRelativeShare + alignedRelativeShare
    }

    override fun calculateAlignedShare(activities: List<Activity>): BigDecimal =
        alignedActivitiesValue
            ?.filter { activity -> activities.contains(activity.activityName) }
            ?.sumOf { it.share?.relativeShareInPercent ?: BigDecimal.ZERO } ?: BigDecimal.ZERO
}

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
        inputs.singleOrNull { getDataPointBaseTypeId(it.dataPointType, specs) == EuTaxonomyRulesConfig.NON_ALIGNED_ACTIVITIES_BASE_TYPE }
            ?: throw IllegalArgumentException(
                "Exactly one input of base type ${EuTaxonomyRulesConfig.NON_ALIGNED_ACTIVITIES_BASE_TYPE} must be provided to extract.",
            )
    val alignedInput =
        inputs.singleOrNull { getDataPointBaseTypeId(it.dataPointType, specs) == EuTaxonomyRulesConfig.ALIGNED_ACTIVITIES_BASE_TYPE }
            ?: throw IllegalArgumentException(
                "Exactly one input of base type ${EuTaxonomyRulesConfig.ALIGNED_ACTIVITIES_BASE_TYPE} must be provided to extract.",
            )

    val nonAlignedActivities = defaultObjectMapper.readValue<N>(nonAlignedInput.dataPoint)
    val alignedActivities = defaultObjectMapper.readValue<A>(alignedInput.dataPoint)
    return EuTaxonomy2020ActivityOperands(
        nonAlignedActivities = nonAlignedActivities,
        alignedActivities = alignedActivities,
        nonAlignedActivitiesValue = nonAlignedActivities?.value,
        alignedActivitiesValue = alignedActivities?.value,
    )
}

internal data class EuTaxonomy2026ActivityOperand<
    E : ExtendedDataPointInterface<Iterable<EuTaxonomyEligibleOrAlignedActivity>?>?,
>(
    val eligibleOrAlignedActivities: E?,
    val eligibleOrAlignedActivitiesValue: Iterable<EuTaxonomyEligibleOrAlignedActivity>?,
) : EuTaxonomyActivitiesOperands {
    override val sources: List<ExtendedDataPointInterface<out Iterable<*>?>?>
        get() = listOf(eligibleOrAlignedActivities)

    override fun calculateEligibleShare(activities: List<Activity>): BigDecimal =
        eligibleOrAlignedActivitiesValue
            ?.filter { activity -> activities.contains(activity.activityName) }
            ?.sumOf { it.relativeEligibleShareInPercent ?: BigDecimal.ZERO } ?: BigDecimal.ZERO

    override fun calculateAlignedShare(activities: List<Activity>): BigDecimal =
        eligibleOrAlignedActivitiesValue
            ?.filter { activity -> activities.contains(activity.activityName) }
            ?.sumOf { it.share?.relativeShareInPercent ?: BigDecimal.ZERO } ?: BigDecimal.ZERO
}

internal inline fun <
    reified E : ExtendedDataPointInterface<Iterable<EuTaxonomyEligibleOrAlignedActivity>?>?,
> extractEuTaxonomy2026ActivityList(
    inputs: Collection<UploadedDataPoint>,
    specs: Map<DataPointType, DataPointTypeSpecification>,
): EuTaxonomy2026ActivityOperand<E> {
    require(inputs.size == 1) { "Exactly one data point must be provided to extract." }

    val eligibleOrAlignedInput =
        inputs.singleOrNull {
            getDataPointBaseTypeId(it.dataPointType, specs) ==
                EuTaxonomyRulesConfig.ELIGIBLE_OR_ALIGNED_ACTIVITIES_BASE_TYPE
        }
            ?: throw IllegalArgumentException(
                "Exactly one input of base type ${EuTaxonomyRulesConfig.ELIGIBLE_OR_ALIGNED_ACTIVITIES_BASE_TYPE}" +
                    "must be provided to extract.",
            )

    val eligibleOrAlignedActivities = defaultObjectMapper.readValue<E>(eligibleOrAlignedInput.dataPoint)
    return EuTaxonomy2026ActivityOperand(
        eligibleOrAlignedActivities = eligibleOrAlignedActivities,
        eligibleOrAlignedActivitiesValue = eligibleOrAlignedActivities?.value,
    )
}
