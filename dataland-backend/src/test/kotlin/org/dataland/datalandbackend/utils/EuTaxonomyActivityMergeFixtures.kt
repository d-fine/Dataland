package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyAlignedActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.RelativeAndAbsoluteFinancialShare
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials202673.custom.EuTaxonomyEligibleOrAlignedActivity
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackend.model.generics.AmountWithCurrency
import org.dataland.datalandbackend.services.datapoints.applyTransformation
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import java.math.BigDecimal

private val nonAlignedRef = IdWithRef(id = "extendedEuTaxonomyNonAlignedActivitiesComponent", ref = "dummy")
private val alignedRef = IdWithRef(id = "extendedEuTaxonomyAlignedActivitiesComponent", ref = "dummy")

const val NON_ALIGNED_TARGET_TYPE = "nonAlignedType"
const val ALIGNED_TARGET_TYPE = "alignedType"
const val ACTIVITY_MERGE_RESULT_TYPE = "resultType"
const val NACE_CODE_FIXTURE = "F.41.20"

// These synthetic specs let extractEuTaxonomyActivityLists resolve which input is the non-aligned and
// which is the aligned activity list purely by dataPointBaseType id, mirroring how the merge logic
// distinguishes inputs by type rather than by position or content.
val activityMergeSpecs =
    dummySpecs +
        (
            NON_ALIGNED_TARGET_TYPE to
                DataPointTypeSpecification(
                    dataPointType = IdWithRef(id = NON_ALIGNED_TARGET_TYPE, ref = "dummy"),
                    name = "Non-Aligned Activities",
                    businessDefinition = "dummy",
                    dataPointBaseType = nonAlignedRef,
                    usedBy = emptyList(),
                    calculationRules = emptyList(),
                )
        ) +
        (
            ALIGNED_TARGET_TYPE to
                DataPointTypeSpecification(
                    dataPointType = IdWithRef(id = ALIGNED_TARGET_TYPE, ref = "dummy"),
                    name = "Aligned Activities",
                    businessDefinition = "dummy",
                    dataPointBaseType = alignedRef,
                    usedBy = emptyList(),
                    calculationRules = emptyList(),
                )
        )

/**
 * Wraps [activities] into an [org.dataland.datalandbackend.model.datapoints.UploadedDataPoint] tagged with the
 * non-aligned target type, since [org.dataland.datalandbackend.services.datapoints.extractEuTaxonomyActivityLists]
 * relies on the data point type to identify which input is the non-aligned activity list.
 */
fun createNonAlignedInput(
    activities: List<EuTaxonomyActivity>?,
    quality: QualityOptions? = QualityOptions.Reported,
) = createUploadedDataPoint(
    defaultObjectMapper.writeValueAsString(
        ExtendedDataPoint(value = activities, quality = quality),
    ),
).copy(dataPointType = NON_ALIGNED_TARGET_TYPE)

/**
 * Wraps [activities] into an [org.dataland.datalandbackend.model.datapoints.UploadedDataPoint] tagged with the
 * aligned target type, analogous to [createNonAlignedInput] but for the aligned activity list.
 */
fun createAlignedInput(
    activities: List<EuTaxonomyAlignedActivity>?,
    quality: QualityOptions? = QualityOptions.Reported,
) = createUploadedDataPoint(
    defaultObjectMapper.writeValueAsString(
        ExtendedDataPoint(value = activities, quality = quality),
    ),
).copy(dataPointType = ALIGNED_TARGET_TYPE)

/**
 * Fixture builder for a non-aligned activity. Provides sensible defaults so individual tests only need
 * to override the fields relevant to what they check.
 */
fun nonAlignedActivity(
    naceCodes: List<String>? = listOf(NACE_CODE_FIXTURE),
    relativeShareInPercent: BigDecimal? = null,
    absoluteShareAmount: BigDecimal? = null,
    currency: String? = "EUR",
    activityName: Activity = Activity.AcquisitionAndOwnershipOfBuildings,
) = EuTaxonomyActivity(
    activityName = activityName,
    naceCodes = naceCodes,
    share =
        RelativeAndAbsoluteFinancialShare(
            absoluteShare = AmountWithCurrency(amount = absoluteShareAmount, currency = currency),
            relativeShareInPercent = relativeShareInPercent,
        ),
)

