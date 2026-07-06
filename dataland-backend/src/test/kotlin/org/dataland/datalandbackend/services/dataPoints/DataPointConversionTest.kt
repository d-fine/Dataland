package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.services.datapoints.DataPointConversion
import org.dataland.datalandbackend.services.datapoints.applyTransformation
import org.dataland.datalandbackend.services.datapoints.mergeDataSources
import org.dataland.datalandbackend.services.datapoints.mergeQuality
import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.time.LocalDate
import java.util.stream.Stream

/**
 * Creates a minimal framework specification fixture for source framework comment rendering tests.
 *
 * @param frameworkId framework identifier to set on the fixture
 * @param frameworkName framework display name to set on the fixture
 * @return a framework specification fixture
 */
private fun createFrameworkSpecification(
    frameworkId: String,
    frameworkName: String,
): FrameworkSpecification =
    FrameworkSpecification(
        framework = IdWithRef(id = frameworkId, ref = "dummy"),
        name = frameworkName,
        businessDefinition = "dummy",
        schema = "{}",
        referencedReportJsonPath = null,
    )

class DataPointConversionTest {
    private val dummyRef = IdWithRef(id = "dummy", ref = "dummy")
    private val currencyRef = IdWithRef(id = "extendedCurrency", ref = "dummy")
    private val currencyTargetType = "currencyTargetType"
    private val sourceFrameworkName = "Test Framework"
    private val sourceFrameworksByType =
        mapOf("dummy" to listOf(createFrameworkSpecification("test-framework", sourceFrameworkName)))
    private val dummySpecs =
        mapOf(
            "dummy" to
                DataPointTypeSpecification(
                    dataPointType = dummyRef,
                    name = "dummy",
                    businessDefinition = "dummy",
                    dataPointBaseType = dummyRef,
                    usedBy = emptyList(),
                    calculationRules = emptyList(),
                ),
        )
    private val currencySpecs =
        dummySpecs +
            (
                currencyTargetType to
                    DataPointTypeSpecification(
                        dataPointType = IdWithRef(id = currencyTargetType, ref = "dummy"),
                        name = "currency target",
                        businessDefinition = "dummy",
                        dataPointBaseType = currencyRef,
                        usedBy = emptyList(),
                        calculationRules = emptyList(),
                    )
            )

    private fun assertBigDecimalEquals(
        expectedValue: String,
        actualValue: BigDecimal?,
    ) {
        assertEquals(0, BigDecimal(expectedValue).compareTo(actualValue))
    }

    private fun createUploadedDataPoint(dataPoint: String): UploadedDataPoint =
        UploadedDataPoint(
            dataPoint = dataPoint,
            companyId = "dummy",
            reportingPeriod = "dummy",
            dataPointType = "dummy",
        )

