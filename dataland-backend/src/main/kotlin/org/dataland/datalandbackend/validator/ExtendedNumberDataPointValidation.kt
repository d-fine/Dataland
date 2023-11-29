package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import java.math.BigDecimal
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * Annotation for the validation of an ExtendedDataPoint<*> holding a number
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [ExtendedNumberDataPointValidator::class])
annotation class ExtendedNumberDataPointValidation(
    val negative: Boolean = false,
    val message: String = "An extended data point holding a number has failed to pass the validation!",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for an ExtendedDataPoint<*> featuring a number
 */
class ExtendedNumberDataPointValidator : ConstraintValidator<ExtendedNumberDataPointValidation, ExtendedDataPoint<*>> {

    private var negative by Delegates.notNull<Boolean>()

    override fun initialize(constraintAnnotation: ExtendedNumberDataPointValidation) {
        this.negative = constraintAnnotation.negative
    }

    override fun isValid(dataPoint: ExtendedDataPoint<*>?, context: ConstraintValidatorContext?): Boolean {
        return if (dataPoint == null) {
            true
        } else {
            if (this.negative) {
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
}
