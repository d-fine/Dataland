package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import java.math.BigDecimal
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * Annotation for the validation of an ExtendedDataPoint<BigDecimal> holding a percentage value
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [PercentageDataPointValidator::class])
annotation class PercentageDataPointValidation(
    val largerThan100: Boolean = false,
    val negative: Boolean = false,
    val message: String = "A percentage data point has failed to pass the validation!",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for an ExtendedDataPoint<BigDecimal> featuring a percentage value
 */
class PercentageDataPointValidator : ConstraintValidator<PercentageDataPointValidation, ExtendedDataPoint<BigDecimal>> {
    companion object {
        const val upperPercentageBound = 100
    }

    private var largerThan100 by Delegates.notNull<Boolean>()
    private var negative by Delegates.notNull<Boolean>()

    override fun initialize(constraintAnnotation: PercentageDataPointValidation) {
        this.largerThan100 = constraintAnnotation.largerThan100
        this.negative = constraintAnnotation.negative
    }

    override fun isValid(dataPoint: ExtendedDataPoint<BigDecimal>?, context: ConstraintValidatorContext?): Boolean {
        return if (dataPoint?.value == null) {
            true
        } else {
            if (this.largerThan100 || this.negative) {
                if (this.largerThan100 && this.negative) {
                    true
                } else if (this.largerThan100 && !this.negative) {
                    dataPoint.value >= BigDecimal.ZERO
                } else {
                    dataPoint.value in BigDecimal(-upperPercentageBound)..BigDecimal(upperPercentageBound)
                }
            } else {
                dataPoint.value in BigDecimal.ZERO..BigDecimal(upperPercentageBound)
            }
        }
    }
}
