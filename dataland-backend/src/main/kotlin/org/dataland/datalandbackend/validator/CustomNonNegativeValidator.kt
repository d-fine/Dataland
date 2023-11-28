package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import java.math.BigDecimal
import kotlin.reflect.KClass

/**
 * Documentation required!
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [CustomNonNegativeValidator::class])
annotation class NonNegativeDataPoint(
    val message: String = "{javax.validation.constraints.NotBlank.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Documentation required!
 */
class CustomNonNegativeValidator : ConstraintValidator<NonNegativeDataPoint, ExtendedDataPoint<*>> {
    override fun isValid(dataPoint: ExtendedDataPoint<*>?, context: ConstraintValidatorContext?): Boolean {
        return if (dataPoint == null) {
            true
        } else {
            when (dataPoint.value) {
                null -> true
                is BigDecimal -> dataPoint.value >= BigDecimal.ZERO
                is Long -> dataPoint.value >= 0
                else -> throw InvalidInputApiException(
                    "This validator is used for a wrong type",
                    "Type ${dataPoint.value} inside ExtendedDataPoint is not handled by number validator",
                )
            }
        }
    }
}
