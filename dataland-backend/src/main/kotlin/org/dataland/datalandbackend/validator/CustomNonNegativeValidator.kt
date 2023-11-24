package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint

class CustomNonNegativeValidator: ConstraintValidator<NonNegativeDataPoint, ExtendedDataPoint<Number>> {
    override fun isValid(dataPoint: ExtendedDataPoint<Number>, context: ConstraintValidatorContext?): Boolean {
        if (dataPoint.value == null) return true
        return (dataPoint.value.toDouble() >= 0) //is this ok?
    }
}


@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [CustomNonNegativeValidator::class])
annotation class NonNegativeDataPoint(
        val message: String = "{javax.validation.constraints.NotBlank.message}",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = []
)

