package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyAlignedActivity
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.datapoints.extended.ExtendedDecimalDataPoint
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification

/**
 * Merges the EU taxonomy activity lists in [inputs] into a single derived data point of [targetType].
 *
 * @param inputs the source data points holding the activity lists
 * @param targetType the data point type assigned to the resulting data point
 * @param specs the data point type specifications used to resolve each input's role
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @return the derived data point holding the merged activities
 */
internal fun DataPointConversion.convertEuTaxonomyActivityMerge(
    inputs: Collection<UploadedDataPoint>,
    targetType: DataPointType,
    specs: Map<DataPointType, DataPointTypeSpecification>,
    sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
): UploadedDataPoint {
    val operands =
        extractEuTaxonomy2020ActivityLists<
            ExtendedDataPoint<Iterable<EuTaxonomyActivity>?>?,
            ExtendedDataPoint<Iterable<EuTaxonomyAlignedActivity>?>?,
        >(inputs, specs)
    val sources = listOf(operands.alignedActivities, operands.nonAlignedActivities)
    val (mergedActivities, activitiesWithConflictingSubstantialContributions, activitiesWithoutAlignedShares) =
        operands
            .mergeLists()
    val baseComment =
        createComment(
            inputs,
            specs,
            sources.filterNotNull(),
            sourceFrameworksByType,
        )
    val comment =
        extendCreateCommentEuTaxonomyActivitiesMerge(
            baseComment,
            activitiesWithConflictingSubstantialContributions,
            activitiesWithoutAlignedShares,
        )
    val calculatedDataPoint =
        ExtendedDataPoint(
            value = mergedActivities.takeIf { it.isNotEmpty() },
            quality = mergeQuality(sources.map { it?.quality }),
            comment = comment,
            dataSource = mergeDataSources(sources.mapNotNull { it?.let(::getDataSource) }),
        )
    return createUploadedDataPoint(
        inputs = inputs,
        targetType = targetType,
        calculatedDataPoint = calculatedDataPoint,
    )
}

/**
 * Derives the share of the eligible or aligned activities of a single activity group from the reported activity lists.
 * A single input is read as an eligible-or-aligned activity list of the EU taxonomy 2026/73 framework, two inputs
 * are read as the non-aligned and aligned activity lists of the EU taxonomy 2020/852 framework.
 *
 * @param inputs the source data points holding the activity lists
 * @param targetType the data point type assigned to the resulting data point
 * @param specs the data point type specifications used to resolve each input's role
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @return the derived share as an [ExtendedDecimalDataPoint]
 */
internal fun DataPointConversion.convertEuTaxonomyShare(
    inputs: Collection<UploadedDataPoint>,
    targetType: DataPointType,
    specs: Map<DataPointType, DataPointTypeSpecification>,
    sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
): UploadedDataPoint {
    val (sources, share) = extractEuTaxonomyShare(inputs, specs, resolveEuTaxonomyShareRule(targetType))
    val calculatedDataPoint =
        ExtendedDecimalDataPoint(
            value = share,
            quality = mergeQuality(sources.map { it?.quality }),
            comment = createComment(inputs, specs, sources.filterNotNull(), sourceFrameworksByType),
            dataSource = mergeDataSources(sources.mapNotNull { it?.let(::getDataSource) }),
        )
    return createUploadedDataPoint(
        inputs = inputs,
        targetType = targetType,
        calculatedDataPoint = calculatedDataPoint,
    )
}
