package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.services.DataPointType
import org.dataland.datalandbackend.services.datapoints.DataPointConversion
import org.dataland.datalandbackend.services.datapoints.applyTransformation
import org.dataland.datalandbackend.services.datapoints.createComment
import org.dataland.datalandbackend.services.datapoints.mergeDataSources
import org.dataland.datalandbackend.services.datapoints.mergeQuality
import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.time.LocalDate
import java.util.stream.Stream

class DataPointConversionTest {
    private val dummyRef = IdWithRef(id = "dummy", ref = "dummy")
    private val currencyRef = IdWithRef(id = "extendedCurrency", ref = "dummy")
    private val currencyTargetType = "currencyTargetType"
    private val dummySpecs =
        mapOf(
            "dummy" to
                DataPointTypeSpecification(
                    dataPointType = dummyRef,
                    name = "dummy",
                    businessDefinition = "dummy",
                    dataPointBaseType = dummyRef,
                    usedBy = emptyList(),
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
                    )
            )

    private val numericDataPointHalf = "json/dataPoints/numericDataPointHalf.json"
    private val nonNumericDataPoint = "./json/dataPoints/nonNumericDataPoint.json"
    private val numericDataPointOne = "json/dataPoints/numericDataPointOne.json"
    private val dataPointWithoutValue = "./json/dataPoints/dataPointWithoutValue.json"
    private val numericDataPointZero = "json/dataPoints/numericDataPointZero.json"

    private fun assertBigDecimalEquals(
        expectedValue: String,
        actualValue: BigDecimal?,
    ) {
        assertEquals(0, BigDecimal(expectedValue).compareTo(actualValue))
    }

    companion object {
        @JvmStatic
        fun provideQualityOptions(): Stream<Arguments> =
            Stream.of(
                Arguments.of(listOf(QualityOptions.Reported), QualityOptions.Reported),
                Arguments.of(listOf(null), null),
                Arguments.of(listOf(QualityOptions.Incomplete, QualityOptions.Reported), QualityOptions.Incomplete),
                Arguments.of(listOf(null, QualityOptions.Reported), null),
                Arguments
                    .of(listOf(QualityOptions.Audited, QualityOptions.Reported, QualityOptions.NoDataFound), QualityOptions.NoDataFound),
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
            val dummyRef = IdWithRef(id = "dummy", ref = "dummy")
            val input1 = createDummyUploadedDataPoint("type1")
            val input2 = createDummyUploadedDataPoint("type2")
            val specs =
                mapOf(
                    "type1" to
                        DataPointTypeSpecification(
                            dataPointType = dummyRef,
                            name = "Input1",
                            businessDefinition = "dummy",
                            dataPointBaseType = dummyRef,
                            usedBy = emptyList(),
                        ),
                    "type2" to
                        DataPointTypeSpecification(
                            dataPointType = dummyRef,
                            name = "Input2",
                            businessDefinition = "dummy",
                            dataPointBaseType = dummyRef,
                            usedBy = emptyList(),
                        ),
                )
            return Stream.of(
                Arguments.of(
                    listOf(input1, input2),
                    specs,
                    "DummyMethod",
                    "This data point was calculated applying the method \"DummyMethod\" using: Input1, Input2 as input.",
                ),
                Arguments.of(
                    listOf(input2, input1),
                    specs,
                    "DummyMethod",
                    "This data point was calculated applying the method \"DummyMethod\" using: Input2, Input1 as input.",
                ),
                Arguments.of(
                    listOf(input1),
                    specs,
                    "DummyMethod",
                    "This data point was calculated applying the method \"DummyMethod\" using: Input1 as input.",
                ),
                Arguments.of(
                    emptyList<UploadedDataPoint>(),
                    emptyMap<DataPointType, DataPointTypeSpecification>(),
                    "DummyMethod",
                    "This data point was calculated applying the method \"DummyMethod\" using:  as input.",
                ),
            )
        }

        @JvmStatic
        fun provideNumericTransformationResults(): Stream<Arguments> =
            Stream.of(
                Arguments.of(listOf("json/dataPoints/numericDataPointHalf.json", "json/dataPoints/numericDataPointOne.json"), "Sum", "1.5"),
                Arguments.of(
                    listOf("json/dataPoints/numericDataPointHalf.json", "json/dataPoints/numericDataPointOne.json"),
                    "Division",
                    "0.5",
                ),
                Arguments.of(
                    listOf("json/dataPoints/numericDataPointHalf.json", "json/dataPoints/numericDataPointOne.json"),
                    "DivisionByPercent",
                    "50.0",
                ),
            )

        private fun createDummyUploadedDataPoint(dataPointType: String) =
            UploadedDataPoint(
                dataPoint = "dummy",
                companyId = "dummy",
                reportingPeriod = "dummy",
                dataPointType = dataPointType,
            )
    }