/**
 * Fixture builder for an aligned activity, analogous to [nonAlignedActivity] but for the aligned side.
 *
 * This intentionally has more than the usually recommended number of parameters, since it is a pure test fixture
 * builder with defaults for every parameter, letting individual tests only specify the few fields relevant to what
 * they check.
 */
@Suppress("LongParameterList", "kotlin:S107")
fun alignedActivity(
    naceCodes: List<String>? = listOf(NACE_CODE_FIXTURE),
    relativeShareInPercent: BigDecimal? = null,
    absoluteShareAmount: BigDecimal? = null,
    substantialContributionToClimateChangeMitigationInPercent: BigDecimal? = null,
    substantialContributionToClimateChangeAdaptationInPercent: BigDecimal? = null,
    enablingActivity: YesNo? = null,
    transitionalActivity: YesNo? = null,
    currency: String? = "EUR",
    activityName: Activity = Activity.AcquisitionAndOwnershipOfBuildings,
) = EuTaxonomyAlignedActivity(
    activityName = activityName,
    naceCodes = naceCodes,
    share =
        RelativeAndAbsoluteFinancialShare(
            absoluteShare = AmountWithCurrency(amount = absoluteShareAmount, currency),
            relativeShareInPercent = relativeShareInPercent,
        ),
    substantialContributionToClimateChangeMitigationInPercent = substantialContributionToClimateChangeMitigationInPercent,
    substantialContributionToClimateChangeAdaptationInPercent = substantialContributionToClimateChangeAdaptationInPercent,
    substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent = null,
    substantialContributionToTransitionToACircularEconomyInPercent = null,
    substantialContributionToPollutionPreventionAndControlInPercent = null,
    substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent = null,
    dnshToClimateChangeMitigation = null,
    dnshToClimateChangeAdaptation = null,
    dnshToSustainableUseAndProtectionOfWaterAndMarineResources = null,
    dnshToTransitionToACircularEconomy = null,
    dnshToPollutionPreventionAndControl = null,
    dnshToProtectionAndRestorationOfBiodiversityAndEcosystems = null,
    minimumSafeguards = null,
    enablingActivity = enablingActivity,
    transitionalActivity = transitionalActivity,
)

/**
 * Runs the EU taxonomy activity merge transformation and returns the full resulting [ExtendedDataPoint], i.e.
 * including the `comment`. This is needed whenever a test has to inspect the generated comment, e.g. to verify
 * the note added about substantial contributions.
 *
 * Furthermore, the activity list is sorted deterministically (by activity name and NACE codes) because
 * `mergeLists()` iterates over a `Set` of identifiers whose iteration order is not a meaningful, assertable
 * property of the merge itself.
 */
fun mergeActivitiesExtendedDataPoint(
    nonAligned: List<EuTaxonomyActivity>?,
    aligned: List<EuTaxonomyAlignedActivity>?,
): ExtendedDataPoint<List<EuTaxonomyEligibleOrAlignedActivity>?> {
    val result =
        applyTransformation(
            listOf(createNonAlignedInput(nonAligned), createAlignedInput(aligned)),
            ACTIVITY_MERGE_RESULT_TYPE,
            "EuTaxonomyActivityMerge",
            activityMergeSpecs,
            sourceFrameworksByType,
        )

    val extendedDataPoint =
        defaultObjectMapper.readValue<ExtendedDataPoint<List<EuTaxonomyEligibleOrAlignedActivity>?>>(
            result.dataPoint,
        )

    return extendedDataPoint.copy(
        value = extendedDataPoint.value?.sortedBy { it.activityName.name + it.naceCodes.orEmpty().joinToString() },
    )
}
