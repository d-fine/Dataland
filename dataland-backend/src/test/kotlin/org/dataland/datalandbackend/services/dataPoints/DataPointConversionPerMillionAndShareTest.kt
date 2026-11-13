package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.datapoints.applyTransformation
import org.dataland.datalandbackend.utils.assertBigDecimalEquals
import org.dataland.datalandbackend.utils.createDecimalDataPointJson
import org.dataland.datalandbackend.utils.createUploadedDataPoint
import org.dataland.datalandbackend.utils.dummySpecs
import org.dataland.datalandbackend.utils.sourceFrameworksByType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal

/**
 * Tests for the per-million and share-of-sum conversions used by the calculated SFDR fields.
 */
class DataPointConversionPerMillionAndShareTest {
    private fun inputs(vararg values: String): List<UploadedDataPoint> =
        values.map { createUploadedDataPoint(createDecimalDataPointJson(it)) }

    private fun calculate(
        method: String,
        vararg values: String,
    ): ExtendedDataPoint<BigDecimal> {
        val result = applyTransformation(inputs(*values), "dummy", method, dummySpecs, sourceFrameworksByType)
        return defaultObjectMapper.readValue(result.dataPoint)
    }

    @ParameterizedTest
    @CsvSource(
        "DivisionPerMillion,'200,4000000',50",
        "DivisionPerMillion,'0,4000000',0",
        "SumDivisionPerMillion,'100,100,1000000',200",
        "SumDivisionPerMillion,'10,20,30,2000000',30",
        "DivisionBySumPerMillion,'90,1000000,500000,500000',45",
        "SumDivisionBySumPerMillion,'10,20,30,1000000,500000',40",
        "SumDivisionBySumPerMillion,'10,20,30,1000000,500000,500000',30",
        "ShareOfSumInPercent,'1,3',25",
        "ShareOfSumInPercent,'0,5',0",
        "ShareOfSumInPercent,'5,0',100",
    )
    fun `check that the values are calculated with the correct units`(
        method: String,
        values: String,
        expected: String,
    ) {
        val result = calculate(method, *values.split(",").toTypedArray())
        assertBigDecimalEquals(expected, result.value)
    }

    @ParameterizedTest
    @CsvSource(
        "DivisionPerMillion,'5,0'",
        "SumDivisionPerMillion,'5,5,0'",
        "DivisionBySumPerMillion,'5,0,0'",
        "SumDivisionBySumPerMillion,'1,2,3,0,0'",
        "ShareOfSumInPercent,'0,0'",
    )
    fun `check that a zero divisor is rejected`(
        method: String,
        values: String,
    ) {
        assertThrows<IllegalArgumentException> { calculate(method, *values.split(",").toTypedArray()) }
    }

    @ParameterizedTest
    @CsvSource(
        "DivisionPerMillion,'1'",
        "DivisionPerMillion,'1,2,3'",
        "SumDivisionPerMillion,'1'",
        "DivisionBySumPerMillion,'1'",
        "SumDivisionBySumPerMillion,'1,2,3,4'",
        "ShareOfSumInPercent,'1'",
    )
    fun `check that too few or too many inputs are rejected`(
        method: String,
        values: String,
    ) {
        assertThrows<IllegalArgumentException> { calculate(method, *values.split(",").toTypedArray()) }
    }

    @Test
    fun `check that inputs without a value are rejected`() {
        val withoutValue = createUploadedDataPoint(createDecimalDataPointJson(null))
        val inputs = listOf(createUploadedDataPoint(createDecimalDataPointJson("1")), withoutValue)
        assertThrows<IllegalArgumentException> {
            applyTransformation(inputs, "dummy", "DivisionPerMillion", dummySpecs, sourceFrameworksByType)
        }
    }

    @Test
    fun `check that integer inputs are accepted by the share calculation`() {
        val integerInputs =
            listOf("2", "6").map {
                createUploadedDataPoint("""{"value": $it, "quality": "Reported"}""")
            }
        val result =
            applyTransformation(integerInputs, "dummy", "ShareOfSumInPercent", dummySpecs, sourceFrameworksByType)
        val parsed = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(result.dataPoint)
        assertBigDecimalEquals("25", parsed.value)
    }

    @Test
    fun `check that the generated comment contains the formula`() {
        val comment = calculate("SumDivisionBySumPerMillion", "1", "2", "3", "4", "5").comment
        assertTrue(comment!!.contains("([1] + [2] + [3]) / ([4] + [5]) * 10^6"))
        assertEquals(true, calculate("ShareOfSumInPercent", "1", "2").comment!!.contains("[1] / ([1] + [2]) * 100"))
    }
}
