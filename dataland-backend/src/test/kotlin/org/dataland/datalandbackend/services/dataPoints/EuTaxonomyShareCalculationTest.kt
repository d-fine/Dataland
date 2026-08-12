package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackend.services.datapoints.applyTransformation
import org.dataland.datalandbackend.utils.ACTIVITY_MERGE_RESULT_TYPE
import org.dataland.datalandbackend.utils.FOSSIL_GAS_ALIGNED_TARGET_TYPE
import org.dataland.datalandbackend.utils.FOSSIL_GAS_ELIGIBLE_TARGET_TYPE
import org.dataland.datalandbackend.utils.NUCLEAR_ALIGNED_TARGET_TYPE
import org.dataland.datalandbackend.utils.NUCLEAR_ELIGIBLE_TARGET_TYPE
import org.dataland.datalandbackend.utils.activityMergeSpecs
import org.dataland.datalandbackend.utils.alignedActivity
import org.dataland.datalandbackend.utils.assertBigDecimalEquals
import org.dataland.datalandbackend.utils.createAlignedInput
import org.dataland.datalandbackend.utils.createEligibleOrAlignedInput
import org.dataland.datalandbackend.utils.createNonAlignedInput
import org.dataland.datalandbackend.utils.createUploadedDataPoint
import org.dataland.datalandbackend.utils.eligibleOrAlignedActivity
import org.dataland.datalandbackend.utils.nonAlignedActivity
import org.dataland.datalandbackend.utils.shareSpecs
import org.dataland.datalandbackend.utils.sourceFrameworksByType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class EuTaxonomyShareCalculationTest {
    /**
     * The following test checks if there appears an exception if the wrong number of inputs is entered
     */
    @Test
    fun `check that EuTaxonomyShare rejects an empty input`() {
        // Case 1: There are zero inputs
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                emptyList(),
                ACTIVITY_MERGE_RESULT_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
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
                ACTIVITY_MERGE_RESULT_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
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
                ACTIVITY_MERGE_RESULT_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that EuTaxonomyShare rejects duplicated inputs of aligned activities `() {
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(
                    createAlignedInput(listOf(alignedActivity())),
                    createAlignedInput(listOf(alignedActivity())),
                ),
                ACTIVITY_MERGE_RESULT_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
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
                ACTIVITY_MERGE_RESULT_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
            )
        }
    }

    /**
     * The following test check that the calculation rule is behaving in the correct way, given that there is a
     * correct input
     */
    @Test
    fun `check that a single nuclear activity in the new framework yields the correct eligible share`() {
        val result =
            applyTransformation(
                listOf(
                    createEligibleOrAlignedInput(
                        listOf(
                            eligibleOrAlignedActivity(
                                activityName = Activity.ElectricityGenerationFromNuclearEnergyInExistingInstallations,
                                relativeEligibleShareInPercent = BigDecimal("7"),
                            ),
                        ),
                    ),
                ),
                NUCLEAR_ELIGIBLE_TARGET_TYPE,
                "EuTaxonomyShare",
                shareSpecs,
                sourceFrameworksByType,
            )

        val extendedDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(result.dataPoint)
        assertBigDecimalEquals("7", extendedDataPoint.value)
    }

    @Test
    fun `check that a single nuclear activity in the new framework yields the correct aligned share and not the eligible share`() {
        val result =
            applyTransformation(
                listOf(
                    createEligibleOrAlignedInput(
                        listOf(
                            eligibleOrAlignedActivity(
                                activityName = Activity.ElectricityGenerationFromNuclearEnergyInExistingInstallations,
                                relativeEligibleShareInPercent = BigDecimal("7"),
                                relativeShareInPercent = BigDecimal("3"),
                            ),
                        ),
                    ),
                ),
                NUCLEAR_ALIGNED_TARGET_TYPE,
                "EuTaxonomyShare",
                shareSpecs,
                sourceFrameworksByType,
            )

        val extendedDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(result.dataPoint)
        assertBigDecimalEquals("3", extendedDataPoint.value)
    }

    @Suppress("ktlint:standard:max-line-length", "MaxLineLength", "kotlin:S103")
    @Test
    fun `check that multiple eligible nuclear activities in the new framework are summed`() {
        val result =
            applyTransformation(
                listOf(
                    createEligibleOrAlignedInput(
                        listOf(
                            eligibleOrAlignedActivity(
                                activityName = Activity.ElectricityGenerationFromNuclearEnergyInExistingInstallations,
                                relativeEligibleShareInPercent = BigDecimal("4"),
                            ),
                            eligibleOrAlignedActivity(
                                activityName = Activity.ConstructionAndSafeOperationOfNewNuclearPowerPlantsForTheGenerationOfElectricityAndOrHeatIncludingForHydrogenProductionUsingBestAvailableTechnologies,
                                relativeEligibleShareInPercent = BigDecimal("3"),
                            ),
                        ),
                    ),
                ),
                NUCLEAR_ELIGIBLE_TARGET_TYPE,
                "EuTaxonomyShare",
                shareSpecs,
                sourceFrameworksByType,
            )
        val extendedDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(result.dataPoint)
        assertBigDecimalEquals("7", extendedDataPoint.value)
    }

    @Test
    fun `check that a non-relevant activity in the new framework is ignored`() {
        val result =
            applyTransformation(
                listOf(
                    createEligibleOrAlignedInput(
                        listOf(
                            eligibleOrAlignedActivity(
                                activityName = Activity.ElectricityGenerationFromNuclearEnergyInExistingInstallations,
                                relativeEligibleShareInPercent = BigDecimal("5"),
                            ),
                            eligibleOrAlignedActivity(
                                activityName = Activity.AcquisitionAndOwnershipOfBuildings,
                                relativeEligibleShareInPercent = BigDecimal("50"),
                            ),
                        ),
                    ),
                ),
                NUCLEAR_ELIGIBLE_TARGET_TYPE,
                "EuTaxonomyShare",
                shareSpecs,
                sourceFrameworksByType,
            )
        val extendedDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(result.dataPoint)
        assertBigDecimalEquals("5", extendedDataPoint.value)
    }

    @Test
    fun `check that an empty activity list in the new framework yields zero`() {
        val result =
            applyTransformation(
                listOf(createEligibleOrAlignedInput(emptyList())),
                NUCLEAR_ELIGIBLE_TARGET_TYPE,
                "EuTaxonomyShare",
                shareSpecs,
                sourceFrameworksByType,
            )
        val extendedDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(result.dataPoint)
        assertBigDecimalEquals("0", extendedDataPoint.value)
    }

    @Test
    fun `check that a single fossil gas activity in the new framework yields the correct eligible share`() {
        val result =
            applyTransformation(
                listOf(
                    createEligibleOrAlignedInput(
                        listOf(
                            eligibleOrAlignedActivity(
                                activityName = Activity.ElectricityGenerationFromFossilGaseousFuels,
                                relativeEligibleShareInPercent = BigDecimal("9"),
                            ),
                        ),
                    ),
                ),
                FOSSIL_GAS_ELIGIBLE_TARGET_TYPE,
                "EuTaxonomyShare",
                shareSpecs,
                sourceFrameworksByType,
            )
        val extendedDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(result.dataPoint)
        assertBigDecimalEquals("9", extendedDataPoint.value)
    }

    @Test
    fun `check that a single fossil gas activity in the new framework yields the correct aligned share`() {
        val result =
            applyTransformation(
                listOf(
                    createEligibleOrAlignedInput(
                        listOf(
                            eligibleOrAlignedActivity(
                                activityName = Activity.ElectricityGenerationFromFossilGaseousFuels,
                                relativeEligibleShareInPercent = BigDecimal("9"),
                                relativeShareInPercent = BigDecimal("2"),
                            ),
                        ),
                    ),
                ),
                FOSSIL_GAS_ALIGNED_TARGET_TYPE,
                "EuTaxonomyShare",
                shareSpecs,
                sourceFrameworksByType,
            )
        val extendedDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(result.dataPoint)
        assertBigDecimalEquals("2", extendedDataPoint.value)
    }

    @Test
    fun `check that a fossil gas activity is filtered out when calculating the nuclear share`() {
        val result =
            applyTransformation(
                listOf(
                    createEligibleOrAlignedInput(
                        listOf(
                            eligibleOrAlignedActivity(
                                activityName = Activity.ElectricityGenerationFromNuclearEnergyInExistingInstallations,
                                relativeEligibleShareInPercent = BigDecimal("5"),
                            ),
                            eligibleOrAlignedActivity(
                                activityName = Activity.ElectricityGenerationFromFossilGaseousFuels,
                                relativeEligibleShareInPercent = BigDecimal("8"),
                            ),
                        ),
                    ),
                ),
                NUCLEAR_ELIGIBLE_TARGET_TYPE,
                "EuTaxonomyShare",
                shareSpecs,
                sourceFrameworksByType,
            )

        val extendedDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(result.dataPoint)
        assertBigDecimalEquals("5", extendedDataPoint.value)
    }

    @Test
    fun `check that a nuclear activity is filtered out when calculating the fossil gas share`() {
        val result =
            applyTransformation(
                listOf(
                    createEligibleOrAlignedInput(
                        listOf(
                            eligibleOrAlignedActivity(
                                activityName = Activity.ElectricityGenerationFromNuclearEnergyInExistingInstallations,
                                relativeEligibleShareInPercent = BigDecimal("5"),
                            ),
                            eligibleOrAlignedActivity(
                                activityName = Activity.ElectricityGenerationFromFossilGaseousFuels,
                                relativeEligibleShareInPercent = BigDecimal("8"),
                            ),
                        ),
                    ),
                ),
                FOSSIL_GAS_ELIGIBLE_TARGET_TYPE,
                "EuTaxonomyShare",
                shareSpecs,
                sourceFrameworksByType,
            )

        val extendedDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(result.dataPoint)
        assertBigDecimalEquals("8", extendedDataPoint.value)
    }

    @Test
    fun `check that the old framework correctly derives eligible and aligned shares from the same activity lists`() {
        val inputs =
            listOf(
                createNonAlignedInput(
                    listOf(
                        nonAlignedActivity(
                            activityName = Activity.ElectricityGenerationFromNuclearEnergyInExistingInstallations,
                            relativeShareInPercent = BigDecimal("6"),
                        ),
                    ),
                ),
                createAlignedInput(
                    listOf(
                        alignedActivity(
                            activityName = Activity.ElectricityGenerationFromNuclearEnergyInExistingInstallations,
                            relativeShareInPercent = BigDecimal("4"),
                        ),
                    ),
                ),
            )

        val eligibleResult =
            applyTransformation(
                inputs, NUCLEAR_ELIGIBLE_TARGET_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
            )
        val eligibleValue = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(eligibleResult.dataPoint).value
        assertBigDecimalEquals("10", eligibleValue)

        val alignedResult =
            applyTransformation(
                inputs, NUCLEAR_ALIGNED_TARGET_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
            )
        val alignedValue = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(alignedResult.dataPoint).value
        assertBigDecimalEquals("4", alignedValue)
    }

    @Test
    fun `check that an activity only in the non-aligned list contributes to eligible but not to aligned share`() {
        val inputs =
            listOf(
                createNonAlignedInput(
                    listOf(
                        nonAlignedActivity(
                            activityName = Activity.ElectricityGenerationFromNuclearEnergyInExistingInstallations,
                            relativeShareInPercent = BigDecimal("6"),
                        ),
                    ),
                ),
                createAlignedInput(emptyList()),
            )

        val eligibleResult =
            applyTransformation(
                inputs, NUCLEAR_ELIGIBLE_TARGET_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
            )
        val eligibleValue = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(eligibleResult.dataPoint).value
        assertBigDecimalEquals("6", eligibleValue)

        val alignedResult =
            applyTransformation(
                inputs, NUCLEAR_ALIGNED_TARGET_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
            )
        val alignedValue = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(alignedResult.dataPoint).value
        assertBigDecimalEquals("0", alignedValue)
    }

    @Test
    fun `check that empty non-aligned and aligned lists in the old framework yield zero for eligible and aligned share`() {
        val inputs =
            listOf(
                createNonAlignedInput(emptyList()),
                createAlignedInput(emptyList()),
            )

        val eligibleResult =
            applyTransformation(
                inputs, NUCLEAR_ELIGIBLE_TARGET_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
            )
        val eligibleValue = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(eligibleResult.dataPoint).value
        assertBigDecimalEquals("0", eligibleValue)

        val alignedResult =
            applyTransformation(
                inputs, NUCLEAR_ALIGNED_TARGET_TYPE, "EuTaxonomyShare", activityMergeSpecs, sourceFrameworksByType,
            )
        val alignedValue = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(alignedResult.dataPoint).value
        assertBigDecimalEquals("0", alignedValue)
    }

    @Test
    fun `check that the result quality is merged from the non-aligned and aligned input qualities`() {
        val result =
            applyTransformation(
                listOf(
                    createNonAlignedInput(
                        listOf(
                            nonAlignedActivity(
                                activityName = Activity.ElectricityGenerationFromNuclearEnergyInExistingInstallations,
                                relativeShareInPercent = BigDecimal("6"),
                            ),
                        ),
                        quality = QualityOptions.Reported,
                    ),
                    createAlignedInput(
                        listOf(
                            alignedActivity(
                                activityName = Activity.ElectricityGenerationFromNuclearEnergyInExistingInstallations,
                                relativeShareInPercent = BigDecimal("4"),
                            ),
                        ),
                        quality = QualityOptions.Incomplete,
                    ),
                ),
                NUCLEAR_ELIGIBLE_TARGET_TYPE,
                "EuTaxonomyShare",
                activityMergeSpecs,
                sourceFrameworksByType,
            )

        val extendedDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal?>>(result.dataPoint)
        Assertions.assertEquals(QualityOptions.Incomplete, extendedDataPoint.quality)
    }
}
