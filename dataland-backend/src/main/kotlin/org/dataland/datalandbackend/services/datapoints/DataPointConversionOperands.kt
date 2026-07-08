package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import java.math.BigDecimal
import java.math.RoundingMode
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint as ExtendedDataPointInterface

private const val CALCULATION_SCALE = 10
private val CALCULATION_ROUNDING_MODE = RoundingMode.HALF_UP

internal data class SumOperands<T : ExtendedDataPointInterface<BigDecimal>>(
    val dataPoints: List<T>,
    val values: List<BigDecimal>,
)

internal inline fun <reified T : ExtendedDataPointInterface<BigDecimal>> extractSumOperands(
    inputs: Collection<UploadedDataPoint>,
): SumOperands<T> {
    val dataPoints = inputs.map { defaultObjectMapper.readValue<T>(it.dataPoint) }
    require(dataPoints.size >= 2) { "At least two data points must be provided for summation." }
    val values =
        dataPoints.map {
            requireNotNull(it.value) { "Data points for summation must not have null value fields." }
        }
    return SumOperands(dataPoints = dataPoints, values = values)
}

internal inline fun <reified T : ExtendedDataPointInterface<*>> extractIdentityOperand(inputs: Collection<UploadedDataPoint>): T {
    val dataPoints = inputs.map { defaultObjectMapper.readValue<T>(it.dataPoint) }
    require(dataPoints.size == 1) { "Exactly one data point must be provided for the identity rule." }
    return dataPoints.single()
}

internal data class DivisionOperands<N : ExtendedDataPointInterface<BigDecimal>, D : ExtendedDataPointInterface<BigDecimal>>(
    val numerator: N,
    val denominator: D,
    val numeratorValue: BigDecimal,
    val denominatorValue: BigDecimal,
) {
    fun calculateValue(multiplier: BigDecimal): BigDecimal =
        numeratorValue.multiply(multiplier).divide(
            denominatorValue,
            CALCULATION_SCALE,
            CALCULATION_ROUNDING_MODE,
        )
}

internal inline fun <
    reified N : ExtendedDataPointInterface<BigDecimal>,
    reified D : ExtendedDataPointInterface<BigDecimal>,
> extractDivisionOperands(
    inputs: Collection<UploadedDataPoint>,
    operationName: String,
): DivisionOperands<N, D> {
    val nullValueErrorMessage = "Data points for $operationName must not have null value fields."
    require(inputs.size == 2) { "Exactly two data points must be provided for $operationName." }
    val numerator = defaultObjectMapper.readValue<N>(inputs.elementAt(0).dataPoint)
    val denominator = defaultObjectMapper.readValue<D>(inputs.elementAt(1).dataPoint)
    val numeratorValue = requireNotNull(numerator.value) { nullValueErrorMessage }
    val denominatorValue = requireNotNull(denominator.value) { nullValueErrorMessage }
    require(denominatorValue.signum() != 0) { "The divisor in $operationName must not be zero." }
    return DivisionOperands(
        numerator = numerator,
        denominator = denominator,
        numeratorValue = numeratorValue,
        denominatorValue = denominatorValue,
    )
}
