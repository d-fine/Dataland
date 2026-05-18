package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.services.DataPointType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import java.math.BigDecimal
import java.math.RoundingMode

// TODO: is this a sensible default?
private const val CALCULATION_SCALE = 10
private val CALCULATION_ROUNDING_MODE = RoundingMode.HALF_UP
private val ONE_HUNDRED = BigDecimal("100")

/**
 * Closed set of strategies for deriving a data point from a collection of other data points.
 * Variants are dispatched by their [id] via [byId].
 */
enum class DataPointConversion(
    val id: String,
) {
    SUM("Sum") {
        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
        ): UploadedDataPoint {
            val dataPoints = inputs.map { defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(it.dataPoint) }
            require(dataPoints.isNotEmpty()) { "At least one data point must be provided for summation." }
            require(dataPoints.none { it.value == null }) { "Data points for summation must not have null value fields." }
            val calculatedDataPoint =
                ExtendedDataPoint(
                    value = dataPoints.sumOf { it.value!! },
                    quality = mergeQuality(dataPoints.map { it.quality }),
                    comment = createComment(inputs.map { it.dataPointType }, "Sum"),
                    dataSource = mergeDataSources(dataPoints.mapNotNull { it.dataSource }),
                )
            return UploadedDataPoint(
                dataPoint = defaultObjectMapper.writeValueAsString(calculatedDataPoint),
                reportingPeriod = inputs.first().reportingPeriod,
                companyId = inputs.first().companyId,
                dataPointType = targetType,
            )
        }
    },

    DIVISION("Division") {
        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
        ): UploadedDataPoint {
            val dataPoints = inputs.map { defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(it.dataPoint) }
            require(dataPoints.none { it.value == null }) { "Data points for division must not have null value fields." }
            require(dataPoints.size == 2) { "Exactly two data points must be provided for division." }
            require(dataPoints[1].value!!.signum() != 0) { "The divisor in division must not be zero." }
            val calculatedDataPoint =
                ExtendedDataPoint(
                    value =
                        dataPoints[0].value!!.divide(
                            dataPoints[1].value!!,
                            CALCULATION_SCALE,
                            CALCULATION_ROUNDING_MODE,
                        ),
                    quality = mergeQuality(dataPoints.map { it.quality }),
                    comment = createComment(inputs.map { it.dataPointType }, "Division"),
                    dataSource = mergeDataSources(dataPoints.mapNotNull { it.dataSource }),
                )

            return UploadedDataPoint(
                dataPoint = defaultObjectMapper.writeValueAsString(calculatedDataPoint),
                reportingPeriod = inputs.first().reportingPeriod,
                companyId = inputs.first().companyId,
                dataPointType = targetType,
            )
        }
    },

    DIVISION_BY_PERCENT("DivisionByPercent") {
        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
        ): UploadedDataPoint {
            val dataPoints = inputs.map { defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(it.dataPoint) }
            require(dataPoints.none { it.value == null }) { "Data points for division by percent must not have null value fields." }
            require(dataPoints.size == 2) { "Exactly two data points must be provided for division by percent." }
            require(dataPoints[1].value!!.signum() != 0) { "The divisor in division by percent must not be zero." }
            val num = dataPoints[0].value!!
            val denom = dataPoints[1].value!!
            // Performs (num * 100) / denom with a single division/rounding step
            val result =
                num.multiply(ONE_HUNDRED).divide(
                    denom,
                    CALCULATION_SCALE,
                    CALCULATION_ROUNDING_MODE,
                )

            val calculatedDataPoint =
                ExtendedDataPoint(
                    value = result,
                    quality = mergeQuality(dataPoints.map { it.quality }),
                    comment = createComment(inputs.map { it.dataPointType }, "Division by percent"),
                    dataSource = mergeDataSources(dataPoints.mapNotNull { it.dataSource }),
                )

            return UploadedDataPoint(
                dataPoint = defaultObjectMapper.writeValueAsString(calculatedDataPoint),
                reportingPeriod = inputs.first().reportingPeriod,
                companyId = inputs.first().companyId,
                dataPointType = targetType,
            )
        }
    },

    IDENTITY("Identity") {
        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
        ): UploadedDataPoint {
            require(inputs.size == 1) { "Exactly one data point must be provided for identity rule." }
            return inputs.first().copy(dataPointType = targetType)
        }
    }, ;

    /**
     * Applies this conversion strategy to merge [inputs] into a single derived data point of [targetType].
     * @param inputs the source data points to be combined
     * @param targetType the data point type assigned to the resulting data point
     * @return the derived data point produced by this strategy
     */
    abstract fun convert(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
    ): UploadedDataPoint

    companion object {
        /**
         * Resolves the [DataPointConversion] whose [id] matches the given string.
         * @param id the textual identifier of the conversion strategy
         * @return the matching [DataPointConversion]
         * @throws IllegalArgumentException if no variant has the given [id]
         */
        fun byId(id: String): DataPointConversion =
            entries.firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("Unsupported method: $id")
    }
}

/**
 * Merges the given [QualityOptions] into a single entry, returning the lowest quality among them.
 */
internal fun mergeQuality(inputs: Collection<QualityOptions?>): QualityOptions? {
    val qualityOrder =
        listOf(
            QualityOptions.Audited, QualityOptions.Reported, QualityOptions.Estimated, QualityOptions.Incomplete,
            QualityOptions.NoDataFound, null,
        )
    return inputs.maxByOrNull { qualityOrder.indexOf(it) }
}

/**
 * Merges the given list of [ExtendedDocumentReference] into a single reference.
 */
internal fun mergeDataSources(inputs: Collection<ExtendedDocumentReference>): ExtendedDocumentReference {
    val usedReference = inputs.map { it.fileReference }.toSet().minOf { it }
    return ExtendedDocumentReference(
        fileReference = usedReference,
        fileName = inputs.first { it.fileReference == usedReference }.fileName,
        publicationDate = inputs.first { it.fileReference == usedReference }.publicationDate,
        page = inputs.first().page,
    )
}

/**
 * Creates a comment for the resulting data point indicating the [source] and [method] used to create it
 */
internal fun createComment(
    source: Collection<DataPointType>,
    method: String,
): String =
    "This data point was calculated applying the method \"$method\" using: " +
        source.joinToString(", ") { it } + " as input."

/**
 * Resolves [method] to a [DataPointConversion] and applies it to [inputs] producing a data point of [targetType].
 */
fun applyTransformation(
    inputs: Collection<UploadedDataPoint>,
    targetType: DataPointType,
    method: String,
): UploadedDataPoint = DataPointConversion.byId(method).convert(inputs, targetType)
