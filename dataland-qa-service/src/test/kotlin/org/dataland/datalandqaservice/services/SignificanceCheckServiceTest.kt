package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService
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
                    originalValue = null,
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
                    originalValue = TextNode("Yes"),
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
                    originalValue = NullNode.instance,
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
                    originalValue = TextNode("Yes"),
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
                    originalValue = null,
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
                    originalValue = TextNode("Yes"),
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
                    originalValue = TextNode("No"),
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
                    originalValue = TextNode("Yes"),
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
                    originalValue = TextNode("No"),
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
        private fun decimal(value: Double): JsonNode = DecimalNode(BigDecimal.valueOf(value))

        @Test
        fun `decimal change above 50 percent relative threshold is significant`() {
            assertTrue(
                service.hasSignificantChange(
                    originalValue = decimal(200.0),
                    liveValue = decimal(100.0),
                    valueType = ValueType.DECIMAL,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `decimal change exactly at 50 percent relative threshold is not significant`() {
            assertFalse(
                service.hasSignificantChange(
                    originalValue = decimal(150.0),
                    liveValue = decimal(100.0),
                    valueType = ValueType.DECIMAL,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `decimal change below 50 percent relative threshold is not significant`() {
            assertFalse(
                service.hasSignificantChange(
                    originalValue = decimal(110.0),
                    liveValue = decimal(100.0),
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
                    originalValue = decimal(100.0),
                    liveValue = decimal(100.0),
                    valueType = ValueType.DECIMAL,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `decimal live value zero and non-zero original is significant`() {
            assertTrue(
                service.hasSignificantChange(
                    originalValue = decimal(10.0),
                    liveValue = decimal(0.0),
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
                    originalValue = decimal(0.0),
                    liveValue = decimal(0.0),
                    valueType = ValueType.DECIMAL,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `decimal decrease above 50 percent is significant`() {
            assertTrue(
                service.hasSignificantChange(
                    originalValue = decimal(40.0),
                    liveValue = decimal(100.0),
                    valueType = ValueType.DECIMAL,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }
    }

    @Nested
    inner class IntegerSignificanceTests {
        private fun integer(value: Int): JsonNode = IntNode(value)

        @Test
        fun `integer change above absolute threshold of 5 is significant`() {
            assertTrue(
                service.hasSignificantChange(
                    originalValue = integer(12),
                    liveValue = integer(5),
                    valueType = ValueType.INTEGER,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `integer change exactly at threshold of 5 is not significant`() {
            assertFalse(
                service.hasSignificantChange(
                    originalValue = integer(10),
                    liveValue = integer(5),
                    valueType = ValueType.INTEGER,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `integer change below absolute threshold of 5 is not significant`() {
            assertFalse(
                service.hasSignificantChange(
                    originalValue = integer(7),
                    liveValue = integer(5),
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
                    originalValue = integer(42),
                    liveValue = integer(42),
                    valueType = ValueType.INTEGER,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }

        @Test
        fun `negative integer change above threshold is significant`() {
            assertTrue(
                service.hasSignificantChange(
                    originalValue = integer(1),
                    liveValue = integer(10),
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
                    originalValue = TextNode("someValue"),
                    liveValue = TextNode("otherValue"),
                    valueType = ValueType.UNSUPPORTED,
                    dataPointType = dummyDataPointType,
                    framework = dummyFramework,
                ),
            )
        }
    }
}
