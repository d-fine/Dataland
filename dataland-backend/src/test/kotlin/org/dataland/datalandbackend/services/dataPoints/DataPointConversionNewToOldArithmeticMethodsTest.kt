package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.services.datapoints.DataPointConversion
import org.dataland.datalandbackend.services.datapoints.applyTransformation
import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackend.utils.assertBigDecimalEquals
import org.dataland.datalandbackend.utils.createCommentSourceFrameworksByType
import org.dataland.datalandbackend.utils.createCommentSpecs
import org.dataland.datalandbackend.utils.createCurrencyDataPointJson
import org.dataland.datalandbackend.utils.createCurrencySpecs
import org.dataland.datalandbackend.utils.createDecimalDataPointJson
import org.dataland.datalandbackend.utils.createDummyUploadedDataPoint
import org.dataland.datalandbackend.utils.createUploadedDataPoint
import org.dataland.datalandbackend.utils.dummySpecs
import org.dataland.datalandbackend.utils.sourceBlock
import org.dataland.datalandbackend.utils.sourceFrameworksByType
import org.dataland.datalandbackend.utils.sourcesSection
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

/**
 * Tests for the arithmetic [DataPointConversion] methods introduced to support calculated fields in the old EU taxonomy
 * frameworks, calculated from fields of the new EU taxonomy frameworks. (Subtraction, ComplementToPercent,
 * MultiplicationByPercent, MultiplicationByComplementPercent, MultiplicationByPercentMinusCurrency). All correctness
 * assertions go through the public [applyTransformation] entry point, mirroring the existing
 * Sum/Division/DivisionByPercent tests in [DataPointConversionTest].
 */
class DataPointConversionNewToOldArithmeticMethodsTest {
    private val currencyTargetType = "currencyTargetType"
    private val currencySpecs = createCurrencySpecs(currencyTargetType)

    private companion object {
        const val NUMERIC_DATA_POINT_HALF = "json/dataPoints/numericDataPointHalf.json"
        const val NUMERIC_DATA_POINT_ONE = "json/dataPoints/numericDataPointOne.json"
        const val NON_NUMERIC_DATA_POINT = "json/dataPoints/nonNumericDataPoint.json"
        const val DATA_POINT_WITHOUT_VALUE = "json/dataPoints/dataPointWithoutValue.json"

        fun calculationComment(
            formula: String,
            vararg sourceBlocks: String,
        ): String =
            "This data point was calculated using the following formula: $formula\n\n***\n\n" +
                sourcesSection(*sourceBlocks)
    }

    private fun numericInput(fixturePath: String) = createUploadedDataPoint(TestResourceFileReader.getJsonString(fixturePath))

    // region Subtraction

    @Test
    fun `check that subtraction of data points works as expected`() {
        val result =
            defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(
                applyTransformation(
                    listOf(numericInput(NUMERIC_DATA_POINT_ONE), numericInput(NUMERIC_DATA_POINT_HALF)),
                    "dummy",
                    "Subtraction",
                    dummySpecs,
                    sourceFrameworksByType,
                ).dataPoint,
            )
        assertBigDecimalEquals("0.5", result.value)
    }

    @Test
    fun `check that subtraction of currency data points preserves the currency`() {
        val minuend = createUploadedDataPoint(createCurrencyDataPointJson("1.0", "EUR"))
        val subtrahend = createUploadedDataPoint(createCurrencyDataPointJson("0.5", "EUR"))
        val result =
            defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(
                applyTransformation(
                    listOf(minuend, subtrahend),
                    currencyTargetType,
                    "Subtraction",
                    currencySpecs,
                    sourceFrameworksByType,
                ).dataPoint,
            )
        assertBigDecimalEquals("0.5", result.value)
        assertEquals("EUR", result.currency)
    }

