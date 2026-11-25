package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import java.math.BigDecimal

private const val REQUIRED_INPUT_COUNT = 3

/**
 * Derives a decimal data point from [inputs] using [compute] on the ordered input values.
 *
 * @param minimumInputCount the minimum number of inputs required
 * @param compute computes the result value from the ordered input values
 * @return the derived data point
 */
internal fun DataPointConversion.convertNumeric(
    inputs: Collection<UploadedDataPoint>,
    targetType: DataPointType,
    specs: Map<DataPointType, DataPointTypeSpecification>,
    sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
    operationName: String,
    minimumInputCount: Int = 2,
    compute: (List<BigDecimal>) -> BigDecimal,
): UploadedDataPoint {
    val orderedInputs = requireOrderedInputs(inputs, operationName)
    val operands = extractNumericOperands(orderedInputs, operationName, minimumInputCount)
    val calculatedDataPoint =
        ExtendedDataPoint(
            value = compute(operands.values),
            quality = mergeQuality(operands.dataPoints.map { it.quality }),
            comment = createComment(inputs, specs, operands.dataPoints, sourceFrameworksByType),
            dataSource = mergeDataSources(operands.dataPoints.mapNotNull(::getDataSource)),
        )
    return createUploadedDataPoint(
        inputs = inputs,
        targetType = targetType,
        calculatedDataPoint = calculatedDataPoint,
    )
}

/**
 * Sums the values of [inputs] into a single derived data point of [targetType].
 * @param inputs the source data points to be summed
 * @param targetType the data point type assigned to the resulting data point
 * @param specs the data point type specifications used to deserialize and label inputs
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @return the derived data point produced by the summation
 */
