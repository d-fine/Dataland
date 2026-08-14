package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackend.services.datapoints.applyTransformation
import org.dataland.datalandbackend.utils.FOSSIL_GAS_ALIGNED_TARGET_TYPE
import org.dataland.datalandbackend.utils.FOSSIL_GAS_ELIGIBLE_TARGET_TYPE
import org.dataland.datalandbackend.utils.NUCLEAR_ALIGNED_TARGET_TYPE
import org.dataland.datalandbackend.utils.NUCLEAR_ELIGIBLE_TARGET_TYPE
import org.dataland.datalandbackend.utils.assertBigDecimalEquals
import org.dataland.datalandbackend.utils.createEligibleOrAlignedInput
import org.dataland.datalandbackend.utils.eligibleOrAlignedActivity
import org.dataland.datalandbackend.utils.shareSpecs
import org.dataland.datalandbackend.utils.sourceFrameworksByType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class EuTaxonomyShareCalculationEligibleOrAlignedTest {
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
    fun `check that two activities with the same activity name in the new framework are summed`() {
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
                                activityName = Activity.ElectricityGenerationFromNuclearEnergyInExistingInstallations,
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
}
