package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
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
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.util.stream.Stream
import kotlin.reflect.KClass

/**
 * Tests for the arithmetic [DataPointConversion] methods introduced to support calculated fields in the old EU taxonomy
 * frameworks, calculated from fields of the new EU taxonomy frameworks. (Subtraction, ComplementToPercent,
 * MultiplicationByPercent, MultiplicationByComplementPercent, MultiplicationByPercentMinusCurrency). All correctness
 * assertions go through the public [applyTransformation] entry point, mirroring the existing
 * Sum/Division/DivisionByPercent tests in [DataPointConversionTest].
 */
class DataPointConversionNewToOldArithmeticMethodsTest {
    private val currencyTargetType = CURRENCY_TARGET_TYPE
    private val currencySpecs = createCurrencySpecs(currencyTargetType)

    /**
     * A calculation method together with a minimal set of valid inputs for it, used only to derive the
     * invalid-input cases in [deriveInvalidInputCases]. The valid inputs are never expected to succeed on their
     * own here; they only serve as a baseline from which invalid variants (too few/too many/non-numeric/null-value
     * inputs) are derived.
     */
    data class PreInvalidInputFixture(
        val calculationMethod: String,
        val validInputs: List<UploadedDataPoint>,
        val targetType: DataPointType = "dummy",
        val specs: Map<DataPointType, DataPointTypeSpecification> = dummySpecs,
        val requiresOrderedInputs: Boolean = true,
    )

    /**
     * A single case exercised by [check that calculation methods throw the expected exceptions for invalid inputs].
     * [description] is used as the parameterized test's display name.
     */
    data class ArithmeticExceptionCase(
        val description: String,
        val fixture: PreInvalidInputFixture,
        val inputs: Collection<UploadedDataPoint>,
        val expectedException: KClass<out Throwable>,
    ) {
        override fun toString() = description
    }

