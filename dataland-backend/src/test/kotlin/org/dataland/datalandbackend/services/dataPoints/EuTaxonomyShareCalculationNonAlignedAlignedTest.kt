package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackend.services.datapoints.applyTransformation
import org.dataland.datalandbackend.utils.NUCLEAR_ALIGNED_TARGET_TYPE
import org.dataland.datalandbackend.utils.NUCLEAR_ELIGIBLE_TARGET_TYPE
import org.dataland.datalandbackend.utils.activityMergeSpecs
import org.dataland.datalandbackend.utils.alignedActivity
import org.dataland.datalandbackend.utils.assertBigDecimalEquals
import org.dataland.datalandbackend.utils.createAlignedInput
import org.dataland.datalandbackend.utils.createNonAlignedInput
import org.dataland.datalandbackend.utils.nonAlignedActivity
import org.dataland.datalandbackend.utils.sourceFrameworksByType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class EuTaxonomyShareCalculationNonAlignedAlignedTest {
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
