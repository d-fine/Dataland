package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.services.datapoints.applyTransformation
import org.dataland.datalandbackend.services.datapoints.mergeComments
import org.dataland.datalandbackend.services.datapoints.mergeQuality
import org.dataland.datalandbackend.services.datapoints.sumOfExtendedDataPoints
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
                Arguments.of(listOf("", ""), "This data point was calculated as the sum of: dummy, dummy"),
                //Arguments.of(listOf(""), null),
                //Arguments.of(listOf("First", "", "Last"), "First, Last"),
            )


    }

    @Test
    fun `check that summation of data points works as expected`() {
        val firstInput = createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint))
        val firstDataPoint = TestResourceFileReader.getKotlinObject<ExtendedDataPoint<BigDecimal>>(numericDataPoint)
        val secondInput = createUploadedDataPoint(TestResourceFileReader.getJsonString(numericDataPoint))
        val inputs = listOf(firstInput, secondInput)
        val result = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(applyTransformation(inputs, "dummy", "Sum").dataPoint)
        assert(result.value == BigDecimal.valueOf(1.0))
        assert(result.dataSource?.fileReference == firstDataPoint.dataSource?.fileReference)
        assert(result.dataSource?.fileName == firstDataPoint.dataSource?.fileName)
        assert(result.dataSource?.page == firstDataPoint.dataSource?.page)
        assert(result.dataSource?.publicationDate == firstDataPoint.dataSource?.publicationDate)
    }

    @Test
    fun `check that summation of data points throws the expected exceptions`() {
        assertThrows<InvalidFormatException> { sumOfExtendedDataPoints(listOf(createUploadedDataPoint(TestResourceFileReader.getJsonString(nonNumericDataPoint))), "dummy") }
        assertThrows<IllegalArgumentException> {
            sumOfExtendedDataPoints(listOf(createUploadedDataPoint(TestResourceFileReader.getJsonString(dataPointWithoutValue))), "dummy")
        }
    }

    @ParameterizedTest
    @MethodSource("provideComments")
    fun `check that merging of comments works as expected`(
        inputs: List<String>,
        expected: String?,
    ) {
        val uploadedDatePoints = inputs.map { createUploadedDataPoint(it) }
        assert(mergeComments(uploadedDatePoints) == expected)
    }

    @ParameterizedTest
    @MethodSource("provideQualityOptions")
    fun `check that merging of quality options works as expected`(
        inputs: List<QualityOptions?>,
        expected: QualityOptions?,
    ) {
        assert(mergeQuality(inputs) == expected)
    }

    fun createUploadedDataPoint(dataPoint: String): UploadedDataPoint {
        return UploadedDataPoint(
            dataPoint = dataPoint,
            companyId = "dummy",
            reportingPeriod = "dummy",
            dataPointType = "dummy"
        )
    }
}
