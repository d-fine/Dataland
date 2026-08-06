package org.dataland.datalandbackend.services.dataPoints

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.services.datapoints.applyTransformation
import org.dataland.datalandbackend.utils.ACTIVITY_MERGE_RESULT_TYPE
import org.dataland.datalandbackend.utils.NACE_CODE_FIXTURE
import org.dataland.datalandbackend.utils.activityMergeSpecs
import org.dataland.datalandbackend.utils.alignedActivity
import org.dataland.datalandbackend.utils.assertBigDecimalEquals
import org.dataland.datalandbackend.utils.createAlignedInput
import org.dataland.datalandbackend.utils.createNonAlignedInput
import org.dataland.datalandbackend.utils.createUploadedDataPoint
import org.dataland.datalandbackend.utils.mergeActivitiesExtendedDataPoint
import org.dataland.datalandbackend.utils.nonAlignedActivity
import org.dataland.datalandbackend.utils.sourceFrameworksByType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class DataPointConversionEuTaxonomyTest {
    @Test
    fun `check that activity merge rejects the wrong number of inputs`() {
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                emptyList(), ACTIVITY_MERGE_RESULT_TYPE, "EuTaxonomyActivityMerge", activityMergeSpecs, sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(createNonAlignedInput(listOf(nonAlignedActivity()))),
                ACTIVITY_MERGE_RESULT_TYPE, "EuTaxonomyActivityMerge", activityMergeSpecs, sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(
                    createNonAlignedInput(listOf(nonAlignedActivity())),
                    createAlignedInput(listOf(alignedActivity())),
                    createAlignedInput(listOf(alignedActivity())),
                ),
                ACTIVITY_MERGE_RESULT_TYPE, "EuTaxonomyActivityMerge", activityMergeSpecs, sourceFrameworksByType,
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
                ACTIVITY_MERGE_RESULT_TYPE, "EuTaxonomyActivityMerge", activityMergeSpecs, sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(
                    createAlignedInput(listOf(alignedActivity())),
                    createAlignedInput(listOf(alignedActivity())),
                ),
                ACTIVITY_MERGE_RESULT_TYPE, "EuTaxonomyActivityMerge", activityMergeSpecs, sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(
                    createUploadedDataPoint("{}"),
                    createAlignedInput(listOf(alignedActivity())),
                ),
                ACTIVITY_MERGE_RESULT_TYPE, "EuTaxonomyActivityMerge", activityMergeSpecs, sourceFrameworksByType,
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
        // Regression check: the conflict note must only be added when substantial contributions actually conflict;
        // here only a single substantial contribution field is set, so no conflict occurs and the base comment
        // should be left untouched.
        assertEquals(
            false,
            extendedDataPoint.comment?.contains("more than one substantial contribution"),
        )
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
                        nonAlignedActivity(naceCodes = listOf(NACE_CODE_FIXTURE, "F.42.11"), relativeShareInPercent = BigDecimal("10")),
                    ),
                aligned =
                    listOf(
                        alignedActivity(naceCodes = listOf("F.42.11", NACE_CODE_FIXTURE), relativeShareInPercent = BigDecimal("20")),
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
}
