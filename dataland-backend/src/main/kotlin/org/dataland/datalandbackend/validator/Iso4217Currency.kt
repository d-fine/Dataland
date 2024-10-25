package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.util.Currency
import kotlin.reflect.KClass

/**
 * Annotation for the minimum validation of a number field or a datapoint holding a number
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [CurrencyStringValidator::class])
annotation class Iso4217Currency(
    val message: String =
        "contains entry that does not represent a ISO 4217 currency. Valid examples: EUR, USD, JPY",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for a field of a string type
 */
class CurrencyStringValidator : ConstraintValidator<Iso4217Currency, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean = isValidCurrency(value)
}

private fun isValidCurrency(value: String?): Boolean {
    if (value == null) return true
    try {
        Currency.getInstance(value)
    } catch (e: IllegalArgumentException) {
        return false
    }
    return true
}
