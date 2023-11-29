package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import java.math.BigDecimal
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * Annotation for the validation of a CurrencyDataPoint
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [CurrencyDataPointValidator::class])
annotation class CurrencyDataPointValidation(
    val negative: Boolean = false,
    val message: String = "A currency data point has failed to pass the validation!",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for a CurrencyDataPoint
 */
class CurrencyDataPointValidator : ConstraintValidator<CurrencyDataPointValidation, CurrencyDataPoint> {

    private var negative by Delegates.notNull<Boolean>()

    override fun initialize(constraintAnnotation: CurrencyDataPointValidation) {
        this.negative = constraintAnnotation.negative
    }

    override fun isValid(dataPoint: CurrencyDataPoint?, context: ConstraintValidatorContext?): Boolean {
        return if (dataPoint == null) {
            true
        } else {
            if (dataPoint.value == null) {
                true
            } else {
                if (this.negative) {
                    true
                } else {
                    dataPoint.value >= BigDecimal.ZERO
                }
            }
        }
    }
}
