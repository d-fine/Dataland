package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.node.IntNode
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService.ValueType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SignificanceCheckServiceIntegerTest : SignificanceCheckServiceTestFixtures() {
    @Test
    fun `integer increase above threshold is significant`() {
        assertTrue(
            service.hasSignificantChange(
                newValue = createIntegerNodeWithAbsoluteOffset(baseIntegerValue, integerAbsoluteThreshold.toInt() + 1),
                liveValue = IntNode(baseIntegerValue),
                valueType = ValueType.INTEGER,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `integer increase at threshold is not significant`() {
        assertFalse(
            service.hasSignificantChange(
                newValue = createIntegerNodeWithAbsoluteOffset(baseIntegerValue, integerAbsoluteThreshold.toInt()),
                liveValue = IntNode(baseIntegerValue),
                valueType = ValueType.INTEGER,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `integer increase below threshold is not significant`() {
        assertFalse(
            service.hasSignificantChange(
                newValue = createIntegerNodeWithAbsoluteOffset(baseIntegerValue, integerAbsoluteThreshold.toInt() - 1),
                liveValue = IntNode(baseIntegerValue),
                valueType = ValueType.INTEGER,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `integer decrease above threshold is significant`() {
        assertTrue(
            service.hasSignificantChange(
                newValue = createIntegerNodeWithAbsoluteOffset(baseIntegerValue, -(integerAbsoluteThreshold.toInt() + 1)),
                liveValue = IntNode(baseIntegerValue),
                valueType = ValueType.INTEGER,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `integer decrease at threshold is not significant`() {
        assertFalse(
            service.hasSignificantChange(
                newValue = createIntegerNodeWithAbsoluteOffset(baseIntegerValue, -integerAbsoluteThreshold.toInt()),
                liveValue = IntNode(baseIntegerValue),
                valueType = ValueType.INTEGER,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `integer decrease below threshold is not significant`() {
        assertFalse(
            service.hasSignificantChange(
                newValue = createIntegerNodeWithAbsoluteOffset(baseIntegerValue, -(integerAbsoluteThreshold.toInt() - 1)),
                liveValue = IntNode(baseIntegerValue),
                valueType = ValueType.INTEGER,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `integer unchanged is not significant`() {
        assertFalse(
            service.hasSignificantChange(
                newValue = IntNode(baseIntegerValue),
                liveValue = IntNode(baseIntegerValue),
                valueType = ValueType.INTEGER,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }
}
