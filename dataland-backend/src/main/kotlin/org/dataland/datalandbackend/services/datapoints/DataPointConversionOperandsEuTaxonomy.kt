package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyAlignedActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials202673.custom.EuTaxonomyEligibleOrAlignedActivity
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint as ExtendedDataPointInterface

/**
 * Common contract of the operand containers an EU taxonomy share can be derived from.
 */
internal interface EuTaxonomyActivitiesOperands {
    /**
     * The source data points the share is derived from, in the order they are reported in generated comments.
     */
    val sources: List<ExtendedDataPointInterface<out Iterable<*>?>?>
}

internal data class MergedListWithCommentData(
    val mergedList: List<EuTaxonomyEligibleOrAlignedActivity>,
    val conflictedSubstantialContributions: List<Activity>,
    val nullShares: List<Activity>,
)

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
    require(inputs.size == 2) { "Exactly two data points must be provided for the merged inputs." }

    val nonAlignedInput =
        inputs.singleOrNull {
            getDataPointBaseTypeId(it.dataPointType, specs) == EuTaxonomyRulesConfig.NON_ALIGNED_ACTIVITIES_BASE_TYPE
        }
            ?: throw IllegalArgumentException(
                "Exactly one input of base type ${EuTaxonomyRulesConfig.NON_ALIGNED_ACTIVITIES_BASE_TYPE} " +
                    "must be provided for the merge.",
            )
    val alignedInput =
        inputs.singleOrNull {
            getDataPointBaseTypeId(it.dataPointType, specs) == EuTaxonomyRulesConfig.ALIGNED_ACTIVITIES_BASE_TYPE
        }
            ?: throw IllegalArgumentException(
                "Exactly one input of base type ${EuTaxonomyRulesConfig.ALIGNED_ACTIVITIES_BASE_TYPE} " +
                    "must be provided for the merge.",
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