    companion object {
        const val ORIGINAL_COMMENT = "Original source comment"
        const val SOURCE_FRAMEWORK_NAME = "Test Framework"
        const val SOURCE_FRAMEWORK_ID = "source-framework"
        const val DATA_POINT_WAS_MAPPED_COMMENT = "This data point was mapped from the following source: [1]\n\n***\n\n"
        const val INPUT_HEADER_COMMENT = "[1] Input1\n"

        @JvmStatic
        fun provideQualityOptions(): Stream<Arguments> =
            Stream.of(
                Arguments.of(listOf(QualityOptions.Reported), QualityOptions.Reported),
                Arguments.of(listOf(null), null),
                Arguments.of(listOf(QualityOptions.Incomplete, QualityOptions.Reported), QualityOptions.Incomplete),
                Arguments.of(listOf(null, QualityOptions.Reported), null),
                Arguments
                    .of(
                        listOf(QualityOptions.Audited, QualityOptions.Reported, QualityOptions.NoDataFound),
                        QualityOptions.NoDataFound,
                    ),
            )

        @JvmStatic
        fun provideDataSources(): Stream<Arguments> {
            val smallerReference =
                ExtendedDocumentReference(
                    fileReference = "50abc",
                    fileName = "fileName1",
                    page = "1",
                    publicationDate = LocalDate.of(2024, 1, 1),
                )
            val largerReference =
                ExtendedDocumentReference(
                    fileReference = "88bed",
                    fileName = "fileName2",
                    page = "5",
                    publicationDate = LocalDate.of(2025, 6, 1),
                )
            val expectedMerged =
                ExtendedDocumentReference(
                    fileReference = "50abc",
                    fileName = "fileName1",
                    page = "1",
                    publicationDate = LocalDate.of(2024, 1, 1),
                )
            return Stream.of(
                Arguments.of(listOf(smallerReference, largerReference), expectedMerged),
                Arguments.of(listOf(largerReference, smallerReference), expectedMerged),
                Arguments.of(emptyList<ExtendedDocumentReference>(), null),
                Arguments.of(listOf(smallerReference), expectedMerged),
            )
        }

        @JvmStatic
        fun provideComments(): Stream<Arguments> {
            val input1 = createDummyUploadedDataPoint("type1")
            val input2 = createDummyUploadedDataPoint("type2")
            val specs = createCommentSpecs()
            val firstDataPoint =
                ExtendedDataPoint(
                    value = BigDecimal.ONE,
                    quality = QualityOptions.Incomplete,
                    comment = ORIGINAL_COMMENT,
                )
            val secondDataPoint = ExtendedDataPoint(value = BigDecimal.TEN, quality = QualityOptions.Reported)
            return Stream.of(
                Arguments.of(
                    listOf(input1, input2),
                    specs,
                    listOf(firstDataPoint, secondDataPoint),
                    DataPointConversion.SUM,
                    calculationComment(
                        "[1] + [2]",
                        sourceBlock(1, "Input1", ORIGINAL_COMMENT),
                        sourceBlock(2, "Input2"),
                    ),
                ),
                Arguments.of(
                    listOf(input1, input2),
                    specs,
                    listOf(firstDataPoint, secondDataPoint),
                    DataPointConversion.DIVISION,
                    calculationComment(
                        "[1] / [2]",
                        sourceBlock(1, "Input1", ORIGINAL_COMMENT),
                        sourceBlock(2, "Input2"),
                    ),
                ),
                Arguments.of(
                    listOf(input1, input2),
                    specs,
                    listOf(firstDataPoint, secondDataPoint),
                    DataPointConversion.DIVISION_BY_PERCENT,
                    calculationComment(
                        "100 * [1] / [2]",
                        sourceBlock(1, "Input1", ORIGINAL_COMMENT),
                        sourceBlock(2, "Input2"),
                    ),
                ),
                Arguments.of(
                    listOf(input1),
                    specs,
                    listOf(firstDataPoint),
                    DataPointConversion.IDENTITY,
                    identityComment(sourceBlock(1, "Input1", ORIGINAL_COMMENT)),
                ),
            )
        }

        @JvmStatic
        fun provideSourceCommentQualityOptions(): Stream<Arguments> =
            Stream.of(
                Arguments.of(QualityOptions.Estimated, "Estimated source comment"),
                Arguments.of(QualityOptions.Incomplete, "Incomplete source comment"),
                Arguments.of(QualityOptions.NoDataFound, "NoDataFound source comment"),
                Arguments.of(null, "Null quality source comment"),
            )

        @JvmStatic
        fun provideNumericTransformationResults(): Stream<Arguments> =
            Stream.of(
                Arguments.of(listOf(NUMERIC_DATA_POINT_HALF, NUMERIC_DATA_POINT_ONE), "Sum", "1.5"),
                Arguments.of(
                    listOf(NUMERIC_DATA_POINT_HALF, NUMERIC_DATA_POINT_ONE),
                    "Division",
                    "0.5",
                ),
                Arguments.of(
                    listOf(NUMERIC_DATA_POINT_HALF, NUMERIC_DATA_POINT_ONE),
                    "DivisionByPercent",
                    "50.0",
                ),
            )

        private const val NUMERIC_DATA_POINT_HALF = "json/dataPoints/numericDataPointHalf.json"
        private const val NON_NUMERIC_DATA_POINT = "json/dataPoints/nonNumericDataPoint.json"
        private const val NUMERIC_DATA_POINT_ONE = "json/dataPoints/numericDataPointOne.json"
        private const val DATA_POINT_WITHOUT_VALUE = "json/dataPoints/dataPointWithoutValue.json"
        private const val NUMERICA_DATA_POINT_ZERO = "json/dataPoints/numericDataPointZero.json"

        private fun createDummyUploadedDataPoint(dataPointType: String) =
            UploadedDataPoint(
                dataPoint = "dummy",
                companyId = "dummy",
                reportingPeriod = "dummy",
                dataPointType = dataPointType,
            )

        private fun createCommentSpecs(): Map<DataPointType, DataPointTypeSpecification> {
            val dummyRef = IdWithRef(id = "dummy", ref = "dummy")
            return mapOf(
                "type1" to createDummySpec(dummyRef, "Input1"),
                "type2" to createDummySpec(dummyRef, "Input2"),
                "type3" to createDummySpec(dummyRef, "Input3"),
            )
        }

        /**
         * Creates source framework fixtures for the data point types used by the comment test cases.
         *
         * @return source framework specifications grouped by source data point type
         */
        private fun createCommentSourceFrameworksByType(): Map<DataPointType, List<FrameworkSpecification>> {
            val sourceFramework = createFrameworkSpecification(SOURCE_FRAMEWORK_ID, SOURCE_FRAMEWORK_NAME)
            return mapOf(
                "type1" to listOf(sourceFramework),
                "type2" to listOf(sourceFramework),
                "type3" to listOf(sourceFramework),
            )
        }

        private fun createDummySpec(
            dummyRef: IdWithRef,
            name: String,
        ): DataPointTypeSpecification =
            DataPointTypeSpecification(
                dataPointType = dummyRef,
                name = name,
                businessDefinition = "dummy",
                dataPointBaseType = dummyRef,
                usedBy = emptyList(),
                calculationRules = emptyList(),
            )

        private fun calculationComment(
            formula: String,
            vararg sourceBlocks: String,
        ): String =
            "This data point was calculated using the following formula: $formula\n\n***\n\n" +
                sourcesSection(*sourceBlocks)

        private fun identityComment(vararg sourceBlocks: String): String =
            DATA_POINT_WAS_MAPPED_COMMENT +
                sourcesSection(*sourceBlocks)

        private fun sourcesSection(vararg sourceBlocks: String): String = sourceBlocks.joinToString(separator = "\n\n")

        private fun sourceBlock(
            index: Int,
            sourceName: String,
            sourceComment: String? = null,
            frameworkName: String = SOURCE_FRAMEWORK_NAME,
        ): String =
            "[$index] $sourceName\n" +
                "+ Framework: $frameworkName" +
                (sourceComment?.let { "\n+ Comment: $it" } ?: "")
    }