    private companion object {
        const val NUMERIC_DATA_POINT_HALF = "json/dataPoints/numericDataPointHalf.json"
        const val NUMERIC_DATA_POINT_ONE = "json/dataPoints/numericDataPointOne.json"
        const val NON_NUMERIC_DATA_POINT = "json/dataPoints/nonNumericDataPoint.json"
        const val DATA_POINT_WITHOUT_VALUE = "json/dataPoints/dataPointWithoutValue.json"
        const val CURRENCY_TARGET_TYPE = "currencyTargetType"

        // Calculation method names, shared between the invalid-input fixtures below and the individual
        // per-method tests further down in this file.
        const val SUBTRACTION = "Subtraction"
        const val COMPLEMENT_TO_PERCENT = "ComplementToPercent"
        const val MULTIPLICATION_BY_PERCENT = "MultiplicationByPercent"
        const val MULTIPLICATION_BY_COMPLEMENT_PERCENT = "MultiplicationByComplementPercent"
        const val MULTIPLICATION_BY_PERCENT_MINUS_CURRENCY = "MultiplicationByPercentMinusCurrency"

        fun calculationComment(
            formula: String,
            vararg sourceBlocks: String,
        ): String =
            "This data point was calculated using the following formula: $formula\n\n***\n\n" +
                sourcesSection(*sourceBlocks)

        fun numericInput(fixturePath: String) = createUploadedDataPoint(TestResourceFileReader.getJsonString(fixturePath))

        // Pre-built inputs reused across the invalid-input cases derived in deriveInvalidInputCases below.
        val EXTRA_NUMERIC_INPUT = numericInput(NUMERIC_DATA_POINT_ONE)
        val NON_NUMERIC_INPUT = numericInput(NON_NUMERIC_DATA_POINT)
        val NULL_VALUE_INPUT = numericInput(DATA_POINT_WITHOUT_VALUE)

        /**
         * The calculation methods, each with a minimal valid set of inputs, used to derive the invalid-input cases
         * below. Every deserialization method reads its BigDecimal-valued inputs the same way (parse failure ->
         * [JsonProcessingException], null value -> [IllegalArgumentException]) before any type-specific
         * (e.g. currency) validation runs, so a single generic corruption (non-numeric/null-value fixture,
         * arity +-1) is enough to derive the expected-exception cases for every method below in
         * [deriveInvalidInputCases], including the currency-based one.
         */
        val CONCRETE_PRE_INVALID_INPUT_FIXTURES =
            listOf(
                PreInvalidInputFixture(
                    SUBTRACTION,
                    listOf(numericInput(NUMERIC_DATA_POINT_HALF), numericInput(NUMERIC_DATA_POINT_HALF)),
                ),
                PreInvalidInputFixture(
                    COMPLEMENT_TO_PERCENT,
                    listOf(numericInput(NUMERIC_DATA_POINT_HALF)),
                    requiresOrderedInputs = false,
                ),
                PreInvalidInputFixture(
                    MULTIPLICATION_BY_PERCENT,
                    listOf(numericInput(NUMERIC_DATA_POINT_HALF), numericInput(NUMERIC_DATA_POINT_HALF)),
                ),
                PreInvalidInputFixture(
                    MULTIPLICATION_BY_COMPLEMENT_PERCENT,
                    listOf(numericInput(NUMERIC_DATA_POINT_HALF), numericInput(NUMERIC_DATA_POINT_HALF)),
                ),
                PreInvalidInputFixture(
                    MULTIPLICATION_BY_PERCENT_MINUS_CURRENCY,
                    listOf(
                        createUploadedDataPoint(createCurrencyDataPointJson("40", "EUR")),
                        createUploadedDataPoint(createDecimalDataPointJson("25")),
                        createUploadedDataPoint(createCurrencyDataPointJson("3", "EUR")),
                    ),
                    targetType = CURRENCY_TARGET_TYPE,
                    specs = createCurrencySpecs(CURRENCY_TARGET_TYPE),
                ),
            )

        /**
         * Derives the invalid-input cases exercised for [fixture]: too few inputs, too many inputs, a
         * non-numeric input, a null-value input, and inputs provided as an unordered [Collection]
         * (for methods where input position matter).
         */
        fun deriveInvalidInputCases(fixture: PreInvalidInputFixture): List<ArithmeticExceptionCase> {
            val validInputs = fixture.validInputs
            val arityAndValueCases =
                listOf(
                    ArithmeticExceptionCase(
                        description = "${fixture.calculationMethod}: too few inputs",
                        fixture = fixture,
                        inputs = validInputs.dropLast(1),
                        expectedException = IllegalArgumentException::class,
                    ),
                    ArithmeticExceptionCase(
                        description = "${fixture.calculationMethod}: too many inputs",
                        fixture = fixture,
                        inputs = validInputs + EXTRA_NUMERIC_INPUT,
                        expectedException = IllegalArgumentException::class,
                    ),
                    ArithmeticExceptionCase(
                        description = "${fixture.calculationMethod}: non-numeric input",
                        fixture = fixture,
                        inputs = listOf(NON_NUMERIC_INPUT) + validInputs.drop(1),
                        expectedException = JsonProcessingException::class,
                    ),
                    ArithmeticExceptionCase(
                        description = "${fixture.calculationMethod}: null-value input",
                        fixture = fixture,
                        inputs = validInputs.dropLast(1) + NULL_VALUE_INPUT,
                        expectedException = IllegalArgumentException::class,
                    ),
                )
            val orderedInputCases =
                if (fixture.requiresOrderedInputs) {
                    listOf(
                        ArithmeticExceptionCase(
                            description = "${fixture.calculationMethod}: inputs provided as an unordered collection",
                            fixture = fixture,
                            inputs = validInputs.toSet(),
                            expectedException = IllegalArgumentException::class,
                        ),
                    )
                } else {
                    emptyList()
                }
            return arityAndValueCases + orderedInputCases
        }

        @JvmStatic
        fun arithmeticExceptionCases(): Stream<ArithmeticExceptionCase> =
            CONCRETE_PRE_INVALID_INPUT_FIXTURES
                .flatMap(::deriveInvalidInputCases)
                .stream()
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("arithmeticExceptionCases")
    fun `check that calculation methods throw the expected exceptions for invalid inputs`(case: ArithmeticExceptionCase) {
        Assertions.assertThrows(case.expectedException.java) {
            applyTransformation(
                case.inputs,
                case.fixture.targetType,
                case.fixture.calculationMethod,
                case.fixture.specs,
                sourceFrameworksByType,
            )
        }
    }

    // region Subtraction

    @Test
    fun `check that subtraction of data points works as expected`() {
        val result =
            defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(
                applyTransformation(
                    listOf(numericInput(NUMERIC_DATA_POINT_ONE), numericInput(NUMERIC_DATA_POINT_HALF)),
                    "dummy",
                    SUBTRACTION,
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
                    SUBTRACTION,
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
                SUBTRACTION,
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
                SUBTRACTION,
                currencySpecs,
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
                applyTransformation(listOf(input), "dummy", COMPLEMENT_TO_PERCENT, dummySpecs, sourceFrameworksByType).dataPoint,
            )
        assertBigDecimalEquals("70", result.value)
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
                applyTransformation(listOf(value, percent), "dummy", MULTIPLICATION_BY_PERCENT, dummySpecs, sourceFrameworksByType)
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
                    MULTIPLICATION_BY_PERCENT,
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
                MULTIPLICATION_BY_PERCENT,
                currencySpecs,
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
                    MULTIPLICATION_BY_COMPLEMENT_PERCENT,
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
                    MULTIPLICATION_BY_COMPLEMENT_PERCENT,
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
                MULTIPLICATION_BY_COMPLEMENT_PERCENT,
                currencySpecs,
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
                    MULTIPLICATION_BY_PERCENT_MINUS_CURRENCY,
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
                MULTIPLICATION_BY_PERCENT_MINUS_CURRENCY,
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
                MULTIPLICATION_BY_PERCENT_MINUS_CURRENCY,
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
