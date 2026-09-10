package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.IntNode
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService.ValueType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SignificanceCheckServiceIndividualThresholdOverrideTest : SignificanceCheckServiceTestFixtures() {
    @Test
    fun `decimal change significant against lower individual threshold but not against global`() {
        val lowerIndividualThreshold = 0.1
        val overrides: Map<DataTypeEnum, Map<String, Double>> =
            mapOf(dummyFramework to mapOf(dummyDataPointType to lowerIndividualThreshold))
        val newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.2)
        val liveValue = DecimalNode(baseDecimalValue)

        assertFalse(
            service.hasSignificantChange(
                newValue = newValue,
                liveValue = liveValue,
                valueType = ValueType.DECIMAL,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
            "expected not significant against the global threshold",
        )
        assertTrue(
            service.hasSignificantChange(
                newValue = newValue,
                liveValue = liveValue,
                valueType = ValueType.DECIMAL,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds.copy(individualDecimalThresholds = overrides),
            ),
            "expected significant against the lower individual threshold",
        )
    }

    @Test
    fun `decimal change not significant against higher individual threshold but significant against global`() {
        val higherIndividualThreshold = 0.9
        val overrides: Map<DataTypeEnum, Map<String, Double>> =
            mapOf(dummyFramework to mapOf(dummyDataPointType to higherIndividualThreshold))
        val newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.6)
        val liveValue = DecimalNode(baseDecimalValue)

        assertTrue(
            service.hasSignificantChange(
                newValue = newValue,
                liveValue = liveValue,
                valueType = ValueType.DECIMAL,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
            "expected significant against the global threshold",
        )
        assertFalse(
            service.hasSignificantChange(
                newValue = newValue,
                liveValue = liveValue,
                valueType = ValueType.DECIMAL,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds.copy(individualDecimalThresholds = overrides),
            ),
            "expected not significant against the higher individual threshold",
        )
    }

    @Test
    fun `integer change significant against lower individual threshold but not against global`() {
        val lowerIndividualThreshold = 2L
        val overrides: Map<DataTypeEnum, Map<String, Long>> =
            mapOf(dummyFramework to mapOf(dummyDataPointType to lowerIndividualThreshold))
        val newValue = createIntegerNodeWithAbsoluteOffset(baseIntegerValue, 3)
        val liveValue = IntNode(baseIntegerValue)

        assertFalse(
            service.hasSignificantChange(
                newValue = newValue,
                liveValue = liveValue,
                valueType = ValueType.INTEGER,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
            "expected not significant against the global threshold",
        )
        assertTrue(
            service.hasSignificantChange(
                newValue = newValue,
                liveValue = liveValue,
                valueType = ValueType.INTEGER,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds.copy(individualIntegerThresholds = overrides),
            ),
            "expected significant against the lower individual threshold",
        )
    }

    @Test
    fun `integer change not significant against higher individual threshold but significant against global`() {
        val higherIndividualThreshold = 10L
        val overrides: Map<DataTypeEnum, Map<String, Long>> =
            mapOf(dummyFramework to mapOf(dummyDataPointType to higherIndividualThreshold))
        val newValue = createIntegerNodeWithAbsoluteOffset(baseIntegerValue, 8)
        val liveValue = IntNode(baseIntegerValue)

        assertTrue(
            service.hasSignificantChange(
                newValue = newValue,
                liveValue = liveValue,
                valueType = ValueType.INTEGER,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
            "expected significant against the global threshold",
        )
        assertFalse(
            service.hasSignificantChange(
                newValue = newValue,
                liveValue = liveValue,
                valueType = ValueType.INTEGER,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds.copy(individualIntegerThresholds = overrides),
            ),
            "expected not significant against the higher individual threshold",
        )
    }

    @Test
    fun `individual threshold falls back to global when no override exists for framework or field`() {
        val overrides: Map<DataTypeEnum, Map<String, Double>> =
            mapOf(DataTypeEnum.eutaxonomyMinusFinancials to mapOf("other-datapoint-type" to 0.1))

        assertFalse(
            service.hasSignificantChange(
                newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.0 + decimalRelativeThreshold * 0.95),
                liveValue = DecimalNode(baseDecimalValue),
                valueType = ValueType.DECIMAL,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds.copy(individualDecimalThresholds = overrides),
            ),
            "expected to fall back to the global threshold when no matching override exists",
        )
    }
}