    @ParameterizedTest
    @MethodSource("provideNumericTransformationResults")
    fun `check that numeric transformations work as expected`(
        inputFixturePaths: List<String>,
        conversionMethod: String,
        expectedValue: String,
    ) {
        val firstDataPoint = TestResourceFileReader.getKotlinObject<ExtendedDataPoint<BigDecimal>>(inputFixturePaths.first())
        val inputs =
            inputFixturePaths.map { fixturePath ->
                createUploadedDataPoint(TestResourceFileReader.getJsonString(fixturePath))
            }
        val result =
            defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(
                applyTransformation(inputs, "dummy", conversionMethod, dummySpecs).dataPoint,
            )

        assertBigDecimalEquals(expectedValue, result.value)
        assert(result.dataSource?.fileReference == firstDataPoint.dataSource?.fileReference)
        assert(result.dataSource?.fileName == firstDataPoint.dataSource?.fileName)
        assert(result.dataSource?.page == firstDataPoint.dataSource?.page)
        assert(result.dataSource?.publicationDate == firstDataPoint.dataSource?.publicationDate)
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
            )
        }
    }

    @Test
    fun `check that summation of data points throws the expected exceptions`() {
        assertThrows<IllegalArgumentException> {
            DataPointConversion.SUM.convert(
                listOf(createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf))),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<JsonProcessingException> {
            DataPointConversion.SUM.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(nonNumericDataPoint)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf)),
                ),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.SUM.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(dataPointWithoutValue)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf)),
                ),
                "dummy",
                dummySpecs,
            )
        }
    }

    @Test
    fun `check that division of data points throws the expected exceptions`() {
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION.convert(
                listOf(createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf))),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf)),
                ),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(dataPointWithoutValue)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf)),
                ),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointZero)),
                ),
                "dummy",
                dummySpecs,
            )
        }
    }

    @Test
    fun `check that division by percent of currency data points preserves the numerator currency`() {
        val numerator = createUploadedDataPoint(createCurrencyDataPoint("0.5", "EUR"))
        val denominator = createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointOne))
        val result =
            defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(
                applyTransformation(
                    listOf(numerator, denominator),
                    currencyTargetType,
                    "DivisionByPercent",
                    currencySpecs,
                ).dataPoint,
            )
        assertBigDecimalEquals("50.0000000000", result.value)
        assertEquals("EUR", result.currency)
    }

    @Test
    fun `check that division by percent of data points throws the expected exceptions`() {
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION_BY_PERCENT.convert(
                listOf(createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf))),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION_BY_PERCENT.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf)),
                ),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION_BY_PERCENT.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(dataPointWithoutValue)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf)),
                ),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION_BY_PERCENT.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointZero)),
                ),
                "dummy",
                dummySpecs,
            )
        }
    }

    @Test
    fun `check that identity conversion works as expected`() {
        val input = createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf))
        val result = applyTransformation(listOf(input), "targetType", "Identity", dummySpecs)
        val inputDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<Any?>>(input.dataPoint)
        val resultDataPoint = defaultObjectMapper.readValue<ExtendedDataPoint<Any?>>(result.dataPoint)
        assert(resultDataPoint.value == inputDataPoint.value)
        assert(resultDataPoint.dataSource == inputDataPoint.dataSource)
        assert(result.dataPointType == "targetType")
        assert(result.companyId == input.companyId)
        assert(result.reportingPeriod == input.reportingPeriod)
    }

    @Test
    fun `check that identity conversion of currency data points preserves the currency`() {
        val input = createUploadedDataPoint(createCurrencyDataPoint("0.5", "EUR"))
        val result = applyTransformation(listOf(input), currencyTargetType, "Identity", currencySpecs)
        val resultDataPoint = defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(result.dataPoint)
        assertBigDecimalEquals("0.5", resultDataPoint.value)
        assertEquals("EUR", resultDataPoint.currency)
        assert(result.dataPointType == currencyTargetType)
        assert(result.companyId == input.companyId)
        assert(result.reportingPeriod == input.reportingPeriod)
    }

    @Test
    fun `check that identity conversion throws the expected exceptions`() {
        assertThrows<IllegalArgumentException> {
            DataPointConversion.IDENTITY.convert(emptyList(), "dummy", dummySpecs)
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.IDENTITY.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPointHalf)),
                ),
                "dummy",
                dummySpecs,
            )
        }
    }

    @Test
    fun `check that an unknown conversion method is rejected`() {
        assertThrows<IllegalArgumentException> {
            applyTransformation(emptyList(), "dummy", "NotARealMethod", dummySpecs)
        }
    }

    @ParameterizedTest
    @MethodSource("provideComments")
    fun `check that creation of comments works as expected`(
        inputs: Collection<UploadedDataPoint>,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        method: String,
        expected: String,
    ) {
        assert(createComment(inputs, specs, method) == expected)
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

    fun createUploadedDataPoint(dataPoint: String): UploadedDataPoint =
        UploadedDataPoint(
            dataPoint = dataPoint,
            companyId = "dummy",
            reportingPeriod = "dummy",
            dataPointType = "dummy",
        )

    private fun createCurrencyDataPoint(
        value: String,
        currency: String,
    ): String =
        defaultObjectMapper.writeValueAsString(
            ExtendedCurrencyDataPoint(
                value = BigDecimal(value),
                currency = currency,
                quality = QualityOptions.Reported,
            ),
        )
}
