package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

const val REPORTING_PERIOD_SHIFT_ERROR_MESSAGE = "Invalid reporting period shift. Only null, 0, or -1 are allowed."

/**
 * Annotation for validating a reporting period shift.
 * Valid values are: null, 0, or -1.
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [ValidReportingPeriodShiftValidator::class])
annotation class ValidReportingPeriodShift(
    val message: String = REPORTING_PERIOD_SHIFT_ERROR_MESSAGE,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator class for reporting period shift.
 * Accepts null, 0, or -1 as valid inputs.
 */
class ValidReportingPeriodShiftValidator : ConstraintValidator<ValidReportingPeriodShift, Int?> {
    override fun isValid(
        value: Int?,
        context: ConstraintValidatorContext?,
    ): Boolean = value == null || value == 0 || value == -1
}
