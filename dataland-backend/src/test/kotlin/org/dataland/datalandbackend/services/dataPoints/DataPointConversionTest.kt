package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
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
                Arguments.of(listOf("Test", "Test"), "Test, Test"),
                Arguments.of(listOf(""), null),
                Arguments.of(listOf("First", "", "Last"), "First, Last"),
            )
    }

    @Test
    fun `check that summation of data points works as expected`() {
        val firstInput = TestResourceFileReader.getJsonString(numericDataPoint)
        val firstDataPoint = TestResourceFileReader.getKotlinObject<ExtendedDataPoint<BigDecimal>>(numericDataPoint)
        val secondInput = TestResourceFileReader.getJsonString(anotherNumericDataPoint)
        val secondDataPoint = TestResourceFileReader.getKotlinObject<ExtendedDataPoint<BigDecimal>>(anotherNumericDataPoint)
        val inputs = listOf(firstInput, secondInput)
        val result = defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(applyTransformation(inputs, "Sum"))
        assert(result.value == BigDecimal.valueOf(2.0))
        assert(result.dataSource?.fileReference == firstDataPoint.dataSource?.fileReference)
        assert(result.dataSource?.fileName == firstDataPoint.dataSource?.fileName)
        assert(result.dataSource?.page == "${firstDataPoint.dataSource?.page}, ${secondDataPoint.dataSource?.page}")
        assert(result.dataSource?.publicationDate == firstDataPoint.dataSource?.publicationDate)
        assert(result.comment == "${firstDataPoint.comment}, ${secondDataPoint.comment}")
    }

    @Test
    fun `check that summation of data points throws the expected exceptions`() {
        assertThrows<InvalidFormatException> { sumOfExtendedDataPoints(listOf(TestResourceFileReader.getJsonString(nonNumericDataPoint))) }
        assertThrows<IllegalArgumentException> {
            sumOfExtendedDataPoints(listOf(TestResourceFileReader.getJsonString(dataPointWithoutValue)))
        }
    }

    @ParameterizedTest
    @MethodSource("provideComments")
    fun `check that merging of comments works as expected`(
        inputs: List<String>,
        expected: String?,
    ) {
        assert(mergeComments(inputs) == expected)
    }

    @ParameterizedTest
    @MethodSource("provideQualityOptions")
    fun `check that merging of quality options works as expected`(
        inputs: List<QualityOptions?>,
        expected: QualityOptions?,
    ) {
        assert(mergeQuality(inputs) == expected)
    }
}
