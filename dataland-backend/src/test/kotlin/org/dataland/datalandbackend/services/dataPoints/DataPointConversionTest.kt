package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.services.datapoints.DataPointConversion
import org.dataland.datalandbackend.services.datapoints.applyTransformation
import org.dataland.datalandbackend.services.datapoints.createComment
import org.dataland.datalandbackend.services.datapoints.mergeQuality
import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.util.stream.Stream

class DataPointConversionTest {
    private val numericDataPoint = "./json/dataPoints/numericDataPointWithExtendedDocumentReference.json"
    private val nonNumericDataPoint = "./json/dataPoints/nonNumericDataPoint.json"
    private val anotherNumericDataPoint = "./json/dataPoints/anotherNumericDataPointForTestingTransformations.json"
    private val dataPointWithoutValue = "./json/dataPoints/dataPointWithoutValue.json"

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
        fun provideComments(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    listOf("Input1", "Input2"), "DummyMethod",
                    "This data point was calculated applying the method \"DummyMethod\" using: Input1, Input2 as input.",
                ),
                // ToDo add further test cases as more methods become available
            )
    }

    @Test
    fun `check that summation of data points works as expected`() {
        val firstInput = createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint))
        val firstDataPoint = TestResourceFileReader.getKotlinObject<ExtendedDataPoint<BigDecimal>>(numericDataPoint)
        val secondInput = createUploadedDataPoint(TestResourceFileReader.getJsonString(anotherNumericDataPoint))
        val inputs = listOf(firstInput, secondInput)
        val result = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(applyTransformation(inputs, "dummy", "Sum").dataPoint)
        assert(result.value == BigDecimal.valueOf(2.0))
        assert(result.dataSource?.fileReference == firstDataPoint.dataSource?.fileReference)
        assert(result.dataSource?.fileName == firstDataPoint.dataSource?.fileName)
        assert(result.dataSource?.page == firstDataPoint.dataSource?.page)
        assert(result.dataSource?.publicationDate == firstDataPoint.dataSource?.publicationDate)
    }

    @Test
    fun `check that summation of data points throws the expected exceptions`() {
        assertThrows<InvalidFormatException> {
            DataPointConversion.SUM.convert(
                listOf(createUploadedDataPoint(TestResourceFileReader.getJsonString(nonNumericDataPoint))),
                "dummy",
            )
        }
        assertThrows<IllegalArgumentException> {
            DataPointConversion.SUM.convert(
                listOf(createUploadedDataPoint(TestResourceFileReader.getJsonString(dataPointWithoutValue))),
                "dummy",
            )
        }
    }

    @Test
    fun `check that an unknown conversion method is rejected`() {
        assertThrows<IllegalArgumentException> {
            applyTransformation(emptyList(), "dummy", "NotARealMethod")
        }
    }

    @ParameterizedTest
    @MethodSource("provideComments")
    fun `check that creation of comments works as expected`(
        inputs: List<String>,
        method: String,
        expected: String,
    ) {
        assert(createComment(inputs, method) == expected)
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
