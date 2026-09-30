package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint
import kotlin.reflect.KClass

/**
 * Annotation for the cross-field validation of an ExtendedCurrencyDataPoint.
 * Enforces that the value and the currency fields must either both be set or both be null.
 */
@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = [CurrencyDataPointConsistencyValidator::class])
annotation class ValidCurrencyDataPoint(
    val message: String =
        "Input validation failed: A currency data point must have both a value and a currency set, " +
            "or have both of them unset.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for the currency/value consistency of an ExtendedCurrencyDataPoint.
 */
class CurrencyDataPointConsistencyValidator : ConstraintValidator<ValidCurrencyDataPoint, ExtendedCurrencyDataPoint> {
    override fun isValid(
        dataPoint: ExtendedCurrencyDataPoint?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (dataPoint == null) return true
        return (dataPoint.value == null) == (dataPoint.currency == null)
    }
}
