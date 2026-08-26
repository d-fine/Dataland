package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService.ValueType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SignificanceCheckServiceNullAndBooleanTest : SignificanceCheckServiceTestFixtures() {
    @Test
    fun `new value null returns false`() {
        assertFalse(
            service.hasSignificantChange(
                newValue = null,
                liveValue = TextNode("Yes"),
                valueType = ValueType.BOOLEAN,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
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
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `new value JSON null returns false`() {
        assertFalse(
            service.hasSignificantChange(
                newValue = NullNode.instance,
                liveValue = TextNode("Yes"),
                valueType = ValueType.BOOLEAN,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
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
                thresholds = defaultThresholds,
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
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `boolean change from Yes to No is significant`() {
        assertTrue(
            service.hasSignificantChange(
                newValue = TextNode("Yes"),
                liveValue = TextNode("No"),
                valueType = ValueType.BOOLEAN,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
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
                thresholds = defaultThresholds,
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
                thresholds = defaultThresholds,
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
                thresholds = defaultThresholds,
            ),
        )
    }

    @Test
    fun `unsupported type is never significant`() {
        assertFalse(
            service.hasSignificantChange(
                newValue = TextNode("someValue"),
                liveValue = TextNode("otherValue"),
                valueType = ValueType.UNSUPPORTED,
                dataPointType = dummyDataPointType,
                framework = dummyFramework,
                thresholds = defaultThresholds,
            ),
        )
    }
}
