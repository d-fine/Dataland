package org.dataland.datalandbackend.services.dataPoints

import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackend.utils.alignedActivity
import org.dataland.datalandbackend.utils.assertBigDecimalEquals
import org.dataland.datalandbackend.utils.mergeActivitiesExtendedDataPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import java.math.BigDecimal

class DataPointConversionEuTaxonomySubstantialContributionTest {
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

    @Test
    fun `check that the comment lists the activities without substantial contributions due to a missing relative share`() {
        val extendedDataPoint =
            mergeActivitiesExtendedDataPoint(
                nonAligned = null,
                aligned =
                    listOf(
                        alignedActivity(
                            activityName = Activity.AcquisitionAndOwnershipOfBuildings,
                            relativeShareInPercent = null,
                            absoluteShareAmount = BigDecimal("100"),
                            substantialContributionToClimateChangeMitigationInPercent = BigDecimal("50"),
                            substantialContributionToClimateChangeAdaptationInPercent = BigDecimal("50"),
                        ),
                    ),
            )

        val comment = extendedDataPoint.comment
        assertNotNull(comment)
        assertEquals(false, comment.contains("more than one substantial contribution"))
        assertEquals(true, comment.contains("Activities without relative aligned share cannot have substantial contributions,"))
        assertEquals(true, comment.contains(Activity.AcquisitionAndOwnershipOfBuildings.value))
    }
}