    @ParameterizedTest
    @MethodSource("provideNumericTransformationResults")
    fun `check that numeric transformations work as expected`(
        inputFixturePaths: List<String>,
        conversionMethod: String,
        expectedValue: String,
    ) {
        val firstDataPoint =
            TestResourceFileReader.getKotlinObject<ExtendedDataPoint<BigDecimal>>(inputFixturePaths.first())
        val inputs =
            inputFixturePaths.map { fixturePath ->
                createUploadedDataPoint(TestResourceFileReader.getJsonString(fixturePath))
            }
        val result =
            defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(
                applyTransformation(inputs, "dummy", conversionMethod, dummySpecs, sourceFrameworksByType).dataPoint,
            )

        assertBigDecimalEquals(expectedValue, result.value)
        assertEquals(firstDataPoint.dataSource, result.dataSource)
    }

    @Test
    fun `check that summation of currency data points preserves the currency`() {
        val firstInput = createUploadedDataPoint(createCurrencyDataPoint("0.5", "EUR"))
        val secondInput = createUploadedDataPoint(createCurrencyDataPoint("1.0", "EUR"))
        val result =
            defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(
                applyTransformation(
                    listOf(firstInput, secondInput),
                    currencyTargetType,
                    "Sum",
                    currencySpecs,
                    sourceFrameworksByType,
                ).dataPoint,
            )
        assertBigDecimalEquals("1.5", result.value)
        assertEquals("EUR", result.currency)
    }

