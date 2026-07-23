package org.dataland.datalandbackend.services.dataPoints

import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyAlignedActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.RelativeAndAbsoluteFinancialShare
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials202673.custom.EuTaxonomyEligibleOrAlignedActivity
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackend.model.generics.AmountWithCurrency
import org.dataland.datalandbackend.services.datapoints.EuTaxonomyActivityOperands
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
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class DataPointConversionTestEuTaxo {
    private val nonAlignedRef = IdWithRef(id = "extendedEuTaxonomyNonAlignedActivitiesComponent", ref = "dummy")
    private val alignedRef = IdWithRef(id = "extendedEuTaxonomyAlignedActivitiesComponent", ref = "dummy")
    private val nonAlignedTargetType = "nonAlignedType"
    private val alignedTargetType = "alignedType"
    private val activityMergeResultType = "resultType"
    private val naceCodeFixture = "F.41.20"

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

    private fun createNonAlignedInput(
        activities: List<EuTaxonomyActivity>?,
        quality: QualityOptions? = QualityOptions.Reported,
    ) = createUploadedDataPoint(
        defaultObjectMapper.writeValueAsString(
            ExtendedDataPoint(value = activities, quality = quality),
        ),
    ).copy(dataPointType = nonAlignedTargetType)

    private fun createAlignedInput(
        activities: List<EuTaxonomyAlignedActivity>?,
        quality: QualityOptions? = QualityOptions.Reported,
    ) = createUploadedDataPoint(
        defaultObjectMapper.writeValueAsString(
            ExtendedDataPoint(value = activities, quality = quality),
        ),
    ).copy(dataPointType = alignedTargetType)

    private fun nonAlignedActivity(
        activityName: Activity = Activity.AcquisitionAndOwnershipOfBuildings,
        naceCodes: List<String>? = listOf(naceCodeFixture),
        relativeShareInPercent: BigDecimal? = null,
        absoluteShareAmount: BigDecimal? = null,
    ) = EuTaxonomyActivity(
        activityName = activityName,
        naceCodes = naceCodes,
        share =
            RelativeAndAbsoluteFinancialShare(
                absoluteShare = AmountWithCurrency(amount = absoluteShareAmount, currency = "EUR"),
                relativeShareInPercent = relativeShareInPercent,
            ),
    )

    private fun alignedActivity(
        activityName: Activity = Activity.AcquisitionAndOwnershipOfBuildings,
        naceCodes: List<String>? = listOf(naceCodeFixture),
        relativeShareInPercent: BigDecimal? = null,
        absoluteShareAmount: BigDecimal? = null,
        substantialContributionToClimateChangeMitigationInPercent: BigDecimal? = null,
        enablingActivity: YesNo? = null,
        transitionalActivity: YesNo? = null,
    ) = EuTaxonomyAlignedActivity(
        activityName = activityName,
        naceCodes = naceCodes,
        share =
            RelativeAndAbsoluteFinancialShare(
                absoluteShare = AmountWithCurrency(amount = absoluteShareAmount, currency = "EUR"),
                relativeShareInPercent = relativeShareInPercent,
            ),
        substantialContributionToClimateChangeMitigationInPercent = substantialContributionToClimateChangeMitigationInPercent,
        substantialContributionToClimateChangeAdaptationInPercent = null,
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

    private fun mergeActivities(
        nonAligned: List<EuTaxonomyActivity>?,
        aligned: List<EuTaxonomyAlignedActivity>?,
    ): List<EuTaxonomyEligibleOrAlignedActivity>? {
        val operands =
            EuTaxonomyActivityOperands<
                ExtendedDataPoint<Iterable<EuTaxonomyActivity>?>?,
                ExtendedDataPoint<Iterable<EuTaxonomyAlignedActivity>?>?,
            >(
                nonAlignedActivities = null,
                alignedActivities = null,
                nonAlignedActivitiesValue = nonAligned,
                alignedActivitiesValue = aligned,
            )
        return operands
            .mergeLists()
            ?.sortedBy { it.activityName.name + it.naceCodes.orEmpty().joinToString() }
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
            mergeActivities(
                nonAligned =
                    listOf(
                        nonAlignedActivity(
                            relativeShareInPercent = BigDecimal("30"),
                            absoluteShareAmount = BigDecimal("100"),
                        ),
                    ),
                aligned = null,
            )

        assertNotNull(result)
        assertEquals(1, result.size)
        val activity = result.single()
        assertBigDecimalEquals("30", activity.relativeEligibleShareInPercent)
        assertEquals(null, activity.share?.relativeShareInPercent)
        assertEquals(null, activity.share?.absoluteShare?.amount)
        assertEquals(null, activity.substantialContributionToClimateChangeMitigationInPercent)
        assertEquals(null, activity.enablingActivity)
        assertEquals(null, activity.transitionalActivity)
    }

    @Test
    fun `check that an activity only in the aligned list is fully derived from the aligned data`() {
        val result =
            mergeActivities(
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
            mergeActivities(
                nonAligned = listOf(nonAlignedActivity(relativeShareInPercent = BigDecimal("20"))),
                aligned =
                    listOf(
                        alignedActivity(
                            relativeShareInPercent = BigDecimal("40"),
                            absoluteShareAmount = BigDecimal("200"),
                        ),
                    ),
            )

        assertNotNull(result)
        assertEquals(1, result.size)
        val activity = result.single()
        assertBigDecimalEquals("60", activity.relativeEligibleShareInPercent)
        assertBigDecimalEquals("40", activity.share?.relativeShareInPercent)
        assertBigDecimalEquals("200", activity.share?.absoluteShare?.amount)
    }

    @Test
    fun `check that duplicate activities within the same list are merged before cross-matching`() {
        val result =
            mergeActivities(
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
            )

        assertNotNull(result)
        assertEquals(1, result.size)
        val activity = result.single()
        assertBigDecimalEquals("25", activity.share?.relativeShareInPercent)
        assertBigDecimalEquals("30", activity.relativeEligibleShareInPercent)
        assertEquals(YesNo.Yes, activity.enablingActivity)
        assertEquals(YesNo.Yes, activity.transitionalActivity)
        assertBigDecimalEquals("30", activity.substantialContributionToClimateChangeMitigationInPercent)
    }

    @Test
    fun `check null-handling when one activity list is entirely absent`() {
        val result =
            mergeActivities(
                nonAligned = null,
                aligned = listOf(alignedActivity(relativeShareInPercent = BigDecimal("40"))),
            )

        assertNotNull(result)
        assertEquals(1, result.size)
    }

    @Test
    fun `check that same activity name with nace codes in different order produces single entry`() {
        val result =
            mergeActivities(
                nonAligned =
                    listOf(
                        nonAlignedActivity(naceCodes = listOf(naceCodeFixture, "F.42.11"), relativeShareInPercent = BigDecimal("10")),
                    ),
                aligned =
                    listOf(
                        alignedActivity(naceCodes = listOf("F.42.11", naceCodeFixture), relativeShareInPercent = BigDecimal("20")),
                    ),
            )

        assertNotNull(result)
        assertEquals(1, result.size)
    }
}