internal fun DataPointConversion.convertSum(
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
 * @param inputs the numerator and denominator source data points
 * @param targetType the data point type assigned to the resulting data point
 * @param specs the data point type specifications used to deserialize and label inputs
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @param multiplier a factor applied to the numerator before the division
 * @param operationName the human-readable operation name used in validation error messages
 * @return the derived data point produced by the division
 */
internal fun DataPointConversion.convertDivision(
    inputs: List<UploadedDataPoint>,
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
 * Subtracts the second of [inputs] from the first into a data point of [targetType].
 *
 * @param inputs the minuend and subtrahend source data points
 * @param targetType the data point type assigned to the resulting data point
 * @param specs the data point type specifications used to deserialize and label inputs
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @return the derived data point produced by the subtraction
 */
internal fun DataPointConversion.convertSubtraction(
    inputs: List<UploadedDataPoint>,
    targetType: DataPointType,
    specs: Map<DataPointType, DataPointTypeSpecification>,
    sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
): UploadedDataPoint {
    val calculatedDataPoint =
        if (isCurrencyDataPoint(targetType, specs)) {
            val operands = extractSubtractionOperands<ExtendedCurrencyDataPoint, ExtendedCurrencyDataPoint>(inputs)
            val sources = listOf(operands.minuend, operands.subtrahend)
            ExtendedCurrencyDataPoint(
                value = operands.calculateValue(),
                currency = getCommonCurrency(sources),
                quality = mergeQuality(sources.map { it.quality }),
                comment = createComment(inputs, specs, sources, sourceFrameworksByType),
                dataSource = mergeDataSources(sources.mapNotNull(::getDataSource)),
            )
        } else {
            val operands =
                extractSubtractionOperands<ExtendedDataPoint<BigDecimal>, ExtendedDataPoint<BigDecimal>>(inputs)
            val sources = listOf(operands.minuend, operands.subtrahend)
            ExtendedDataPoint(
                value = operands.calculateValue(),
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
 * Subtracts the single element of [inputs] from 100 into a data point of [targetType].
 *
 * @param inputs the single source data point holding the percentage to complement
 * @param targetType the data point type assigned to the resulting data point
 * @param specs the data point type specifications used to deserialize and label inputs
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @return the derived data point produced by the complement calculation
 */
internal fun DataPointConversion.convertComplementToPercent(
    inputs: Collection<UploadedDataPoint>,
    targetType: DataPointType,
    specs: Map<DataPointType, DataPointTypeSpecification>,
    sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
): UploadedDataPoint {
    val dataPoint = extractIdentityOperand<ExtendedDataPoint<BigDecimal>>(inputs)
    val value =
        requireNotNull(dataPoint.value) {
            "Data point for complement to percent must not have a null value field."
        }
    val calculatedDataPoint =
        ExtendedDataPoint(
            value = ONE_HUNDRED.subtract(value),
            quality = dataPoint.quality,
            comment = createComment(inputs, specs, listOf(dataPoint), sourceFrameworksByType),
            dataSource = dataPoint.dataSource,
        )

    return createUploadedDataPoint(
        inputs = inputs,
        targetType = targetType,
        calculatedDataPoint = calculatedDataPoint,
    )
}

/**
 * Multiplies the first of [inputs] by the second, treated as a percentage, into a data point of [targetType].
 *
 * @param inputs the value and percentage source data points
 * @param targetType the data point type assigned to the resulting data point
 * @param specs the data point type specifications used to deserialize and label inputs
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @param useComplement true to multiply by the complement of the percentage (100 - percentage) instead of the
 * percentage itself
 * @param operationName the human-readable operation name used in validation error messages
 * @return the derived data point produced by the multiplication
 */
internal fun DataPointConversion.convertMultiplicationByPercent(
    inputs: List<UploadedDataPoint>,
    targetType: DataPointType,
    specs: Map<DataPointType, DataPointTypeSpecification>,
    sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
    useComplement: Boolean,
    operationName: String,
): UploadedDataPoint {
    val calculatedDataPoint =
        if (isCurrencyDataPoint(targetType, specs)) {
            val operands = extractPercentageMultiplicationOperands<ExtendedCurrencyDataPoint>(inputs, operationName)
            val sources = listOf(operands.valueDataPoint, operands.percentDataPoint)
            ExtendedCurrencyDataPoint(
                value = operands.calculateShare(useComplement),
                currency = getCurrency(operands.valueDataPoint),
                quality = mergeQuality(sources.map { it.quality }),
                comment = createComment(inputs, specs, sources, sourceFrameworksByType),
                dataSource = mergeDataSources(sources.mapNotNull(::getDataSource)),
            )
        } else {
            val operands =
                extractPercentageMultiplicationOperands<ExtendedDataPoint<BigDecimal>>(inputs, operationName)
            val sources = listOf(operands.valueDataPoint, operands.percentDataPoint)
            ExtendedDataPoint(
                value = operands.calculateShare(useComplement),
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
 * Multiplies the first of [inputs] by the second, treated as a percentage, and subtracts the third from the
 * result into a data point of [targetType].
 *
 * @param inputs the value, percentage and amount-to-subtract source data points, in that order
 * @param targetType the data point type assigned to the resulting data point
 * @param specs the data point type specifications used to deserialize and label inputs
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @return the derived data point produced by the calculation
 */
internal fun DataPointConversion.convertMultiplicationByPercentMinusCurrency(
    inputs: List<UploadedDataPoint>,
    targetType: DataPointType,
    specs: Map<DataPointType, DataPointTypeSpecification>,
    sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
): UploadedDataPoint {
    val operationName = "multiplication by percent minus currency"
    require(inputs.size == REQUIRED_INPUT_COUNT) { "Exactly three data points must be provided for $operationName." }
    val percentageOperands =
        extractPercentageMultiplicationOperands<ExtendedCurrencyDataPoint>(inputs.subList(0, 2), operationName)
    val amountToSubtract = defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(inputs[2].dataPoint)
    val amountToSubtractValue =
        requireNotNull(amountToSubtract.value) {
            "Data points for $operationName must not have null value fields."
        }
    val sources = listOf(percentageOperands.valueDataPoint, percentageOperands.percentDataPoint, amountToSubtract)
    val calculatedDataPoint =
        ExtendedCurrencyDataPoint(
            value = percentageOperands.calculateShare().subtract(amountToSubtractValue),
            currency = getCommonCurrency(listOf(percentageOperands.valueDataPoint, amountToSubtract)),
            quality = mergeQuality(sources.map { it.quality }),
            comment = createComment(inputs, specs, sources, sourceFrameworksByType),
            dataSource = mergeDataSources(sources.mapNotNull(::getDataSource)),
        )

    return createUploadedDataPoint(
        inputs = inputs,
        targetType = targetType,
        calculatedDataPoint = calculatedDataPoint,
    )
}

/**
 * Maps the single element of [inputs] into a derived data point of [targetType] without altering its value.
 * @param inputs the single source data point to be mapped
 * @param targetType the data point type assigned to the resulting data point
 * @param specs the data point type specifications used to deserialize and label inputs
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @return the derived data point produced by the identity mapping
 */
internal fun DataPointConversion.convertIdentity(
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
