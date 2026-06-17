package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService.Companion.DECIMAL_RELATIVE_THRESHOLD
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService.Companion.INTEGER_ABSOLUTE_THRESHOLD
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService.ValueType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class SignificanceCheckServiceTest {
    private val service = SignificanceCheckService()
    private val dummyFramework = DataTypeEnum.sfdr
    private val dummyDataPointType = "some-datapoint-type"

    private val baseDecimalValue = BigDecimal.valueOf(100.0)
    private val baseIntegerValue = 10

    private fun createDecimalNodeWithRelativeMultiplier(
        baseValue: BigDecimal,
        multiplier: Double,
    ): JsonNode = DecimalNode(baseValue.multiply(BigDecimal.valueOf(multiplier)))

    private fun createIntegerNodeWithAbsoluteOffset(
        baseValue: Int,
        absoluteOffset: Int,
    ): JsonNode = IntNode(baseValue + absoluteOffset)

    @Nested
    inner class ResolveValueTypeTests {
        @Test
        fun `extendedDecimal resolves to DECIMAL`() {
            assertEquals(ValueType.DECIMAL, service.resolveValueType("extendedDecimal"))
        }

        @Test
        fun `extendedInteger resolves to INTEGER`() {
            assertEquals(ValueType.INTEGER, service.resolveValueType("extendedInteger"))
        }

        @Test
        fun `extendedEnumYesNo resolves to BOOLEAN`() {
            assertEquals(ValueType.BOOLEAN, service.resolveValueType("extendedEnumYesNo"))
        }

        @Test
        fun `unknown base type id resolves to UNSUPPORTED`() {
            assertEquals(ValueType.UNSUPPORTED, service.resolveValueType("extendedEnumYesNoNa"))
            assertEquals(ValueType.UNSUPPORTED, service.resolveValueType("extendedCurrency"))
            assertEquals(ValueType.UNSUPPORTED, service.resolveValueType("plainDate"))
            assertEquals(ValueType.UNSUPPORTED, service.resolveValueType("some-unknown-type"))
        }
    }

    @Nested
    inner class NullValueTests {
        @Test
        fun `original value null returns false`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = null,
                    liveValue = TextNode("Yes"),
                    valueType = ValueType.BOOLEAN,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `live value null returns false`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = TextNode("Yes"),
                    liveValue = null,
                    valueType = ValueType.BOOLEAN,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `original value JSON null returns false`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = NullNode.instance,
                    liveValue = TextNode("Yes"),
                    valueType = ValueType.BOOLEAN,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `live value JSON null returns false`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = TextNode("Yes"),
                    liveValue = NullNode.instance,
                    valueType = ValueType.BOOLEAN,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `both null returns false`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = null,
                    liveValue = null,
                    valueType = ValueType.BOOLEAN,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }
    }

    @Nested
    inner class BooleanSignificanceTests {
        @Test
        fun `boolean change from Yes to No is significant`() {
            assertTrue(
                service.hasSignificantChange(
                    newValue = TextNode("Yes"),
                    liveValue = TextNode("No"),
                    valueType = ValueType.BOOLEAN,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `boolean change from No to Yes is significant`() {
            assertTrue(
                service.hasSignificantChange(
                    newValue = TextNode("No"),
                    liveValue = TextNode("Yes"),
                    valueType = ValueType.BOOLEAN,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `same boolean value Yes to Yes is not significant`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = TextNode("Yes"),
                    liveValue = TextNode("Yes"),
                    valueType = ValueType.BOOLEAN,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `same boolean value No to No is not significant`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = TextNode("No"),
                    liveValue = TextNode("No"),
                    valueType = ValueType.BOOLEAN,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }
    }

    @Nested
    inner class DecimalSignificanceTests {
        @Test
        fun `decimal increase above threshold is significant`() {
            assertTrue(
                service.hasSignificantChange(
                    newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.0 + DECIMAL_RELATIVE_THRESHOLD * 1.05),
                    liveValue = DecimalNode(baseDecimalValue),
                    valueType = ValueType.DECIMAL,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `decimal increase at threshold is not significant `() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.0 + DECIMAL_RELATIVE_THRESHOLD),
                    liveValue = DecimalNode(baseDecimalValue),
                    valueType = ValueType.DECIMAL,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `decimal increase below threshold is not significant`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.0 + DECIMAL_RELATIVE_THRESHOLD * 0.95),
                    liveValue = DecimalNode(baseDecimalValue),
                    valueType = ValueType.DECIMAL,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `decimal decrease above threshold is significant`() {
            assertTrue(
                service.hasSignificantChange(
                    newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.0 - DECIMAL_RELATIVE_THRESHOLD * 1.05),
                    liveValue = DecimalNode(baseDecimalValue),
                    valueType = ValueType.DECIMAL,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `decimal decrease at threshold is not significant`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.0 - DECIMAL_RELATIVE_THRESHOLD),
                    liveValue = DecimalNode(baseDecimalValue),
                    valueType = ValueType.DECIMAL,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `decimal decrease below threshold is not significant`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = createDecimalNodeWithRelativeMultiplier(baseDecimalValue, 1.0 - DECIMAL_RELATIVE_THRESHOLD * 0.95),
                    liveValue = DecimalNode(baseDecimalValue),
                    valueType = ValueType.DECIMAL,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
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
                ),
            )
        }
    }

    @Nested
    inner class IntegerSignificanceTests {
        @Test
        fun `integer increase above threshold is significant`() {
            assertTrue(
                service.hasSignificantChange(
                    newValue = createIntegerNodeWithAbsoluteOffset(baseIntegerValue, INTEGER_ABSOLUTE_THRESHOLD.toInt() + 1),
                    liveValue = IntNode(baseIntegerValue),
                    valueType = ValueType.INTEGER,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `integer increase at threshold is not significant`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = createIntegerNodeWithAbsoluteOffset(baseIntegerValue, INTEGER_ABSOLUTE_THRESHOLD.toInt()),
                    liveValue = IntNode(baseIntegerValue),
                    valueType = ValueType.INTEGER,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `integer increase below threshold is not significant`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = createIntegerNodeWithAbsoluteOffset(baseIntegerValue, INTEGER_ABSOLUTE_THRESHOLD.toInt() - 1),
                    liveValue = IntNode(baseIntegerValue),
                    valueType = ValueType.INTEGER,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `integer decrease above threshold is significant`() {
            assertTrue(
                service.hasSignificantChange(
                    newValue = createIntegerNodeWithAbsoluteOffset(baseIntegerValue, -(INTEGER_ABSOLUTE_THRESHOLD.toInt() + 1)),
                    liveValue = IntNode(baseIntegerValue),
                    valueType = ValueType.INTEGER,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `integer decrease at threshold is not significant`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = createIntegerNodeWithAbsoluteOffset(baseIntegerValue, -INTEGER_ABSOLUTE_THRESHOLD.toInt()),
                    liveValue = IntNode(baseIntegerValue),
                    valueType = ValueType.INTEGER,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `integer decrease below threshold is not significant`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = createIntegerNodeWithAbsoluteOffset(baseIntegerValue, -(INTEGER_ABSOLUTE_THRESHOLD.toInt() - 1)),
                    liveValue = IntNode(baseIntegerValue),
                    valueType = ValueType.INTEGER,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
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
                ),
            )
        }
    }

    @Nested
    inner class UnsupportedTypeTests {
        @Test
        fun `unsupported type is never significant`() {
            assertFalse(
                service.hasSignificantChange(
                    newValue = TextNode("someValue"),
                    liveValue = TextNode("otherValue"),
                    valueType = ValueType.UNSUPPORTED,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }
    }
}
