package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import java.math.BigDecimal
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint as ExtendedDataPointInterface

private val ONE_HUNDRED = BigDecimal("100")

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
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String =
            getNumberedSourceReferences(inputs).joinToString(" + ")

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint = convertSum(inputs, targetType, specs, sourceFrameworksByType)
    },

    DIVISION("Division") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String =
            getNumberedSourceReferences(inputs).joinToString(" / ")

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint =
            convertDivision(
                inputs = inputs,
                targetType = targetType,
                specs = specs,
                sourceFrameworksByType = sourceFrameworksByType,
                multiplier = BigDecimal.ONE,
                operationName = "division",
            )
    },

    DIVISION_BY_PERCENT("DivisionByPercent") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String =
            "100 * ${getNumberedSourceReferences(inputs).joinToString(" / ")}"

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint =
            convertDivision(
                inputs = inputs,
                targetType = targetType,
                specs = specs,
                sourceFrameworksByType = sourceFrameworksByType,
                multiplier = ONE_HUNDRED,
                operationName = "division by percent",
            )
    },

    IDENTITY("Identity") {
        override fun createComment(
            inputs: Collection<UploadedDataPoint>,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            dataPoints: Collection<ExtendedDataPointInterface<*>>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): String =
            "This data point was mapped from the following source: " +
                "${getNumberedSourceReferences(inputs).single()}\n\n***\n\n" +
                getSourcesSection(inputs, specs, dataPoints, sourceFrameworksByType)

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint = convertIdentity(inputs, targetType, specs, sourceFrameworksByType)
    }, ;

    /**
     * Applies this conversion strategy to merge [inputs] into a single derived data point of [targetType].
     *
     * @param inputs the source data points to be combined
     * @param targetType the data point type assigned to the resulting data point
     * @param specs the data point type specifications used to deserialize and label inputs
     * @param sourceFrameworksByType framework specifications associated with each source data point type
     * @return the derived data point produced by this strategy
     */
    abstract fun convert(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
    ): UploadedDataPoint

    /**
     * Creates a comment for the resulting data point describing this conversion's formula.
     *
     * @param inputs the uploaded data points used as calculation inputs
     * @param specs the data point type specifications used to resolve input display names
     * @param dataPoints the deserialized source data points used for the conversion
     * @param sourceFrameworksByType framework specifications associated with each source data point type
     * @return a generated comment describing the calculation
     */
    open fun createComment(
        inputs: Collection<UploadedDataPoint>,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        dataPoints: Collection<ExtendedDataPointInterface<*>>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
    ): String =
        getCalculationComment(
            formula = getCalculationFormula(inputs),
            inputs = inputs,
            specs = specs,
            dataPoints = dataPoints,
            sourceFrameworksByType = sourceFrameworksByType,
        )

    /**
     * Returns the formula fragment used in the generated calculation comment.
     *
     * @param inputs the uploaded data points used as calculation inputs
     * @return a formula using numbered source references such as `[1]`
     */
    protected open fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String =
        throw UnsupportedOperationException("Conversion $id does not use a calculation formula.")

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

    /**
     * Sums the values of [inputs] into a single derived data point of [targetType].
     *
     * @param inputs the source data points to be summed
     * @param targetType the data point type assigned to the resulting data point
     * @param specs the data point type specifications used to deserialize and label inputs
     * @param sourceFrameworksByType framework specifications associated with each source data point type
     * @return the derived data point produced by the summation
     */
    protected fun convertSum(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
    ): UploadedDataPoint {
        val calculatedDataPoint =
            if (isCurrencyDataPoint(targetType, specs)) {
                val operands = extractSumOperands<ExtendedCurrencyDataPoint>(inputs)
                ExtendedCurrencyDataPoint(
                    value = operands.values.sumOf { it },
                    currency = getCommonCurrency(operands.dataPoints),
                    quality = mergeQuality(operands.dataPoints.map { it.quality }),
                    comment = createComment(inputs, specs, operands.dataPoints, sourceFrameworksByType),
                    dataSource = mergeDataSources(operands.dataPoints.mapNotNull(::getDataSource)),
                )
            } else {
                val operands = extractSumOperands<ExtendedDataPoint<BigDecimal>>(inputs)
                ExtendedDataPoint(
                    value = operands.values.sumOf { it },
                    quality = mergeQuality(operands.dataPoints.map { it.quality }),
                    comment = createComment(inputs, specs, operands.dataPoints, sourceFrameworksByType),
                    dataSource = mergeDataSources(operands.dataPoints.mapNotNull(::getDataSource)),
                )
            }

        return createUploadedDataPoint(
            inputs = inputs,
            targetType = targetType,
            calculatedDataPoint = calculatedDataPoint,
        )
    }

    /**
     * Divides the first of [inputs] by the second, optionally scaling by [multiplier], into a data point of [targetType].
     *
     * @param inputs the numerator and denominator source data points
     * @param targetType the data point type assigned to the resulting data point
     * @param specs the data point type specifications used to deserialize and label inputs
     * @param sourceFrameworksByType framework specifications associated with each source data point type
     * @param multiplier a factor applied to the numerator before the division
     * @param operationName the human-readable operation name used in validation error messages
     * @return the derived data point produced by the division
     */
    protected fun convertDivision(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        multiplier: BigDecimal,
        operationName: String,
    ): UploadedDataPoint {
        val calculatedDataPoint =
            if (isCurrencyDataPoint(targetType, specs)) {
                val operands =
                    extractDivisionOperands<ExtendedCurrencyDataPoint, ExtendedDataPoint<BigDecimal>>(
                        inputs,
                        operationName,
                    )
                val sources = listOf(operands.numerator, operands.denominator)
                ExtendedCurrencyDataPoint(
                    value = operands.calculateValue(multiplier),
                    currency = getCurrency(operands.numerator),
                    quality = mergeQuality(sources.map { it.quality }),
                    comment = createComment(inputs, specs, sources, sourceFrameworksByType),
                    dataSource = mergeDataSources(sources.mapNotNull(::getDataSource)),
                )
            } else {
                val operands =
                    extractDivisionOperands<ExtendedDataPoint<BigDecimal>, ExtendedDataPoint<BigDecimal>>(
                        inputs,
                        operationName,
                    )
                val sources = listOf(operands.numerator, operands.denominator)
                ExtendedDataPoint(
                    value = operands.calculateValue(multiplier),
                    quality = mergeQuality(sources.map { it.quality }),
                    comment = createComment(inputs, specs, sources, sourceFrameworksByType),
                    dataSource = mergeDataSources(sources.mapNotNull(::getDataSource)),
                )
            }

        return createUploadedDataPoint(
            inputs = inputs,
            targetType = targetType,
            calculatedDataPoint = calculatedDataPoint,
        )
    }

    /**
     * Maps the single element of [inputs] into a derived data point of [targetType] without altering its value.
     *
     * @param inputs the single source data point to be mapped
     * @param targetType the data point type assigned to the resulting data point
     * @param specs the data point type specifications used to deserialize and label inputs
     * @param sourceFrameworksByType framework specifications associated with each source data point type
     * @return the derived data point produced by the identity mapping
     */
    protected fun convertIdentity(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
    ): UploadedDataPoint {
        val calculatedDataPoint =
            if (isCurrencyDataPoint(targetType, specs)) {
                val dataPoint = extractIdentityOperand<ExtendedCurrencyDataPoint>(inputs)
                ExtendedCurrencyDataPoint(
                    value = dataPoint.value,
                    currency = getCurrency(dataPoint),
                    quality = dataPoint.quality,
                    comment = createComment(inputs, specs, listOf(dataPoint), sourceFrameworksByType),
                    dataSource = dataPoint.dataSource,
                )
            } else {
                val dataPoint = extractIdentityOperand<ExtendedDataPoint<Any?>>(inputs)
                ExtendedDataPoint(
                    value = dataPoint.value,
                    quality = dataPoint.quality,
                    comment = createComment(inputs, specs, listOf(dataPoint), sourceFrameworksByType),
                    dataSource = dataPoint.dataSource,
                )
            }

        return createUploadedDataPoint(
            inputs = inputs,
            targetType = targetType,
            calculatedDataPoint = calculatedDataPoint,
        )
    }
}

/**
 * Resolves [method] to a [DataPointConversion] and applies it to [inputs] producing a data point of [targetType].
 *
 * @param inputs the source data points to be converted
 * @param targetType the data point type assigned to the resulting data point
 * @param method the textual identifier of the conversion strategy
 * @param specs the data point type specifications used during conversion
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @return the derived data point produced by the resolved strategy
 */
fun applyTransformation(
    inputs: Collection<UploadedDataPoint>,
    targetType: DataPointType,
    method: String,
    specs: Map<DataPointType, DataPointTypeSpecification>,
    sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
): UploadedDataPoint = DataPointConversion.byId(method).convert(inputs, targetType, specs, sourceFrameworksByType)
