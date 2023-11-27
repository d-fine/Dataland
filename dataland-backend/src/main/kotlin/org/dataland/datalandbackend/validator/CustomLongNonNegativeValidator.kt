package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint


@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [CustomLongNonNegativeValidator::class])
annotation class LongNonNegativeDataPoint(
        val message: String = "{javax.validation.constraints.NotBlank.message}",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = []
)

class CustomLongNonNegativeValidator : ConstraintValidator<LongNonNegativeDataPoint, ExtendedDataPoint<Long>> {
    override fun isValid(dataPoint: ExtendedDataPoint<Long>?, context: ConstraintValidatorContext?): Boolean {
        return if (dataPoint == null) {
            true
        } else {
            dataPoint.value == null || dataPoint.value >= 0
        }
    }
}

