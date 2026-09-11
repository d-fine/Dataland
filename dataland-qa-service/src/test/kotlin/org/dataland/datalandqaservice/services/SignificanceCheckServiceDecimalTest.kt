package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.node.DecimalNode
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService.ValueType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class SignificanceCheckServiceDecimalTest : SignificanceCheckServiceTestFixtures() {
    @Test
    fun `decimal increase above threshold is significant`() {
        assertTrue(
            service.hasSignificantChange(
                newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.0 + decimalRelativeThreshold * 1.05),
                liveValue = DecimalNode(baseDecimalValue),
                valueType = ValueType.DECIMAL,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `decimal increase at threshold is not significant `() {
        assertFalse(
            service.hasSignificantChange(
                newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.0 + decimalRelativeThreshold),
                liveValue = DecimalNode(baseDecimalValue),
                valueType = ValueType.DECIMAL,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `decimal increase below threshold is not significant`() {
        assertFalse(
            service.hasSignificantChange(
                newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.0 + decimalRelativeThreshold * 0.95),
                liveValue = DecimalNode(baseDecimalValue),
                valueType = ValueType.DECIMAL,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `decimal decrease above threshold is significant`() {
        assertTrue(
            service.hasSignificantChange(
                newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.0 - decimalRelativeThreshold * 1.05),
                liveValue = DecimalNode(baseDecimalValue),
                valueType = ValueType.DECIMAL,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `decimal decrease at threshold is not significant`() {
        assertFalse(
            service.hasSignificantChange(
                newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.0 - decimalRelativeThreshold),
                liveValue = DecimalNode(baseDecimalValue),
                valueType = ValueType.DECIMAL,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `decimal decrease below threshold is not significant`() {
        assertFalse(
            service.hasSignificantChange(
                newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.0 - decimalRelativeThreshold * 0.95),
                liveValue = DecimalNode(baseDecimalValue),
                valueType = ValueType.DECIMAL,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `decimal unchanged is not significant`() {
        assertFalse(
            service.hasSignificantChange(
                newValue = DecimalNode(baseDecimalValue),
                liveValue = DecimalNode(baseDecimalValue),
                valueType = ValueType.DECIMAL,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `decimal live value zero and non-zero new value is significant`() {
        assertTrue(
            service.hasSignificantChange(
                newValue = DecimalNode(baseDecimalValue),
                liveValue = DecimalNode(BigDecimal.ZERO),
                valueType = ValueType.DECIMAL,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `decimal both values zero is not significant`() {
        assertFalse(
            service.hasSignificantChange(
                newValue = DecimalNode(BigDecimal.ZERO),
                liveValue = DecimalNode(BigDecimal.ZERO),
                valueType = ValueType.DECIMAL,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }
}
