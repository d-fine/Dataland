package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import java.math.BigDecimal


@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [CustomBigDecimalNonNegativeValidator::class])
annotation class BigDecimalNonNegativeDataPoint(
        val message: String = "{javax.validation.constraints.NotBlank.message}",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = []
)


class CustomBigDecimalNonNegativeValidator : ConstraintValidator<BigDecimalNonNegativeDataPoint, ExtendedDataPoint<BigDecimal>> {
    override fun isValid(dataPoint: ExtendedDataPoint<BigDecimal>, context: ConstraintValidatorContext?): Boolean {
        return dataPoint.value == null || dataPoint.value >= BigDecimal.ZERO
    }
}

