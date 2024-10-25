package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.model.datapoints.standard.CurrencyDataPoint
import kotlin.reflect.KClass

/**
 * Constraint Annotation for cross-validating fields "value" and "currency" of a data point containing a currency
 */
@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = [ValueCurrencyValidator::class])
annotation class ValueAndCurrency(
    val message: String = "Input validation: currency and value have to be both set or both null.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator Class containing logic for ValueAndCurrency Constraint Annotation
 */
class ValueCurrencyValidator : ConstraintValidator<ValueAndCurrency, CurrencyDataPoint> {
    /**
     * Value and currency have to be both set or both null. Setting only one of the two is invalid.
     */
    override fun isValid(
        dataPoint: CurrencyDataPoint?,
        context: ConstraintValidatorContext?,
    ): Boolean =
        when {
            dataPoint?.value == null && dataPoint?.currency != null -> false
            dataPoint?.value != null && dataPoint.currency == null -> false
            else -> true
        }
}
