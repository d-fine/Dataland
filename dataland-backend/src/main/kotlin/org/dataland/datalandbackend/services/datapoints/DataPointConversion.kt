package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import java.math.BigDecimal
import java.math.RoundingMode
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint as ExtendedDataPointInterface

private const val CALCULATION_SCALE = 10
private const val FORMULA_COMMENT_PREFIX = "This data point was calculated using the following formula: "
private val CALCULATION_ROUNDING_MODE = RoundingMode.HALF_UP
private val ONE_HUNDRED = BigDecimal("100")
private const val EXTENDED_CURRENCY_BASE_TYPE = "extendedCurrency"

/**
 * Closed set of strategies for deriving a data point from a collection of other data points.
 *
 * Variants are dispatched by their [id] via [byId].
 *
 * @param id the textual identifier of the conversion strategy
 */
enum class DataPointConversion(
    val id: String,
) {
    SUM("Sum") {
        override fun createComment(
            inputs: Collection<UploadedDataPoint>,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            dataPoints: Collection<ExtendedDataPointInterface<*>>,
        ): String {
            val sourceNames = getQuotedSourceNames(inputs, specs)
            val formula =
                if (sourceNames.size > 2) {
                    sourceNames.joinToString("\n+ ")
                } else {
                    sourceNames.joinToString(" + ")
                }
            return "$FORMULA_COMMENT_PREFIX$formula."
        }

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
        ): UploadedDataPoint {
            val calculatedDataPoint =
                if (isCurrencyDataPoint(targetType, specs)) {
                    val dataPoints = inputs.map { defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(it.dataPoint) }
                    require(dataPoints.size >= 2) { "At least two data points must be provided for summation." }
                    val values =
                        dataPoints.map {
                            requireNotNull(it.value) { "Data points for summation must not have null value fields." }
                        }
                    ExtendedCurrencyDataPoint(
                        value = values.sumOf { it },
                        currency = getCommonCurrency(dataPoints),
                        quality = mergeQuality(dataPoints.map { it.quality }),
                        comment = createComment(inputs, specs, dataPoints),
                        dataSource = mergeDataSources(dataPoints.mapNotNull { it.dataSource }),
                    )
                } else {
                    val dataPoints = inputs.map { defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(it.dataPoint) }
                    require(dataPoints.size >= 2) { "At least two data points must be provided for summation." }
                    val values =
                        dataPoints.map {
                            requireNotNull(it.value) { "Data points for summation must not have null value fields." }
                        }
                    ExtendedDataPoint(
                        value = values.sumOf { it },
                        quality = mergeQuality(dataPoints.map { it.quality }),
                        comment = createComment(inputs, specs, dataPoints),
                        dataSource = mergeDataSources(dataPoints.mapNotNull { it.dataSource }),
                    )
                }
            return createUploadedDataPoint(
                inputs = inputs,
                targetType = targetType,
                calculatedDataPoint = calculatedDataPoint,
            )
        }
    },

    DIVISION("Division") {
        override fun createComment(
            inputs: Collection<UploadedDataPoint>,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            dataPoints: Collection<ExtendedDataPointInterface<*>>,
        ): String = "$FORMULA_COMMENT_PREFIX${getQuotedSourceNames(inputs, specs).joinToString(" / ")}."

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
        ): UploadedDataPoint {
            val calculatedDataPoint =
                if (isCurrencyDataPoint(targetType, specs)) {
                    require(inputs.size == 2) { "Exactly two data points must be provided for division." }
                    val numerator =
                        defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(inputs.elementAt(0).dataPoint)
                    val denominator =
                        defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(inputs.elementAt(1).dataPoint)
                    val numeratorValue =
                        requireNotNull(numerator.value) { "Data points for division must not have null value fields." }
                    val denominatorValue =
                        requireNotNull(denominator.value) { "Data points for division must not have null value fields." }
                    require(denominatorValue.signum() != 0) { "The divisor in division must not be zero." }
                    ExtendedCurrencyDataPoint(
                        value =
                            numeratorValue.divide(
                                denominatorValue,
                                CALCULATION_SCALE,
                                CALCULATION_ROUNDING_MODE,
                            ),
                        currency = getCurrency(numerator),
                        quality = mergeQuality(listOf(numerator.quality, denominator.quality)),
                        comment = createComment(inputs, specs, listOf(numerator, denominator)),
                        dataSource = mergeDataSources(listOfNotNull(numerator.dataSource, denominator.dataSource)),
                    )
                } else {
                    val dataPoints = inputs.map { defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(it.dataPoint) }
                    require(dataPoints.size == 2) { "Exactly two data points must be provided for division." }
                    val numerator =
                        requireNotNull(dataPoints[0].value) { "Data points for division must not have null value fields." }
                    val denominator =
                        requireNotNull(dataPoints[1].value) { "Data points for division must not have null value fields." }
                    require(denominator.signum() != 0) { "The divisor in division must not be zero." }
                    ExtendedDataPoint(
                        value =
                            numerator.divide(
                                denominator,
                                CALCULATION_SCALE,
                                CALCULATION_ROUNDING_MODE,
                            ),
                        quality = mergeQuality(dataPoints.map { it.quality }),
                        comment = createComment(inputs, specs, dataPoints),
                        dataSource = mergeDataSources(dataPoints.mapNotNull { it.dataSource }),
                    )
                }

            return createUploadedDataPoint(
                inputs = inputs,
                targetType = targetType,
                calculatedDataPoint = calculatedDataPoint,
            )
        }
    },

    DIVISION_BY_PERCENT("DivisionByPercent") {
        override fun createComment(
            inputs: Collection<UploadedDataPoint>,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            dataPoints: Collection<ExtendedDataPointInterface<*>>,
        ): String = "${FORMULA_COMMENT_PREFIX}100 * ${getQuotedSourceNames(inputs, specs).joinToString(" / ")}."

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
        ): UploadedDataPoint {
            val calculatedDataPoint =
                if (isCurrencyDataPoint(targetType, specs)) {
                    require(inputs.size == 2) { "Exactly two data points must be provided for division by percent." }
                    val numerator =
                        defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(inputs.elementAt(0).dataPoint)
                    val denominator =
                        defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(inputs.elementAt(1).dataPoint)
                    val numeratorValue =
                        requireNotNull(numerator.value) { "Data points for division by percent must not have null value fields." }
                    val denominatorValue =
                        requireNotNull(denominator.value) { "Data points for division by percent must not have null value fields." }
                    require(denominatorValue.signum() != 0) { "The divisor in division by percent must not be zero." }
                    val result =
                        numeratorValue.multiply(ONE_HUNDRED).divide(
                            denominatorValue,
                            CALCULATION_SCALE,
                            CALCULATION_ROUNDING_MODE,
                        )
                    ExtendedCurrencyDataPoint(
                        value = result,
                        currency = getCurrency(numerator),
                        quality = mergeQuality(listOf(numerator.quality, denominator.quality)),
                        comment = createComment(inputs, specs, listOf(numerator, denominator)),
                        dataSource = mergeDataSources(listOfNotNull(numerator.dataSource, denominator.dataSource)),
                    )
                } else {
                    val dataPoints = inputs.map { defaultObjectMapper.readValue<ExtendedDataPoint<BigDecimal>>(it.dataPoint) }
                    require(dataPoints.size == 2) { "Exactly two data points must be provided for division by percent." }
                    val numerator =
                        requireNotNull(dataPoints[0].value) {
                            "Data points for division by percent must not have null value fields."
                        }
                    val denominator =
                        requireNotNull(dataPoints[1].value) {
                            "Data points for division by percent must not have null value fields."
                        }
                    require(denominator.signum() != 0) {
                        "The divisor in division by percent must not be zero."
                    }
                    val result =
                        numerator.multiply(ONE_HUNDRED).divide(
                            denominator,
                            CALCULATION_SCALE,
                            CALCULATION_ROUNDING_MODE,
                        )
                    ExtendedDataPoint(
                        value = result,
                        quality = mergeQuality(dataPoints.map { it.quality }),
                        comment = createComment(inputs, specs, dataPoints),
                        dataSource = mergeDataSources(dataPoints.mapNotNull { it.dataSource }),
                    )
                }

            return createUploadedDataPoint(
                inputs = inputs,
                targetType = targetType,
                calculatedDataPoint = calculatedDataPoint,
            )
        }
    },

    IDENTITY("Identity") {
        override fun createComment(
            inputs: Collection<UploadedDataPoint>,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            dataPoints: Collection<ExtendedDataPointInterface<*>>,
        ): String? = dataPoints.single().comment

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
        ): UploadedDataPoint {
            val calculatedDataPoint =
                if (isCurrencyDataPoint(targetType, specs)) {
                    val dataPoints = inputs.map { defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(it.dataPoint) }
                    require(dataPoints.size == 1) { "Exactly one data point must be provided for the identity rule." }
                    dataPoints.first().let {
                        ExtendedCurrencyDataPoint(
                            value = it.value,
                            currency = getCurrency(it),
                            quality = it.quality,
                            comment = createComment(inputs, specs, dataPoints),
                            dataSource = it.dataSource,
                        )
                    }
                } else {
                    val dataPoints = inputs.map { defaultObjectMapper.readValue<ExtendedDataPoint<Any?>>(it.dataPoint) }
                    require(dataPoints.size == 1) { "Exactly one data point must be provided for the identity rule." }
                    dataPoints.first().let {
                        ExtendedDataPoint(
                            value = it.value,
                            quality = it.quality,
                            comment = createComment(inputs, specs, dataPoints),
                            dataSource = it.dataSource,
                        )
                    }
                }

            return createUploadedDataPoint(
                inputs = inputs,
                targetType = targetType,
                calculatedDataPoint = calculatedDataPoint,
            )
        }
    }, ;

    /**
     * Applies this conversion strategy to merge [inputs] into a single derived data point of [targetType].
     *
     * @param inputs the source data points to be combined
     * @param targetType the data point type assigned to the resulting data point
     * @param specs the data point type specifications used to deserialize and label inputs
     * @return the derived data point produced by this strategy
     */
    abstract fun convert(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
        specs: Map<DataPointType, DataPointTypeSpecification>,
    ): UploadedDataPoint

    /**
     * Creates a comment for the resulting data point describing this conversion's formula.
     *
     * @param inputs the uploaded data points used as calculation inputs
     * @param specs the data point type specifications used to resolve input display names
     * @param dataPoints the deserialized source data points used for the conversion
     * @return a generated comment describing the calculation
     */
    abstract fun createComment(
        inputs: Collection<UploadedDataPoint>,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        dataPoints: Collection<ExtendedDataPointInterface<*>>,
    ): String?

    companion object {
        /**
         * Resolves the [DataPointConversion] whose [id] matches the given string.
         *
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
 * Checks whether [dataPointType] has the extended currency base type in [specs].
 *
 * @param dataPointType the data point type to inspect
 * @param specs the data point type specifications keyed by type
 * @return true if the type is specified as an extended currency data point
 */
private fun isCurrencyDataPoint(
    dataPointType: DataPointType,
    specs: Map<DataPointType, DataPointTypeSpecification>,
): Boolean = specs[dataPointType]?.dataPointBaseType?.id == EXTENDED_CURRENCY_BASE_TYPE

/**
 * Returns the currency set on [dataPoint].
 *
 * @param dataPoint the currency data point to inspect
 * @return the non-null currency of the data point
 * @throws IllegalArgumentException if [dataPoint] has no currency
 */
private fun getCurrency(dataPoint: ExtendedCurrencyDataPoint): String {
    require(dataPoint.currency != null) { "Currency data points used in calculations must have a currency." }
    return dataPoint.currency
}

/**
 * Returns the shared currency used by all [dataPoints].
 *
 * @param dataPoints the currency data points to inspect
 * @return the single currency used by all given data points
 * @throws IllegalArgumentException if not all data points have the same non-null currency
 */
private fun getCommonCurrency(dataPoints: Collection<ExtendedCurrencyDataPoint>): String {
    val currencies = dataPoints.map { getCurrency(it) }.toSet()
    require(currencies.size == 1) { "Currency data points used in summation must all have the same currency." }
    return currencies.single()
}

/**
 * Wraps [calculatedDataPoint] in an [UploadedDataPoint] using metadata from the first source input.
 *
 * @param inputs the source inputs providing reporting period and company ID
 * @param targetType the data point type assigned to the calculated data point
 * @param calculatedDataPoint the calculated data point object to serialize
 * @return the uploaded data point representing the calculation result
 */
private fun <T : Any> createUploadedDataPoint(
    inputs: Collection<UploadedDataPoint>,
    targetType: DataPointType,
    calculatedDataPoint: BaseDataPoint<T>,
): UploadedDataPoint =
    UploadedDataPoint(
        dataPoint = defaultObjectMapper.writeValueAsString(calculatedDataPoint),
        reportingPeriod = inputs.first().reportingPeriod,
        companyId = inputs.first().companyId,
        dataPointType = targetType,
    )

/**
 * Merges the given [QualityOptions] into a single entry, returning the lowest quality among them.
 *
 * @param inputs the quality values to merge
 * @return the lowest quality value among the inputs
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
 *
 * @param inputs the document references to merge
 * @return the reference with the smallest file reference, or null if no references are provided
 */
internal fun mergeDataSources(inputs: Collection<ExtendedDocumentReference>): ExtendedDocumentReference? =
    inputs.minByOrNull { it.fileReference }

/**
 * Resolves the display names of [inputs] in [specs] and wraps each name in double quotes.
 *
 * @param inputs the uploaded data points used as calculation inputs
 * @param specs the data point type specifications used to resolve input display names
 * @return the quoted display names of all inputs in order
 */
private fun getQuotedSourceNames(
    inputs: Collection<UploadedDataPoint>,
    specs: Map<DataPointType, DataPointTypeSpecification>,
): List<String> = inputs.map { "\"${specs.getValue(it.dataPointType).name}\"" }

/**
 * Resolves [method] to a [DataPointConversion] and applies it to [inputs] producing a data point of [targetType].
 *
 * @param inputs the source data points to be converted
 * @param targetType the data point type assigned to the resulting data point
 * @param method the textual identifier of the conversion strategy
 * @param specs the data point type specifications used during conversion
 * @return the derived data point produced by the resolved strategy
 */
fun applyTransformation(
    inputs: Collection<UploadedDataPoint>,
    targetType: DataPointType,
    method: String,
    specs: Map<DataPointType, DataPointTypeSpecification>,
): UploadedDataPoint = DataPointConversion.byId(method).convert(inputs, targetType, specs)
