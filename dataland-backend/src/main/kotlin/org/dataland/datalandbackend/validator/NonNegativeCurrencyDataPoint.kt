package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import java.math.BigDecimal
import kotlin.reflect.KClass

/**
 * Documentation required!
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [CustomCurrencyNonNegativeValidator::class])
annotation class NonNegativeCurrencyDataPoint(
    val message: String = "{javax.validation.constraints.NotBlank.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Documentation required!
 */
class CustomCurrencyNonNegativeValidator : ConstraintValidator<NonNegativeCurrencyDataPoint, CurrencyDataPoint> {
    override fun isValid(dataPoint: CurrencyDataPoint?, context: ConstraintValidatorContext?): Boolean {
        return if (dataPoint == null) {
            true
        } else {
            dataPoint.value == null || dataPoint.value >= BigDecimal.ZERO
        }
    }
}
