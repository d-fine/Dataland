package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.services.DataPointType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import java.math.BigDecimal

typealias DataPointContent = String

/**
 * Extendable class for implementing the various ways of deriving a data point from other data points
 */
sealed class DataPointConversions {
    /**
     * Abstract transformation function to cover all possible transformation rules
     */
    abstract fun convertDataPoints(inputs: Collection<UploadedDataPoint>, targetType: DataPointType): UploadedDataPoint

    object Sum : DataPointConversions() {
        override fun convertDataPoints(inputs: Collection<UploadedDataPoint>, targetType: DataPointType) = sumOfExtendedDataPoints(inputs, targetType)
    }
}

/**
 * Function to create a new data point of type ExtendedDataPoint<BigDecimal> given a list of existing data points in their string
 * representation. The values of the existing data points is summed up.
 * @param inputs the list of data points to be used as basis
 * @return the string representation of the combined new data point
 * @throws IllegalArgumentException if any of the input data points have a value of null
 * @throws InvalidFormatException if the casting into ExtendedDataPoint<BigDecimal> for any of the input data points fails
 */
fun sumOfExtendedDataPoints(inputs: Collection<UploadedDataPoint>, targetType: DataPointType): UploadedDataPoint {

    val dataPoints = inputs.map { defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(it.dataPoint) }
    if (dataPoints.any { it.value == null }) throw IllegalArgumentException("Data points for summation must not have null value fields.")
    val sum = dataPoints.sumOf { it.value as BigDecimal }
    val resultingDataPoint =
        ExtendedDataPoint(
            value = sum,
            quality = mergeQuality(dataPoints.map { it.quality }),
            comment = mergeComments(inputs),
            dataSource = mergeDataSources(dataPoints.mapNotNull { it.dataSource }),
        )
    return UploadedDataPoint(dataPoint = defaultObjectMapper.writeValueAsString(resultingDataPoint),
        reportingPeriod = inputs.first().reportingPeriod,
        companyId = inputs.first().companyId,
        dataPointType = targetType,
        )
}

/**
 * Merges the comments passed to the function into a single string (null values are ignored)
 */
fun mergeComments(inputs: Collection<UploadedDataPoint>): String {
    return "This data point was calculated as the sum of: " + inputs.joinToString(", ") { it.dataPointType }
}

/**
 * Merges the given [QualityOptions] into a single entry. Uses the lowes quality from any of the given options.
 */
fun mergeQuality(inputs: Collection<QualityOptions?>): QualityOptions? {
    val qualityOrder =
        listOf(
            QualityOptions.Audited, QualityOptions.Reported, QualityOptions.Estimated, QualityOptions.Incomplete,
            QualityOptions.NoDataFound, null,
        )
    return inputs.maxByOrNull { qualityOrder.indexOf(it) }
}

/**
 * Merges the given list of [ExtendedDocumentReference] into one single entry
 */
fun mergeDataSources(inputs: Collection<ExtendedDocumentReference>): ExtendedDocumentReference {
    val usedReference = inputs.map { it.fileReference }.toSet().minOf { it }

    return ExtendedDocumentReference(
        fileReference = usedReference,
        fileName = inputs.first { it.fileReference == usedReference }.fileName,
        publicationDate = inputs.first { it.fileReference == usedReference }.publicationDate,
        page = inputs.first().page,
    )
}

/**
 * Wrapper function to transform a given [inputs] of data points according to the chosen [method]
 */
fun applyTransformation(
    inputs: Collection<UploadedDataPoint>,
    targetType: String,
    method: String,
): UploadedDataPoint =
    when (method) {
        "Sum" -> DataPointConversions.Sum.convertDataPoints(inputs, targetType)
        else -> throw IllegalArgumentException("Unsupported method: $method")
    }
