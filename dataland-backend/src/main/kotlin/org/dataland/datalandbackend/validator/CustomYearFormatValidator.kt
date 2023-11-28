package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Annotation class for validating the YYYY-MM-DD Year Format
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [CustomYearFormatValidator::class])
annotation class YearFormat(
    val message: String = "{javax.validation.constraints.NotBlank.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Documentation required!
 */
class CustomYearFormatValidator : ConstraintValidator<YearFormat, String> {

    override fun isValid(yearString: String?, context: ConstraintValidatorContext?): Boolean {
        val yearFormatRegEx = Regex(
            "^(?<year>\\d{4})(?<sep>[^\\w\\s])(?<month>1[0-2]|0[1-9])\\k<sep>(?<day>0[1-9]|" +
                "[12][0-9]|(11\\k<sep>|[^1][4-9]\\k<sep>)30|(1[02]\\k<sep>|[^1][13578]\\k<sep>)3[01])",
        )

        return if (yearString == null) {
            true
        } else {
            yearFormatRegEx.matches(yearString)
        }
    }
}
