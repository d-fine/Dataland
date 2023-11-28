package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import java.math.BigDecimal
import kotlin.reflect.KClass

/**
Annotation class for validating percentages
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [CustomPercentageValidator::class])
annotation class BigDecimalPercentageDataPoint(
    val message: String = "{javax.validation.constraints.NotBlank.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Documentation required!
 */
class CustomPercentageValidator : ConstraintValidator<BigDecimalPercentageDataPoint, ExtendedDataPoint<BigDecimal>> {
    companion object {
        const val oneHundred = 100
    }

    override fun isValid(dataPoint: ExtendedDataPoint<BigDecimal>?, context: ConstraintValidatorContext?): Boolean {
        return if (dataPoint == null) {
            true
        } else {
            dataPoint.value == null || dataPoint.value in BigDecimal.ZERO..BigDecimal(oneHundred)
        }
    }
}
