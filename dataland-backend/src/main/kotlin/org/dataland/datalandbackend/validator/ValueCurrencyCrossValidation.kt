package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.model.datapoints.standard.CurrencyData
import kotlin.reflect.KClass

/**
 * Constraint Annotation for cross-validating fields "Value" and "Quality" of ExtendedDataPoints
 */
@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = [ValueCurrencyCrossValidator::class])
annotation class ValueAndCurrency(
    val message: String = "provided currency and value do not fit.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator Class containing logic for QualityAndValue Constraint Annotation
 */
class ValueCurrencyCrossValidator : ConstraintValidator<ValueAndCurrency, CurrencyData> {
    /**
     * Currently, only checks if value is null. In this case, quality must either be null or NoDataFound
     */
    override fun isValid(
        dataPoint: CurrencyData?,
        context: ConstraintValidatorContext?,
    ): Boolean =
        when {
            dataPoint?.value == null && dataPoint?.currency != null -> false
            dataPoint?.value != null && dataPoint?.currency == null -> false
            else -> true
        }
}
