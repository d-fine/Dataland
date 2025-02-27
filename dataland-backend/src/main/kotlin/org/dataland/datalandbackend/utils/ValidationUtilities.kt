package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Returns true if the first argument is null or larger than or equal to the second argument.
 */
fun isGreaterOrEqual(
    value: Number?,
    minimumValue: Long,
) = when (value) {
    null -> true
    is BigDecimal -> value >= BigDecimal.valueOf(minimumValue)
    is BigInteger -> value >= BigInteger.valueOf(minimumValue)
    is Long -> value >= minimumValue
    else -> throw InvalidInputApiException(
        "This validator is used for a wrong type",
        "Type ${value::class.simpleName} is not handled by number validator",
    )
}

/**
 * Returns true if the first argument is null or smaller than or equal to the second argument.
 */
fun isLessOrEqual(
    value: Number?,
    maximumValue: Long,
) = when (value) {
    null -> true
    is BigDecimal -> value <= BigDecimal.valueOf(maximumValue)
    is BigInteger -> value <= BigInteger.valueOf(maximumValue)
    is Long -> value <= maximumValue
    else -> throw InvalidInputApiException(
        "This validator is used for a wrong type",
        "Type ${value::class.simpleName} is not handled by number validator",
    )
}

/**
 * Validate the minimum value constraint for a generic BaseDataPoint.
 */
fun validateMinimumValueConstraint(
    dataPoint: BaseDataPoint<*>?,
    minimumValue: Long,
): Boolean = validateBoundaryConstraint(dataPoint, minimumValue, ::isGreaterOrEqual)

/**
 * Validate the maximum value constraint for a generic BaseDataPoint.
 */
fun validateMaximumValueConstraint(
    dataPoint: BaseDataPoint<*>?,
    maximumValue: Long,
): Boolean = validateBoundaryConstraint(dataPoint, maximumValue, ::isLessOrEqual)

/**
 * Common code for minimum and maximum value constraint validation.
 */
private fun validateBoundaryConstraint(
    dataPoint: BaseDataPoint<*>?,
    boundary: Long,
    comparingOp: (Number, Long) -> Boolean,
): Boolean =
    if (dataPoint?.value == null) {
        true
    } else if (dataPoint.value !is Number) {
        throw InvalidInputApiException(
            "This validator is used for a wrong type",
            "Type ${dataPoint.value!!::class.simpleName} as data point value is not handled by number validator",
        )
    } else {
        comparingOp(dataPoint.value as Number, boundary)
    }

/**
 *  A helper class that stores a regex pattern to match against a constraint obtained from
 *  the specification service and a function that is invoked if the regex pattern matches.
 */
private data class SpecificationServiceConstraintHandler(
    val regexPattern: String,
    val handlerMethod: (BaseDataPoint<*>?, MatchResult) -> Unit,
)

private const val INTEGER_NUMBER_REGEX = "(-?\\d+)"

private val specificationServiceConstraintHandlers: List<SpecificationServiceConstraintHandler> =
    listOf(
        SpecificationServiceConstraintHandler("between:${INTEGER_NUMBER_REGEX},${INTEGER_NUMBER_REGEX}", ::validateBetweenConstraint),
        SpecificationServiceConstraintHandler("min:${INTEGER_NUMBER_REGEX}", ::validateMinConstraint),
        SpecificationServiceConstraintHandler("max:${INTEGER_NUMBER_REGEX}", ::validateMaxConstraint),
    )

private fun validateBetweenConstraint(
    dataPoint: BaseDataPoint<*>?,
    matchResult: MatchResult,
) {
    val minValue = matchResult.groupValues[1].toLong()
    val maxValue = matchResult.groupValues[2].toLong()
    if (!validateMaximumValueConstraint(dataPoint, maxValue) || !validateMinimumValueConstraint(dataPoint, minValue)) {
        throw InvalidInputApiException(
            "Boundary constraint violation",
            "This posted number lies outside the allowed range [$minValue, $maxValue]",
        )
    }
}

private fun validateMinConstraint(
    dataPoint: BaseDataPoint<*>?,
    matchResult: MatchResult,
) {
    val minValue = matchResult.groupValues[1].toLong()
    if (!validateMinimumValueConstraint(dataPoint, minValue)) {
        throw InvalidInputApiException(
            "Minimum constraint violation",
            "This posted number is smaller than the allowed minimum $minValue",
        )
    }
}

private fun validateMaxConstraint(
    dataPoint: BaseDataPoint<*>?,
    matchResult: MatchResult,
) {
    val maxValue = matchResult.groupValues[1].toLong()
    if (!validateMaximumValueConstraint(dataPoint, maxValue)) {
        throw InvalidInputApiException(
            "Maximum constraint violation",
            "This posted number is larger than the allowed maximum $maxValue",
        )
    }
}

/**
 * Validate a single constraint obtained from the specification service.
 * @param dataPoint the BaseDataPoint whose value shall be validated
 * @param constraint a string encoding the constraint
 */
private fun validateConstraint(
    dataPoint: BaseDataPoint<*>?,
    constraint: String,
) {
    val constraintMatches =
        specificationServiceConstraintHandlers.map { handler ->
            Regex(handler.regexPattern).matchEntire(constraint)?.also { matchResult ->
                handler.handlerMethod(dataPoint, matchResult)
            }
        }
    if (constraintMatches.all { it == null }) {
        throw InternalServerErrorApiException(
            "Processing data point error",
            "The data point could not be validated because of an internal error",
            "The constraint '$constraint' was not processed by any of the constraint handlers!'",
        )
    }
}

/**
 * Validates constraints obtained from the specification service as a list of strings.
 * A null value always validates.
 * @param dataPoint the BaseDataPoint whose value shall be validated
 * @param constraints a list of strings encoding the constraints
 */
fun validateConstraints(
    dataPoint: BaseDataPoint<*>?,
    constraints: List<String>,
) {
    if (dataPoint != null) {
        constraints.forEach { validateConstraint(dataPoint, it) }
    }
}
