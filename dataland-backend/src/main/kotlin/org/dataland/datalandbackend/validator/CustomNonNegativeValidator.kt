package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint

class CustomNonNegativeValidator : ConstraintValidator<NonNegativeDataPoint, ExtendedDataPoint<T>> {
    override fun isValid(dataPoint: ExtendedDataPoint<T>, context: ConstraintValidatorContext?): Boolean {
        if (dataPoint.value == null) return true
        return (dataPoint.value > 0)
    }
}


@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [CustomNonNegativeValidator::class])
annotation class NonNegativeDataPoint(
        val message: String = "{javax.validation.constraints.NotBlank.message}",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = []
)

