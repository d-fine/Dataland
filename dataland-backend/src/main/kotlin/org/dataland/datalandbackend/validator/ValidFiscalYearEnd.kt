package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Annotation for validating fiscal year-end strings in the format "dd-MMM" (e.g., "31-Mar").
 */
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_GETTER,
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [FiscalYearEndValidator::class])
annotation class ValidFiscalYearEnd(
    val message: String = "must be in format dd-MMM (e.g. \"31-Mar\") and represent a valid calendar date; 29-Feb is not allowed",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator for fiscal year-end strings in the format "dd-MMM" (e.g., "31-Mar").
 *     Valid:
 *     - 01–31 for Jan, Mar, May, Jul, Aug, Oct, Dec
 *     - 01–30 for Apr, Jun, Sep, Nov
 *     - 01–28 for Feb (29-Feb explicitly disallowed)
 * Ensures the date is valid according to month-specific day limits.
 */
class FiscalYearEndValidator : ConstraintValidator<ValidFiscalYearEnd, String?> {
    private val regex =
        Regex(
            pattern =
                "^(" +
                    "((0[1-9]|[12][0-9]|3[01])-(Jan|Mar|May|Jul|Aug|Oct|Dec))|" +
                    "((0[1-9]|[12][0-9]|30)-(Apr|Jun|Sep|Nov))|" +
                    "((0[1-9]|1[0-9]|2[0-8])-Feb)" +
                    ")$",
        )

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value == null) return true
        return regex.matches(value)
    }
}
