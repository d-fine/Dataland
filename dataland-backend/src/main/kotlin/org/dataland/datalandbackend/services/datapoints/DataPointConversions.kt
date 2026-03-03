package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
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
    abstract fun convertDataPoints(inputs: List<DataPointContent>): DataPointContent

    object Sum : DataPointConversions() {
        override fun convertDataPoints(inputs: List<DataPointContent>) = sumOfExtendedDataPoints(inputs)
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
fun sumOfExtendedDataPoints(inputs: List<DataPointContent>): DataPointContent {
    val dataPoints = inputs.map { defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(it) }
    if (dataPoints.any { it.value == null }) throw IllegalArgumentException("Data points for summation must not have null value fields.")
    val sum = dataPoints.sumOf { it.value as BigDecimal }
    val resultingDataPoint =
        ExtendedDataPoint(
            value = sum,
            quality = mergeQuality(dataPoints.map { it.quality }),
            comment = mergeComments(dataPoints.mapNotNull { it.comment }),
            dataSource = mergeDataSources(dataPoints.mapNotNull { it.dataSource }),
        )
    return defaultObjectMapper.writeValueAsString(resultingDataPoint)
}

/**
 * Merges the comments passed to the function into a single string (null values are ignored)
 */
fun mergeComments(inputs: List<String?>): String? {
    val comments = inputs.mapNotNull { it }.filter { !it.isBlank() }
    return if (comments.isEmpty()) {
        null
    } else {
        comments.joinToString(", ")
    }
}

/**
 * Merges the given [QualityOptions] into a single entry. Uses the lowes quality from any of the given options.
 */
fun mergeQuality(inputs: List<QualityOptions?>): QualityOptions? {
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
fun mergeDataSources(inputs: List<ExtendedDocumentReference>): ExtendedDocumentReference {
    val usedReference = inputs.map { it.fileReference }.toSet().minOf { it }

    return ExtendedDocumentReference(
        fileReference = usedReference,
        fileName = inputs.first { it.fileReference == usedReference }.fileName,
        publicationDate = inputs.first { it.fileReference == usedReference }.publicationDate,
        page = inputs.filter { it.fileReference == usedReference }.mapNotNull { it.page }.joinToString { it },
    )
}

/**
 * Wrapper function to transform a given [inputs] of data points according to the chosen [method]
 */
fun applyTransformation(
    inputs: List<DataPointContent>,
    method: String,
): DataPointContent =
    when (method) {
        "Sum" -> DataPointConversions.Sum.convertDataPoints(inputs)
        else -> throw IllegalArgumentException("Unsupported method: $method")
    }
