package org.dataland.datalandbackend.services.dataPoints

import org.dataland.datalandbackend.services.datapoints.applyTransformation
import org.dataland.datalandbackend.services.datapoints.resolveEuTaxonomyShareRule
import org.dataland.datalandbackend.utils.ACTIVITY_MERGE_RESULT_TYPE
import org.dataland.datalandbackend.utils.NUCLEAR_ELIGIBLE_TARGET_TYPE
import org.dataland.datalandbackend.utils.activityMergeSpecs
import org.dataland.datalandbackend.utils.alignedActivity
import org.dataland.datalandbackend.utils.createAlignedInput
import org.dataland.datalandbackend.utils.createNonAlignedInput
import org.dataland.datalandbackend.utils.createUploadedDataPoint
import org.dataland.datalandbackend.utils.nonAlignedActivity
import org.dataland.datalandbackend.utils.sourceFrameworksByType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EuTaxonomyShareCalculationValidationTest {
    @Test
    fun `check that EuTaxonomyShare rejects an empty input`() {
        // Case 1: There are zero inputs
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                emptyList(),
                NUCLEAR_ELIGIBLE_TARGET_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that EuTaxonomyShare rejects three inputs`() {
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(
                    createNonAlignedInput(listOf(nonAlignedActivity())),
                    createAlignedInput(listOf(alignedActivity())),
                    createAlignedInput(listOf(alignedActivity())),
                ),
                NUCLEAR_ELIGIBLE_TARGET_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
            )
        }
    }

    /**
     * The following test checks if there appears an exception if duplicated or a wrong types of activities are given as input
     */
    @Test
    fun `check that EuTaxonomyShare rejects duplicated inputs of non aligned activities `() {
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(
                    createNonAlignedInput(listOf(nonAlignedActivity())),
                    createNonAlignedInput(listOf(nonAlignedActivity())),
                ),
                NUCLEAR_ELIGIBLE_TARGET_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that EuTaxonomyShare rejects two aligned inputs without a non-aligned input`() {
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(
                    createAlignedInput(listOf(alignedActivity())),
                    createAlignedInput(listOf(alignedActivity())),
                ),
                NUCLEAR_ELIGIBLE_TARGET_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that EuTaxonomyShare rejects a wrong input of activities `() {
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(
                    createUploadedDataPoint("{}"),
                    createAlignedInput(listOf(alignedActivity())),
                ),
                NUCLEAR_ELIGIBLE_TARGET_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that resolveEuTaxonomyShareRule rejects an unknown target type`() {
        assertThrows<IllegalArgumentException> {
            resolveEuTaxonomyShareRule(ACTIVITY_MERGE_RESULT_TYPE)
        }
    }
}
