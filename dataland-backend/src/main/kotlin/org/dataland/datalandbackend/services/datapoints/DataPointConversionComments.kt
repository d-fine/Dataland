package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint as ExtendedDataPointInterface

/**
 * Builds the complete comment for a calculated data point.
 *
 * @param formula the formula to display, using numbered source references such as `[1]`
 * @param inputs the uploaded source data points used for the calculation
 * @param specs the data point type specifications used to resolve source display names
 * @param dataPoints the deserialized source data points used to inspect quality and source comments
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @return the generated calculation comment including formula and source details
 */
internal fun getCalculationComment(
    formula: String,
    inputs: Collection<UploadedDataPoint>,
    specs: Map<DataPointType, DataPointTypeSpecification>,
    dataPoints: Collection<ExtendedDataPointInterface<*>>,
    sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
): String =
    "This data point was calculated using the following formula: $formula\n\n***\n\n" +
        getSourcesSection(inputs, specs, dataPoints, sourceFrameworksByType)

/**
 * Returns numbered source references for the given inputs.
 *
 * @param inputs the uploaded source data points to reference
 * @return references in input order, starting with `[1]`
 */
internal fun getNumberedSourceReferences(inputs: Collection<UploadedDataPoint>): List<String> =
    inputs.mapIndexed { index, _ -> "[${index + 1}]" }

/**
 * Builds the source details section for a calculated or identity-mapped data point.
 *
 * Every source entry contains the source data point type name and framework display name. Source comments are included
 * only when the source data point quality is not [QualityOptions.Reported] or [QualityOptions.Audited].
 *
 * @param inputs the uploaded source data points used to resolve source type names
 * @param specs the data point type specifications keyed by source data point type
 * @param dataPoints the deserialized source data points used to inspect quality and comments
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @return the formatted sources section
 */
internal fun getSourcesSection(
    inputs: Collection<UploadedDataPoint>,
    specs: Map<DataPointType, DataPointTypeSpecification>,
    dataPoints: Collection<ExtendedDataPointInterface<*>>,
    sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
): String =
    inputs
        .zip(dataPoints)
        .mapIndexed { index, (input, dataPoint) ->
            val sourceName = specs.getValue(input.dataPointType).name
            val sourceFrameworkName = getSourceFrameworkLabel(sourceFrameworksByType[input.dataPointType].orEmpty())
            val commentLine =
                if (dataPoint.quality == QualityOptions.Reported || dataPoint.quality == QualityOptions.Audited) {
                    ""
                } else {
                    val sourceComment = dataPoint.comment?.takeIf { it.isNotBlank() } ?: "none"
                    "\n+ Comment: $sourceComment"
                }
            "[${index + 1}] $sourceName\n" +
                "+ Framework: $sourceFrameworkName" +
                commentLine
        }.joinToString(separator = "\n\n")

/**
 * Formats source framework specifications for display in generated calculation comments.
 *
 * @param sourceFrameworks framework specifications associated with a source data point type
 * @return a stable comma-separated list of framework names, or "Unknown" when no framework is available
 */
internal fun getSourceFrameworkLabel(sourceFrameworks: List<FrameworkSpecification>): String =
    sourceFrameworks
        .map { it.name }
        .distinct()
        .sorted()
        .takeIf { it.isNotEmpty() }
        ?.joinToString(", ")
        ?: "Unknown"

/**
 * Extends [baseComment] with notes about activities affected by the EU taxonomy activity merge.
 *
 * @param baseComment the comment generated for the merge conversion before these notes are appended
 * @param activitiesWithConflictingSubstantialContributions activities for which conflicting substantial
 *   contributions were detected and removed
 * @param activitiesWithoutAlignedShares activities for which the relative aligned share was null, so their
 *   substantial contributions were removed
 * @return [baseComment] extended with the applicable notes
 */
internal fun extendCreateCommentEuTaxonomyActivitiesMerge(
    baseComment: String,
    activitiesWithConflictingSubstantialContributions: List<Activity>,
    activitiesWithoutAlignedShares: List<Activity>,
): String {
    var comment = baseComment
    comment =
        if (activitiesWithConflictingSubstantialContributions.isNotEmpty()) {
            comment + "\n\n" +
                "For the following activities, more than one substantial contribution was reported as non-zero " +
                "in the old framework. Since the substantial contributions are expected to sum up to the " +
                "activity's aligned relative share, but the correct scaling could not be determined from the " +
                "old framework, no substantial contributions were mapped for these activities:\n\n" +
                activitiesWithConflictingSubstantialContributions.joinToString("\n") { "- ${it.value}" }
        } else {
            comment
        }

    comment =
        if (activitiesWithoutAlignedShares.isNotEmpty()) {
            comment + "\n\n" +
                "Activities without relative aligned share cannot have substantial contributions," +
                "whose values must add up to the relative share. Thus, the substantial contributions" +
                "were removed for these activities:\n\n" +
                activitiesWithoutAlignedShares.joinToString("\n") { "- ${it.value}" }
        } else {
            comment
        }

    return comment
}