    @Test
    fun `check that summation of currency data points rejects mixed currencies`() {
        val firstInput = createUploadedDataPoint(createCurrencyDataPoint("0.5", "EUR"))
        val secondInput = createUploadedDataPoint(createCurrencyDataPoint("1.0", "USD"))
        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(firstInput, secondInput),
                currencyTargetType,
                "Sum",
                currencySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that summation of currency data points rejects missing currencies`() {
        val firstInput = createUploadedDataPoint(createCurrencyDataPoint("0.5", null))
        val secondInput = createUploadedDataPoint(createCurrencyDataPoint("1.0", "EUR"))

        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(firstInput, secondInput),
                currencyTargetType,
                "Sum",
                currencySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that summation of data points throws the expected exceptions`() {
        assertThrows<IllegalArgumentException> {
            DataPointConversion.SUM.convert(
                listOf(createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF))),
                "dummy",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        assertThrows<JsonProcessingException> {
            DataPointConversion.SUM.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NON_NUMERIC_DATA_POINT)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF)),
                ),
                "dummy",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.SUM.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(DATA_POINT_WITHOUT_VALUE)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF)),
                ),
                "dummy",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that division of data points throws the expected exceptions`() {
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION.convert(
                listOf(createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF))),
                "dummy",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF)),
                ),
                "dummy",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(DATA_POINT_WITHOUT_VALUE)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF)),
                ),
                "dummy",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERICA_DATA_POINT_ZERO)),
                ),
                "dummy",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that division of currency data points preserves the numerator currency`() {
        val numerator = createUploadedDataPoint(createCurrencyDataPoint("0.5", "EUR"))
        val denominator = createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_ONE))
        val result =
            defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(
                applyTransformation(
                    listOf(numerator, denominator),
                    currencyTargetType,
                    "Division",
                    currencySpecs,
                    sourceFrameworksByType,
                ).dataPoint,
            )

        assertBigDecimalEquals("0.5", result.value)
        assertEquals("EUR", result.currency)
    }

    @Test
    fun `check that division of currency data points rejects missing numerator currency`() {
        val numerator = createUploadedDataPoint(createCurrencyDataPoint("0.5", null))
        val denominator = createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_ONE))

        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(numerator, denominator),
                currencyTargetType,
                "Division",
                currencySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that division by percent of currency data points preserves the numerator currency`() {
        val numerator = createUploadedDataPoint(createCurrencyDataPoint("0.5", "EUR"))
        val denominator = createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_ONE))
        val result =
            defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(
                applyTransformation(
                    listOf(numerator, denominator),
                    currencyTargetType,
                    "DivisionByPercent",
                    currencySpecs,
                    sourceFrameworksByType,
                ).dataPoint,
            )
        assertBigDecimalEquals("50", result.value)
        assertEquals("EUR", result.currency)
    }

    @Test
    fun `check that division by percent of currency data points rejects missing numerator currency`() {
        val numerator = createUploadedDataPoint(createCurrencyDataPoint("0.5", null))
        val denominator = createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_ONE))

        assertThrows<IllegalArgumentException> {
            applyTransformation(
                listOf(numerator, denominator),
                currencyTargetType,
                "DivisionByPercent",
                currencySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that division by percent of data points throws the expected exceptions`() {
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION_BY_PERCENT.convert(
                listOf(createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF))),
                "dummy",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION_BY_PERCENT.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF)),
                ),
                "dummy",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION_BY_PERCENT.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(DATA_POINT_WITHOUT_VALUE)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF)),
                ),
                "dummy",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION_BY_PERCENT.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERICA_DATA_POINT_ZERO)),
                ),
                "dummy",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that identity conversion works as expected`() {
        val inputDataPoint =
            defaultObjectMapper
                .readValue<ExtendedDataPoint<BigDecimal>>(
                    TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF),
                ).copy(comment = ORIGINAL_COMMENT)
        val input = createUploadedDataPoint(defaultObjectMapper.writeValueAsString(inputDataPoint))
        val result = applyTransformation(listOf(input), "targetType", "Identity", dummySpecs, sourceFrameworksByType)
        val resultDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(result.dataPoint)
        assert(resultDataPoint.value == inputDataPoint.value)
        assertEquals(
            DATA_POINT_WAS_MAPPED_COMMENT +
                "[1] dummy\n" +
                "+ Framework: $sourceFrameworkName\n" +
                "+ Comment: $ORIGINAL_COMMENT",
            resultDataPoint.comment,
        )
        assert(resultDataPoint.dataSource == inputDataPoint.dataSource)
        assert(result.dataPointType == "targetType")
        assert(result.companyId == input.companyId)
        assert(result.reportingPeriod == input.reportingPeriod)
    }

    @Test
    fun `check that identity conversion of currency data points preserves the currency`() {
        val input = createUploadedDataPoint(createCurrencyDataPoint("0.5", "EUR", "Original currency comment"))
        val result =
            applyTransformation(listOf(input), currencyTargetType, "Identity", currencySpecs, sourceFrameworksByType)
        val resultDataPoint = defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(result.dataPoint)
        assertBigDecimalEquals("0.5", resultDataPoint.value)
        assertEquals("EUR", resultDataPoint.currency)
        assertEquals(
            DATA_POINT_WAS_MAPPED_COMMENT +
                "[1] dummy\n" +
                "+ Framework: $sourceFrameworkName",
            resultDataPoint.comment,
        )
        assert(result.dataPointType == currencyTargetType)
        assert(result.companyId == input.companyId)
        assert(result.reportingPeriod == input.reportingPeriod)
    }

    @Test
    fun `check that identity conversion throws the expected exceptions`() {
        assertThrows<IllegalArgumentException> {
            DataPointConversion.IDENTITY.convert(emptyList(), "dummy", dummySpecs, sourceFrameworksByType)
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.IDENTITY.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(NUMERIC_DATA_POINT_HALF)),
                ),
                "dummy",
                dummySpecs,
                sourceFrameworksByType,
            )
        }
    }

    @Test
    fun `check that an unknown conversion method is rejected`() {
        assertThrows<IllegalArgumentException> {
            applyTransformation(emptyList(), "dummy", "NotARealMethod", dummySpecs, sourceFrameworksByType)
        }
    }

    @ParameterizedTest
    @MethodSource("provideComments")
    fun `check that creation of comments works as expected`(
        inputs: Collection<UploadedDataPoint>,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        dataPoints: Collection<ExtendedDataPoint<*>>,
        conversion: DataPointConversion,
        expected: String,
    ) {
        assertEquals(
            expected,
            conversion.createComment(inputs, specs, dataPoints, createCommentSourceFrameworksByType()),
        )
    }

    @Test
    fun `check that source section uses the framework name for each source type`() {
        val input1 = createDummyUploadedDataPoint("type1")
        val input2 = createDummyUploadedDataPoint("type2")
        val dataPoints =
            listOf(
                ExtendedDataPoint(value = BigDecimal.ONE, quality = QualityOptions.Reported),
                ExtendedDataPoint(value = BigDecimal.TEN, quality = QualityOptions.Reported),
            )
        val sourceFrameworksByType =
            mapOf(
                "type1" to listOf(createFrameworkSpecification("first-framework", "First Framework")),
                "type2" to listOf(createFrameworkSpecification("second-framework", "Second Framework")),
            )

        val comment =
            DataPointConversion.SUM.createComment(
                listOf(input1, input2),
                createCommentSpecs(),
                dataPoints,
                sourceFrameworksByType,
            )

        assertEquals(
            calculationComment(
                "[1] + [2]",
                sourceBlock(1, "Input1", frameworkName = "First Framework"),
                sourceBlock(2, "Input2", frameworkName = "Second Framework"),
            ),
            comment,
        )
    }

    @Test
    fun `check that unknown framework is used when source type has no framework name`() {
        val input = createDummyUploadedDataPoint("type1")
        val dataPoint = ExtendedDataPoint(value = BigDecimal.ONE, quality = QualityOptions.Reported)
        val comment =
            DataPointConversion.IDENTITY.createComment(
                listOf(input),
                createCommentSpecs(),
                listOf(dataPoint),
                emptyMap(),
            )

        assertEquals(
            identityComment(sourceBlock(1, "Input1", frameworkName = "Unknown")),
            comment,
        )
    }

    @ParameterizedTest
    @MethodSource("provideSourceCommentQualityOptions")
    fun `check that source comments are appended for non reported and non audited quality options`(
        quality: QualityOptions?,
        sourceComment: String,
    ) {
        val input = createDummyUploadedDataPoint("type1")
        val dataPoint = ExtendedDataPoint(value = BigDecimal.ONE, quality = quality, comment = sourceComment)
        val comment =
            DataPointConversion.IDENTITY.createComment(
                listOf(input),
                createCommentSpecs(),
                listOf(dataPoint),
                mapOf("type1" to listOf(createFrameworkSpecification(SOURCE_FRAMEWORK_ID, sourceFrameworkName))),
            )

        assertEquals(
            DATA_POINT_WAS_MAPPED_COMMENT +
                INPUT_HEADER_COMMENT +
                "+ Framework: $sourceFrameworkName\n" +
                "+ Comment: $sourceComment",
            comment,
        )
    }

    @ParameterizedTest
    @EnumSource(value = QualityOptions::class, names = ["Reported", "Audited"])
    fun `check that source comments are omitted for reported and audited data points`(quality: QualityOptions) {
        val input = createDummyUploadedDataPoint("type1")
        val dataPoint =
            ExtendedDataPoint(value = BigDecimal.ONE, quality = quality, comment = "$quality source comment")
        val comment =
            DataPointConversion.IDENTITY.createComment(
                listOf(input),
                createCommentSpecs(),
                listOf(dataPoint),
                mapOf("type1" to listOf(createFrameworkSpecification(SOURCE_FRAMEWORK_ID, sourceFrameworkName))),
            )

        assertEquals(
            DATA_POINT_WAS_MAPPED_COMMENT +
                INPUT_HEADER_COMMENT +
                "+ Framework: $sourceFrameworkName",
            comment,
        )
    }

    @Test
    fun `check that source comment placeholder is used for non reported data points without comments`() {
        val input = createDummyUploadedDataPoint("type1")
        val dataPoint = ExtendedDataPoint(value = BigDecimal.ONE, quality = QualityOptions.Estimated, comment = " ")
        val comment =
            DataPointConversion.IDENTITY.createComment(
                listOf(input),
                createCommentSpecs(),
                listOf(dataPoint),
                mapOf("type1" to listOf(createFrameworkSpecification(SOURCE_FRAMEWORK_ID, sourceFrameworkName))),
            )

        assertEquals(
            DATA_POINT_WAS_MAPPED_COMMENT +
                INPUT_HEADER_COMMENT +
                "+ Framework: $sourceFrameworkName\n" +
                "+ Comment: none",
            comment,
        )
    }

    @ParameterizedTest
    @MethodSource("provideQualityOptions")
    fun `check that merging of quality options works as expected`(
        inputs: List<QualityOptions?>,
        expected: QualityOptions?,
    ) {
        assert(mergeQuality(inputs) == expected)
    }

    @ParameterizedTest
    @MethodSource("provideDataSources")
    fun `check that merging of data sources works as expected`(
        inputs: List<ExtendedDocumentReference>,
        expected: ExtendedDocumentReference?,
    ) {
        assert(mergeDataSources(inputs) == expected)
    }

    private fun createCurrencyDataPoint(
        value: String,
        currency: String?,
        comment: String? = null,
    ): String =
        defaultObjectMapper.writeValueAsString(
            ExtendedCurrencyDataPoint(
                value = BigDecimal(value),
                currency = currency,
                quality = QualityOptions.Reported,
                comment = comment,
            ),
        )
}
