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
        naceCodes: List<String>? = listOf(naceCodeFixture),
        relativeShareInPercent: BigDecimal? = null,
        absoluteShareAmount: BigDecimal? = null,
        currency: String? = "EUR"
    ) = EuTaxonomyActivity(
        activityName = Activity.AcquisitionAndOwnershipOfBuildings,
        naceCodes = naceCodes,
        share =
            RelativeAndAbsoluteFinancialShare(
                absoluteShare = AmountWithCurrency(amount = absoluteShareAmount, currency = currency),
                relativeShareInPercent = relativeShareInPercent,
            ),
    )

    @Suppress("LongParameterList")
    private fun alignedActivity(
        naceCodes: List<String>? = listOf(naceCodeFixture),
        relativeShareInPercent: BigDecimal? = null,
        absoluteShareAmount: BigDecimal? = null,
        substantialContributionToClimateChangeMitigationInPercent: BigDecimal? = null,
        enablingActivity: YesNo? = null,
        transitionalActivity: YesNo? = null,
        currency: String? = "EUR",
    ) = EuTaxonomyAlignedActivity(
        activityName = Activity.AcquisitionAndOwnershipOfBuildings,
        naceCodes = naceCodes,
        share =
            RelativeAndAbsoluteFinancialShare(
                absoluteShare = AmountWithCurrency(amount = absoluteShareAmount, currency),
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

        return extendedDataPoint.value
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
        assertEquals(null, activity.share)
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
                            substantialContributionToClimateChangeMitigationInPercent = BigDecimal("100"),
                        ),
                    ),
            )

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
        assertBigDecimalEquals("25", activity.substantialContributionToClimateChangeMitigationInPercent)
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

    @Test
    fun `Check that non-relevant activities are not added`() {
        val result =
            mergeActivities(
                nonAligned =
                    listOf(
                        nonAlignedActivity(absoluteShareAmount = BigDecimal("10"), currency = null),
                    ),
                aligned = listOf(),
            )
        assertNull(result)
    }

    @Test
    fun `Check that activities with different currencies are not merged and activity that is non-relevant is not added`() {
        val result =
            mergeActivities(
                nonAligned =
                    listOf(
                        nonAlignedActivity(absoluteShareAmount = BigDecimal("10"), currency = "EUR"),
                        nonAlignedActivity(absoluteShareAmount = BigDecimal("5"), currency = "USD"),
                    ),
                aligned = listOf(alignedActivity(absoluteShareAmount = BigDecimal("10"), currency = "USD")),
            )
        assertNotNull(result)
        assertEquals(1, result.size)
        val activity = result.single()
        assertNotNull(activity.share)
        assertNotNull(activity.share.absoluteShare)
        assertBigDecimalEquals("10", activity.share.absoluteShare.amount)
    }

    @Test
    fun `Check that activities with different currencies are not merged without deletion of meaningless activity`() {
        val result =
            mergeActivities(
                nonAligned = listOf(nonAlignedActivity(absoluteShareAmount = BigDecimal("10"), currency = "USD")),
                aligned =
                    listOf(
                        alignedActivity(absoluteShareAmount = BigDecimal("10"), currency = "EUR"),
                        alignedActivity(absoluteShareAmount = BigDecimal("5"), currency = "USD"),
                    ),
            )
        assertNotNull(result)
        assertEquals(2, result.size)
    }
}
