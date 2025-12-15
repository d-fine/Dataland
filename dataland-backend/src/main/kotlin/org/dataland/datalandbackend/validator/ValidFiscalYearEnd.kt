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
    // Low-complexity: only validates basic shape and allowed month token
    private val regex = Regex("""^(0[1-9]|[12]\d|3[01])-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)$""")

    private val maxDayByMonth =
        mapOf(
            "Jan" to 31, "Feb" to 28, "Mar" to 31, "Apr" to 30,
            "May" to 31, "Jun" to 30, "Jul" to 31, "Aug" to 31,
            "Sep" to 30, "Oct" to 31, "Nov" to 30, "Dec" to 31,
        )

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value == null) return true

        val match = regex.matchEntire(value) ?: return false
        val day = match.groupValues[1].toInt()
        val month = match.groupValues[2]

        val maxDay = maxDayByMonth[month] ?: return false
        return day in 1..maxDay
    }
}
