package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.services.DataPointType
import org.dataland.datalandbackend.services.datapoints.DataPointConversion
import org.dataland.datalandbackend.services.datapoints.applyTransformation
import org.dataland.datalandbackend.services.datapoints.createComment
import org.dataland.datalandbackend.services.datapoints.mergeQuality
import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.util.stream.Stream

class DataPointConversionTest {
    private val dummyRef = IdWithRef(id = "dummy", ref = "dummy")
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

    private val numericDataPoint = "./json/dataPoints/numericDataPointWithExtendedDocumentReference.json"
    private val nonNumericDataPoint = "./json/dataPoints/nonNumericDataPoint.json"
    private val anotherNumericDataPoint = "./json/dataPoints/anotherNumericDataPointForTestingTransformations.json"
    private val dataPointWithoutValue = "./json/dataPoints/dataPointWithoutValue.json"
    private val zeroNumericDataPoint = "./json/dataPoints/zeroNumericDataPoint.json"

    // ToDo clean up the source files for the tests
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
        fun provideComments(): Stream<Arguments> {
            val dummyRef = IdWithRef(id = "dummy", ref = "dummy")
            val input1 =
                UploadedDataPoint(
                    dataPoint = "dummy",
                    companyId = "dummy",
                    reportingPeriod = "dummy",
                    dataPointType = "type1",
                )
            val input2 =
                UploadedDataPoint(
                    dataPoint = "dummy",
                    companyId = "dummy",
                    reportingPeriod = "dummy",
                    dataPointType = "type2",
                )
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
                // ToDo add further test cases as more methods become available
            )
        }
    }

    @Test
    fun `check that summation of data points works as expected`() {
        val firstInput = createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint))
        val firstDataPoint = TestResourceFileReader.getKotlinObject<ExtendedDataPoint<BigDecimal>>(numericDataPoint)
        val secondInput = createUploadedDataPoint(TestResourceFileReader.getJsonString(anotherNumericDataPoint))
        val inputs = listOf(firstInput, secondInput)
        val result =
            defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(
                applyTransformation(
                    inputs,
                    "dummy", "Sum", dummySpecs,
                ).dataPoint,
            )
        assert(result.value == BigDecimal.valueOf(2.0))
        assert(result.dataSource?.fileReference == firstDataPoint.dataSource?.fileReference)
        assert(result.dataSource?.fileName == firstDataPoint.dataSource?.fileName)
        assert(result.dataSource?.page == firstDataPoint.dataSource?.page)
        assert(result.dataSource?.publicationDate == firstDataPoint.dataSource?.publicationDate)
    }

    @Test
    fun `check that summation of data points throws the expected exceptions`() {
        assertThrows<IllegalArgumentException> {
            DataPointConversion.SUM.convert(
                listOf(createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint))),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<JsonProcessingException> {
            DataPointConversion.SUM.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(nonNumericDataPoint)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint)),
                ),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.SUM.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(dataPointWithoutValue)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint)),
                ),
                "dummy",
                dummySpecs,
            )
        }
    }

    @Test
    fun `check that division of data points works as expected`() {
        val numerator = createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint))
        val denominator = createUploadedDataPoint(TestResourceFileReader.getJsonString(anotherNumericDataPoint))
        val result =
            defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(
                applyTransformation(listOf(numerator, denominator), "dummy", "Division", dummySpecs).dataPoint,
            )
        assert(result.value == BigDecimal("0.3333333333"))
    }

    @Test
    fun `check that division of data points throws the expected exceptions`() {
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION.convert(
                listOf(createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint))),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint)),
                ),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(dataPointWithoutValue)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint)),
                ),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(zeroNumericDataPoint)),
                ),
                "dummy",
                dummySpecs,
            )
        }
    }

    @Test
    fun `check that division by percent of data points works as expected`() {
        val numerator = createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint))
        val denominator = createUploadedDataPoint(TestResourceFileReader.getJsonString(anotherNumericDataPoint))
        val result =
            defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(
                applyTransformation(listOf(numerator, denominator), "dummy", "DivisionByPercent", dummySpecs).dataPoint,
            )
        assert(result.value == BigDecimal("33.3333333333"))
    }

    @Test
    fun `check that division by percent of data points throws the expected exceptions`() {
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION_BY_PERCENT.convert(
                listOf(createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint))),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION_BY_PERCENT.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint)),
                ),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION_BY_PERCENT.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(dataPointWithoutValue)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint)),
                ),
                "dummy",
                dummySpecs,
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.DIVISION_BY_PERCENT.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(zeroNumericDataPoint)),
                ),
                "dummy",
                dummySpecs,
            )
        }
    }

    @Test
    fun `check that identity conversion works as expected`() {
        val input = createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint))
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
    fun `check that identity conversion throws the expected exceptions`() {
        assertThrows<IllegalArgumentException> {
            DataPointConversion.IDENTITY.convert(emptyList(), "dummy", dummySpecs)
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.IDENTITY.convert(
                listOf(
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint)),
                    createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint)),
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

    fun createUploadedDataPoint(dataPoint: String): UploadedDataPoint =
        UploadedDataPoint(
            dataPoint = dataPoint,
            companyId = "dummy",
            reportingPeriod = "dummy",
            dataPointType = "dummy",
        )
}