    @Test
    fun `check that subtraction of currency data points rejects mixed currencies`() {
        val minuend = createUploadedDataPoint(createCurrencyDataPointJson("1.0", "EUR"))
        val subtrahend = createUploadedDataPoint(createCurrencyDataPointJson("0.5", "USD"))
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(minuend, subtrahend),
                currencyTargetType,
                "Subtraction",
                currencySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that subtraction of currency data points rejects missing currencies`() {
        val minuend = createUploadedDataPoint(createCurrencyDataPointJson("1.0", null))
        val subtrahend = createUploadedDataPoint(createCurrencyDataPointJson("0.5", "EUR"))
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(minuend, subtrahend),
                currencyTargetType,
                "Subtraction",
                currencySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that subtraction of data points throws the expected exceptions`() {
        // Too few inputs
        assertThrows<IllegalArgumentException> {
            applyTransformation(listOf(numericInput(NUMERIC_DATA_POINT_HALF)), "dummy", "Subtraction", dummySpecs, sourceFrameworksByType)
        }
        // Too many inputs
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(numericInput(NUMERIC_DATA_POINT_HALF), numericInput(NUMERIC_DATA_POINT_HALF), numericInput(NUMERIC_DATA_POINT_ONE)),
                "dummy",
                "Subtraction",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        // Non-numeric input
        assertThrows<JsonProcessingException> {
            applyTransformation(
                listOf(numericInput(NON_NUMERIC_DATA_POINT), numericInput(NUMERIC_DATA_POINT_HALF)),
                "dummy",
                "Subtraction",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        // null-value input
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(numericInput(DATA_POINT_WITHOUT_VALUE), numericInput(NUMERIC_DATA_POINT_HALF)),
                "dummy",
                "Subtraction",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that the subtraction comment renders the expected formula`() {
        val input1 = createDummyUploadedDataPoint("type1")
        val input2 = createDummyUploadedDataPoint("type2")
        val dataPoints =
            listOf(
                ExtendedDataPoint(value = BigDecimal.TEN, quality = QualityOptions.Reported),
                ExtendedDataPoint(value = BigDecimal.ONE, quality = QualityOptions.Reported),
            )
        val comment =
            DataPointConversion.SUBTRACTION.createComment(
                listOf(input1, input2),
                createCommentSpecs(),
                dataPoints,
                createCommentSourceFrameworksByType(),
            )
        assertEquals(
            calculationComment("[1] - [2]", sourceBlock(1, "Input1"), sourceBlock(2, "Input2")),
            comment,
        )
    }

    // endregion

    // region ComplementToPercent

    @Test
    fun `check that complement to percent of a data point works as expected`() {
        val input = createUploadedDataPoint(createDecimalDataPointJson("30"))
        val result =
            defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(
                applyTransformation(listOf(input), "dummy", "ComplementToPercent", dummySpecs, sourceFrameworksByType).dataPoint,
            )
        assertBigDecimalEquals("70", result.value)
    }

    @Test
    fun `check that complement to percent throws the expected exceptions`() {
        // Empty list as input
        assertThrows<IllegalArgumentException> {
            applyTransformation(emptyList(), "dummy", "ComplementToPercent", dummySpecs, sourceFrameworksByType)
        }
        // Too many inputs
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(numericInput(NUMERIC_DATA_POINT_HALF), numericInput(NUMERIC_DATA_POINT_ONE)),
                "dummy",
                "ComplementToPercent",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        // null-value input
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(numericInput(DATA_POINT_WITHOUT_VALUE)),
                "dummy",
                "ComplementToPercent",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that the complement to percent comment renders the expected formula`() {
        val input = createDummyUploadedDataPoint("type1")
        val dataPoint = ExtendedDataPoint(value = BigDecimal.TEN, quality = QualityOptions.Reported)
        val comment =
            DataPointConversion.COMPLEMENT_TO_PERCENT.createComment(
                listOf(input),
                createCommentSpecs(),
                listOf(dataPoint),
                createCommentSourceFrameworksByType(),
            )
        assertEquals(calculationComment("100 - [1]", sourceBlock(1, "Input1")), comment)
    }

    // endregion

    // region MultiplicationByPercent

    @Test
    fun `check that multiplication by percent of data points works as expected`() {
        val value = createUploadedDataPoint(createDecimalDataPointJson("40"))
        val percent = createUploadedDataPoint(createDecimalDataPointJson("25"))
        val result =
            defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(
                applyTransformation(listOf(value, percent), "dummy", "MultiplicationByPercent", dummySpecs, sourceFrameworksByType)
                    .dataPoint,
            )
        assertBigDecimalEquals("10", result.value)
    }

    @Test
    fun `check that multiplication by percent of currency data points preserves the currency`() {
        val value = createUploadedDataPoint(createCurrencyDataPointJson("40", "EUR"))
        val percent = createUploadedDataPoint(createDecimalDataPointJson("25"))
        val result =
            defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(
                applyTransformation(
                    listOf(value, percent),
                    currencyTargetType,
                    "MultiplicationByPercent",
                    currencySpecs,
                    sourceFrameworksByType,
                ).dataPoint,
            )
        assertBigDecimalEquals("10", result.value)
        assertEquals("EUR", result.currency)
    }

    @Test
    fun `check that multiplication by percent of currency data points rejects missing currency`() {
        val value = createUploadedDataPoint(createCurrencyDataPointJson("40", null))
        val percent = createUploadedDataPoint(createDecimalDataPointJson("25"))
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(value, percent),
                currencyTargetType,
                "MultiplicationByPercent",
                currencySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that multiplication by percent of data points throws the expected exceptions`() {
        // To few inputs
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(numericInput(NUMERIC_DATA_POINT_HALF)),
                "dummy",
                "MultiplicationByPercent",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        // To many inputs
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(numericInput(NUMERIC_DATA_POINT_HALF), numericInput(NUMERIC_DATA_POINT_ONE), numericInput(NUMERIC_DATA_POINT_ONE)),
                "dummy",
                "MultiplicationByPercent",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        // null-value input
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(numericInput(DATA_POINT_WITHOUT_VALUE), numericInput(NUMERIC_DATA_POINT_HALF)),
                "dummy",
                "MultiplicationByPercent",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that the multiplication by percent comment renders the expected formula`() {
        val input1 = createDummyUploadedDataPoint("type1")
        val input2 = createDummyUploadedDataPoint("type2")
        val dataPoints =
            listOf(
                ExtendedDataPoint(value = BigDecimal.TEN, quality = QualityOptions.Reported),
                ExtendedDataPoint(value = BigDecimal.ONE, quality = QualityOptions.Reported),
            )
        val comment =
            DataPointConversion.MULTIPLICATION_BY_PERCENT.createComment(
                listOf(input1, input2),
                createCommentSpecs(),
                dataPoints,
                createCommentSourceFrameworksByType(),
            )
        assertEquals(
            calculationComment("[1] * [2] / 100", sourceBlock(1, "Input1"), sourceBlock(2, "Input2")),
            comment,
        )
    }

    // endregion

    // region MultiplicationByComplementPercent

    @Test
    fun `check that multiplication by complement percent of data points works as expected`() {
        val value = createUploadedDataPoint(createDecimalDataPointJson("40"))
        val percent = createUploadedDataPoint(createDecimalDataPointJson("25"))
        val result =
            defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(
                applyTransformation(
                    listOf(value, percent),
                    "dummy",
                    "MultiplicationByComplementPercent",
                    dummySpecs,
                    sourceFrameworksByType,
                ).dataPoint,
            )
        assertBigDecimalEquals("30", result.value)
    }

    @Test
    fun `check that multiplication by complement percent of currency data points preserves the currency`() {
        val value = createUploadedDataPoint(createCurrencyDataPointJson("40", "EUR"))
        val percent = createUploadedDataPoint(createDecimalDataPointJson("25"))
        val result =
            defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(
                applyTransformation(
                    listOf(value, percent),
                    currencyTargetType,
                    "MultiplicationByComplementPercent",
                    currencySpecs,
                    sourceFrameworksByType,
                ).dataPoint,
            )
        assertBigDecimalEquals("30", result.value)
        assertEquals("EUR", result.currency)
    }

    @Test
    fun `check that multiplication by complement percent of currency data points rejects missing currency`() {
        val value = createUploadedDataPoint(createCurrencyDataPointJson("40", null))
        val percent = createUploadedDataPoint(createDecimalDataPointJson("25"))
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(value, percent),
                currencyTargetType,
                "MultiplicationByComplementPercent",
                currencySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that multiplication by complement percent of data points throws the expected exceptions`() {
        // Too few arguments
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(numericInput(NUMERIC_DATA_POINT_HALF)),
                "dummy",
                "MultiplicationByComplementPercent",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        // Too many arguments
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(numericInput(NUMERIC_DATA_POINT_HALF), numericInput(NUMERIC_DATA_POINT_HALF), numericInput(NUMERIC_DATA_POINT_HALF)),
                "dummy",
                "MultiplicationByComplementPercent",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        // null-value input
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(numericInput(DATA_POINT_WITHOUT_VALUE), numericInput(NUMERIC_DATA_POINT_HALF)),
                "dummy",
                "MultiplicationByComplementPercent",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that the multiplication by complement percent comment renders the expected formula`() {
        val input1 = createDummyUploadedDataPoint("type1")
        val input2 = createDummyUploadedDataPoint("type2")
        val dataPoints =
            listOf(
                ExtendedDataPoint(value = BigDecimal.TEN, quality = QualityOptions.Reported),
                ExtendedDataPoint(value = BigDecimal.ONE, quality = QualityOptions.Reported),
            )
        val comment =
            DataPointConversion.MULTIPLICATION_BY_COMPLEMENT_PERCENT.createComment(
                listOf(input1, input2),
                createCommentSpecs(),
                dataPoints,
                createCommentSourceFrameworksByType(),
            )
        assertEquals(
            calculationComment("[1] * (100 - [2]) / 100", sourceBlock(1, "Input1"), sourceBlock(2, "Input2")),
            comment,
        )
    }

    // endregion

    // region MultiplicationByPercentMinusCurrency

    @Test
    fun `check that multiplication by percent minus currency works as expected`() {
        val value = createUploadedDataPoint(createCurrencyDataPointJson("40", "EUR"))
        val percent = createUploadedDataPoint(createDecimalDataPointJson("25"))
        val amountToSubtract = createUploadedDataPoint(createCurrencyDataPointJson("3", "EUR"))
        val result =
            defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(
                applyTransformation(
                    listOf(value, percent, amountToSubtract),
                    "dummy",
                    "MultiplicationByPercentMinusCurrency",
                    dummySpecs,
                    sourceFrameworksByType,
                ).dataPoint,
            )
        assertBigDecimalEquals("7", result.value)
        assertEquals("EUR", result.currency)
    }

    @Test
    fun `check that multiplication by percent minus currency rejects mixed currencies`() {
        val value = createUploadedDataPoint(createCurrencyDataPointJson("40", "EUR"))
        val percent = createUploadedDataPoint(createDecimalDataPointJson("25"))
        val amountToSubtract = createUploadedDataPoint(createCurrencyDataPointJson("3", "USD"))
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(value, percent, amountToSubtract),
                "dummy",
                "MultiplicationByPercentMinusCurrency",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that multiplication by percent minus currency rejects missing currencies`() {
        val value = createUploadedDataPoint(createCurrencyDataPointJson("40", null))
        val percent = createUploadedDataPoint(createDecimalDataPointJson("25"))
        val amountToSubtract = createUploadedDataPoint(createCurrencyDataPointJson("3", "EUR"))
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(value, percent, amountToSubtract),
                "dummy",
                "MultiplicationByPercentMinusCurrency",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that multiplication by percent minus currency throws the expected exceptions`() {
        val value = createUploadedDataPoint(createCurrencyDataPointJson("40", "EUR"))
        val percent = createUploadedDataPoint(createDecimalDataPointJson("25"))
        val amountToSubtract = createUploadedDataPoint(createCurrencyDataPointJson("3", "EUR"))
        // Too few arguments
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(value, percent),
                "dummy",
                "MultiplicationByPercentMinusCurrency",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        // Too many arguments
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(value, percent, amountToSubtract, amountToSubtract),
                "dummy",
                "MultiplicationByPercentMinusCurrency",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        // null-value input
        val amountToSubtractWithoutValue = createUploadedDataPoint("""{"currency":"EUR","quality":"Reported"}""")
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(value, percent, amountToSubtractWithoutValue),
                "dummy",
                "MultiplicationByPercentMinusCurrency",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that the multiplication by percent minus currency comment renders the expected formula`() {
        val input1 = createDummyUploadedDataPoint("type1")
        val input2 = createDummyUploadedDataPoint("type2")
        val input3 = createDummyUploadedDataPoint("type3")
        val dataPoints =
            listOf(
                ExtendedDataPoint(value = BigDecimal.TEN, quality = QualityOptions.Reported),
                ExtendedDataPoint(value = BigDecimal.ONE, quality = QualityOptions.Reported),
                ExtendedDataPoint(value = BigDecimal.ONE, quality = QualityOptions.Reported),
            )
        val comment =
            DataPointConversion.MULTIPLICATION_BY_PERCENT_MINUS_CURRENCY.createComment(
                listOf(input1, input2, input3),
                createCommentSpecs(),
                dataPoints,
                createCommentSourceFrameworksByType(),
            )
        assertEquals(
            calculationComment(
                "([1] * [2] / 100) - [3]",
                sourceBlock(1, "Input1"),
                sourceBlock(2, "Input2"),
                sourceBlock(3, "Input3"),
            ),
            comment,
        )
    }

    // endregion
}
