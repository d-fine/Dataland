package org.dataland.datalandbackend.services.dataPoints

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
import org.dataland.datalandbackend.utils.assertBigDecimalEquals
import org.dataland.datalandbackend.utils.createUploadedDataPoint
import org.dataland.datalandbackend.utils.dummySpecs
import org.dataland.datalandbackend.utils.sourceFrameworksByType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class DataPointConversionEuTaxonomyTest {
    private val nonAlignedRef = IdWithRef(id = "extendedEuTaxonomyNonAlignedActivitiesComponent", ref = "dummy")
    private val alignedRef = IdWithRef(id = "extendedEuTaxonomyAlignedActivitiesComponent", ref = "dummy")
    private val nonAlignedTargetType = "nonAlignedType"
    private val alignedTargetType = "alignedType"
    private val activityMergeResultType = "resultType"
    private val naceCodeFixture = "F.41.20"

    // These synthetic specs let extractEuTaxonomyActivityLists resolve which input is the non-aligned and
    // which is the aligned activity list purely by dataPointBaseType id, mirroring how the merge logic
    // distinguishes inputs by type rather than by position or content.
    private val activitiesSpecs =
        dummySpecs +
            (
                nonAlignedTargetType to
                    DataPointTypeSpecification(
                        dataPointType = IdWithRef(id = nonAlignedTargetType, ref = "dummy"),
                        name = "Non-Aligned Activities",
                        businessDefinition = "dummy",
                        dataPointBaseType = nonAlignedRef,
                        usedBy = emptyList(),
                        calculationRules = emptyList(),
                    )
            ) +
            (
                alignedTargetType to
                    DataPointTypeSpecification(
                        dataPointType = IdWithRef(id = alignedTargetType, ref = "dummy"),
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
    private fun createNonAlignedInput(
        activities: List<EuTaxonomyActivity>?,
        quality: QualityOptions? = QualityOptions.Reported,
    ) = createUploadedDataPoint(
        defaultObjectMapper.writeValueAsString(
            ExtendedDataPoint(value = activities, quality = quality),
        ),
    ).copy(dataPointType = nonAlignedTargetType)

    /**
     * Wraps [activities] into an [org.dataland.datalandbackend.model.datapoints.UploadedDataPoint] tagged with the
     * aligned target type, analogous to [createNonAlignedInput] but for the aligned activity list.
     */
    private fun createAlignedInput(
        activities: List<EuTaxonomyAlignedActivity>?,
        quality: QualityOptions? = QualityOptions.Reported,
    ) = createUploadedDataPoint(
        defaultObjectMapper.writeValueAsString(
            ExtendedDataPoint(value = activities, quality = quality),
        ),
    ).copy(dataPointType = alignedTargetType)

    /**
     * Fixture builder for a non-aligned activity. Provides sensible defaults so individual tests only need
     * to override the fields relevant to what they check.
     */
    private fun nonAlignedActivity(
        naceCodes: List<String>? = listOf(naceCodeFixture),
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
     */
    @Suppress("LongParameterList")
    private fun alignedActivity(
        naceCodes: List<String>? = listOf(naceCodeFixture),
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
     * Furthermore, the activity list is sorted deterministically (by activity name and * NACE codes)
     * because `mergeLists()` iterates over a `Set` of identifiers whose iteration order is not a
     * meaningful, assertable property of the merge itself.
     */
    private fun mergeActivitiesExtendedDataPoint(
        nonAligned: List<EuTaxonomyActivity>?,
        aligned: List<EuTaxonomyAlignedActivity>?,
    ): ExtendedDataPoint<List<EuTaxonomyEligibleOrAlignedActivity>?> {
        val result =
            applyTransformation(
                listOf(createNonAlignedInput(nonAligned), createAlignedInput(aligned)),
                activityMergeResultType,
                "EuTaxonomyActivityMerge",
                activitiesSpecs,
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

    @Test
    fun `check that activity merge rejects the wrong number of inputs`() {
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                emptyList(), activityMergeResultType, "EuTaxonomyActivityMerge", activitiesSpecs, sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(createNonAlignedInput(listOf(nonAlignedActivity()))),
                activityMergeResultType, "EuTaxonomyActivityMerge", activitiesSpecs, sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(
                    createNonAlignedInput(listOf(nonAlignedActivity())),
                    createAlignedInput(listOf(alignedActivity())),
                    createAlignedInput(listOf(alignedActivity())),
                ),
                activityMergeResultType, "EuTaxonomyActivityMerge", activitiesSpecs, sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that activity merge rejects inputs with duplicated or unknown base types`() {
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(
                    createNonAlignedInput(listOf(nonAlignedActivity())),
                    createNonAlignedInput(listOf(nonAlignedActivity())),
                ),
                activityMergeResultType, "EuTaxonomyActivityMerge", activitiesSpecs, sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(
                    createAlignedInput(listOf(alignedActivity())),
                    createAlignedInput(listOf(alignedActivity())),
                ),
                activityMergeResultType, "EuTaxonomyActivityMerge", activitiesSpecs, sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(
                    createUploadedDataPoint("{}"),
                    createAlignedInput(listOf(alignedActivity())),
                ),
                activityMergeResultType, "EuTaxonomyActivityMerge", activitiesSpecs, sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that an activity only in the non-aligned list is mapped with null aligned-derived fields`() {
        val result =
            mergeActivitiesExtendedDataPoint(
                nonAligned =
                    listOf(
                        nonAlignedActivity(
                            relativeShareInPercent = BigDecimal("30"),
                            absoluteShareAmount = BigDecimal("100"),
                        ),
                    ),
                aligned = null,
            ).value

        assertNotNull(result)
        assertEquals(1, result.size)
        val activity = result.single()
        assertBigDecimalEquals("30", activity.relativeEligibleShareInPercent)
        assertEquals(null, activity.share)
        assertEquals(null, activity.substantialContributionToClimateChangeMitigationInPercent)
        assertEquals(null, activity.enablingActivity)
        assertEquals(null, activity.transitionalActivity)
    }

    @Test
    fun `check that an activity only in the aligned list is fully derived from the aligned data`() {
        val extendedDataPoint =
            mergeActivitiesExtendedDataPoint(
                nonAligned = null,
                aligned =
                    listOf(
                        alignedActivity(
                            relativeShareInPercent = BigDecimal("40"),
                            absoluteShareAmount = BigDecimal("200"),
                            substantialContributionToClimateChangeMitigationInPercent = BigDecimal("10"),
                            enablingActivity = YesNo.Yes,
                            transitionalActivity = YesNo.No,
                        ),
                    ),
            )
        val result = extendedDataPoint.value

        assertNotNull(result)
        assertEquals(1, result.size)
        val activity = result.single()
        assertBigDecimalEquals("40", activity.relativeEligibleShareInPercent)
        assertBigDecimalEquals("40", activity.share?.relativeShareInPercent)
        assertBigDecimalEquals("200", activity.share?.absoluteShare?.amount)
        assertEquals("EUR", activity.share?.absoluteShare?.currency)
        assertBigDecimalEquals("40", activity.substantialContributionToClimateChangeMitigationInPercent)
        assertEquals(null, activity.substantialContributionToClimateChangeAdaptationInPercent)
        assertEquals(YesNo.Yes, activity.enablingActivity)
        assertEquals(YesNo.No, activity.transitionalActivity)
    }

    @Test
    fun `check that the same activity present in both lists is merged into a single entry`() {
        val result =
            mergeActivitiesExtendedDataPoint(
                nonAligned = listOf(nonAlignedActivity(relativeShareInPercent = BigDecimal("20"))),
                aligned =
                    listOf(
                        alignedActivity(
                            relativeShareInPercent = BigDecimal("40"),
                            absoluteShareAmount = BigDecimal("200"),
                            substantialContributionToClimateChangeMitigationInPercent = BigDecimal("100"),
                        ),
                    ),
            ).value

        assertNotNull(result)
        assertEquals(1, result.size)
        val activity = result.single()
        assertBigDecimalEquals("60", activity.relativeEligibleShareInPercent)
        assertBigDecimalEquals("40", activity.share?.relativeShareInPercent)
        assertBigDecimalEquals("200", activity.share?.absoluteShare?.amount)
        assertBigDecimalEquals("40", activity.substantialContributionToClimateChangeMitigationInPercent)
    }

    @Test
    fun `check that duplicate activities within the same list are merged before cross-matching`() {
        val result =
            mergeActivitiesExtendedDataPoint(
                nonAligned = listOf(nonAlignedActivity(relativeShareInPercent = BigDecimal("5"))),
                aligned =
                    listOf(
                        alignedActivity(
                            relativeShareInPercent = BigDecimal("10"),
                            substantialContributionToClimateChangeMitigationInPercent = BigDecimal("5"),
                            enablingActivity = YesNo.Yes,
                        ),
                        alignedActivity(
                            relativeShareInPercent = BigDecimal("15"),
                            substantialContributionToClimateChangeMitigationInPercent = BigDecimal("0"),
                            enablingActivity = YesNo.No,
                            transitionalActivity = YesNo.Yes,
                        ),
                    ),
            ).value

        assertNotNull(result)
        assertEquals(1, result.size)
        val activity = result.single()
        assertBigDecimalEquals("25", activity.share?.relativeShareInPercent)
        assertBigDecimalEquals("30", activity.relativeEligibleShareInPercent)
        assertEquals(YesNo.Yes, activity.enablingActivity)
        assertEquals(YesNo.Yes, activity.transitionalActivity)
        assertBigDecimalEquals("25", activity.substantialContributionToClimateChangeMitigationInPercent)
    }

    @Test
    fun `check that same activity name with nace codes in different order produces single entry`() {
        val result =
            mergeActivitiesExtendedDataPoint(
                nonAligned =
                    listOf(
                        nonAlignedActivity(naceCodes = listOf(naceCodeFixture, "F.42.11"), relativeShareInPercent = BigDecimal("10")),
                    ),
                aligned =
                    listOf(
                        alignedActivity(naceCodes = listOf("F.42.11", naceCodeFixture), relativeShareInPercent = BigDecimal("20")),
                    ),
            ).value

        assertNotNull(result)
        assertEquals(1, result.size)
    }

    @Test
    fun `check that non-relevant activities are not added`() {
        val result =
            mergeActivitiesExtendedDataPoint(
                nonAligned =
                    listOf(
                        nonAlignedActivity(absoluteShareAmount = BigDecimal("10"), currency = null),
                    ),
                aligned = listOf(),
            ).value
        assertNull(result)
    }

    @Test
    fun `check that activities with different currencies are not merged and that the non-relevant activity is not added`() {
        val result =
            mergeActivitiesExtendedDataPoint(
                nonAligned =
                    listOf(
                        nonAlignedActivity(absoluteShareAmount = BigDecimal("10"), currency = "EUR"),
                        nonAlignedActivity(absoluteShareAmount = BigDecimal("5"), currency = "USD"),
                    ),
                aligned = listOf(alignedActivity(absoluteShareAmount = BigDecimal("10"), currency = "USD")),
            ).value
        assertNotNull(result)
        assertEquals(1, result.size)
        val activity = result.single()
        assertNotNull(activity.share)
        assertNotNull(activity.share.absoluteShare)
        assertBigDecimalEquals("10", activity.share.absoluteShare.amount)
    }

    @Test
    fun `check that activities with different currencies are not merged and that a relevant activity is not deleted`() {
        val result =
            mergeActivitiesExtendedDataPoint(
                nonAligned = listOf(nonAlignedActivity(absoluteShareAmount = BigDecimal("10"), currency = "USD")),
                aligned =
                    listOf(
                        alignedActivity(absoluteShareAmount = BigDecimal("10"), currency = "EUR"),
                        alignedActivity(absoluteShareAmount = BigDecimal("5"), currency = "USD"),
                    ),
            ).value
        assertNotNull(result)
        assertEquals(2, result.size)
    }

    @Test
    fun `check that conflicting substantial contributions are removed but an otherwise relevant activity is kept`() {
        val result =
            mergeActivitiesExtendedDataPoint(
                nonAligned = null,
                aligned =
                    listOf(
                        alignedActivity(
                            relativeShareInPercent = BigDecimal("50"),
                            absoluteShareAmount = BigDecimal("100"),
                            substantialContributionToClimateChangeMitigationInPercent = BigDecimal("50"),
                            substantialContributionToClimateChangeAdaptationInPercent = BigDecimal("50"),
                        ),
                    ),
            ).value

        assertNotNull(result)
        assertEquals(1, result.size)
        val activity = result.single()
        assertNotNull(activity.share)
        assertBigDecimalEquals("100", activity.share.absoluteShare?.amount)
        assertEquals(null, activity.substantialContributionToClimateChangeMitigationInPercent)
        assertEquals(null, activity.substantialContributionToClimateChangeAdaptationInPercent)
    }

    @Test
    fun `check that an activity relevant only due to a substantial contribution conflict is fully removed`() {
        val conflictingActivity =
            alignedActivity(
                substantialContributionToClimateChangeMitigationInPercent = BigDecimal("50"),
                substantialContributionToClimateChangeAdaptationInPercent = BigDecimal("50"),
            ).copy(share = null)

        val result =
            mergeActivitiesExtendedDataPoint(
                nonAligned = null,
                aligned = listOf(conflictingActivity),
            ).value

        assertNull(result)
    }

    @Test
    fun `check that the comment lists the activities with conflicting substantial contributions`() {
        val extendedDataPoint =
            mergeActivitiesExtendedDataPoint(
                nonAligned = null,
                aligned =
                    listOf(
                        alignedActivity(
                            activityName = Activity.AcquisitionAndOwnershipOfBuildings,
                            relativeShareInPercent = BigDecimal("50"),
                            absoluteShareAmount = BigDecimal("100"),
                            substantialContributionToClimateChangeMitigationInPercent = BigDecimal("50"),
                            substantialContributionToClimateChangeAdaptationInPercent = BigDecimal("50"),
                        ),
                        alignedActivity(
                            activityName = Activity.Afforestation,
                            relativeShareInPercent = BigDecimal("30"),
                            absoluteShareAmount = BigDecimal("60"),
                            substantialContributionToClimateChangeMitigationInPercent = BigDecimal("30"),
                            substantialContributionToClimateChangeAdaptationInPercent = BigDecimal("30"),
                        ),
                    ),
            )

        val comment = extendedDataPoint.comment
        assertNotNull(comment)
        assertEquals(true, comment.contains("more than one substantial contribution"))
        assertEquals(true, comment.contains(Activity.AcquisitionAndOwnershipOfBuildings.value))
        assertEquals(true, comment.contains(Activity.Afforestation.value))
    }
}
