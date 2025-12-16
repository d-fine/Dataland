package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.time.Month
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
    private companion object {
        private const val MIN_DAY = 1
    }

    // Validates shape + month token
    private val regex =
        Regex("""^(0[1-9]|[12]\d|3[01])-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)$""")

    private val monthByToken: Map<String, Month> =
        mapOf(
            "Jan" to Month.JANUARY,
            "Feb" to Month.FEBRUARY,
            "Mar" to Month.MARCH,
            "Apr" to Month.APRIL,
            "May" to Month.MAY,
            "Jun" to Month.JUNE,
            "Jul" to Month.JULY,
            "Aug" to Month.AUGUST,
            "Sep" to Month.SEPTEMBER,
            "Oct" to Month.OCTOBER,
            "Nov" to Month.NOVEMBER,
            "Dec" to Month.DECEMBER,
        )

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean =
        value?.let { v ->
            regex.matchEntire(v)?.let { match ->
                val day = match.groupValues[1].toInt()
                val month = monthByToken[match.groupValues[2]]

                month != null && day >= MIN_DAY && day <= month.length(false) // false => non-leap => 29-Feb invalid
            } ?: false
        } ?: true
}
