package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * Annotation for the maximum validation of a number field or a datapoint holding a number
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [FieldMaximumValidator::class, DataPointMaximumValidator::class])
annotation class MaximumValue(
    val maximumValue: Long = 100,
    val message: String = "Input validation failed: The value of the field is a larger number than the set maximum.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the maximum validation logic for a field of a number type
 */
class FieldMaximumValidator : ConstraintValidator<MaximumValue, Number> {
    private var maximumValue by Delegates.notNull<Long>()

    override fun initialize(constraintAnnotation: MaximumValue) {
        this.maximumValue = constraintAnnotation.maximumValue
    }

    override fun isValid(
        value: Number?,
        context: ConstraintValidatorContext?,
    ): Boolean = isValidNumber(value, maximumValue)
}

/**
 * Class holding the maximum validation logic for a field of a datapoint type with number value
 */
class DataPointMaximumValidator : ConstraintValidator<MaximumValue, BaseDataPoint<*>> {
    private var maximumValue by Delegates.notNull<Long>()

    override fun initialize(constraintAnnotation: MaximumValue) {
        this.maximumValue = constraintAnnotation.maximumValue
    }

    override fun isValid(
        dataPoint: BaseDataPoint<*>?,
        context: ConstraintValidatorContext?,
    ): Boolean =
        if (dataPoint?.value == null) {
            true
        } else if (dataPoint.value !is Number) {
            throw InvalidInputApiException(
                "This validator is used for a wrong type",
                "Type ${dataPoint.value!!::class.simpleName} as data point value is not handled by number validator",
            )
        } else {
            isValidNumber(dataPoint.value as Number, maximumValue)
        }
}

private fun isValidNumber(
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
