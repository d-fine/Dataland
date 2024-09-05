package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Annotation for validating a page range as a string.
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [PageRangeValidator::class])
annotation class PageRange(
    val message: String = "Input validation failed: Invalid page range format.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator class for validating a page range in the form of "A-B" or a single page number.
 */
class PageRangeValidator : ConstraintValidator<PageRange, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        // Allow nulls, delegate to @NotNull if needed
        if (value == null) return true

        // Regular expression to match a single page number or a range A-B
        val regexSinglePage = """^\d+$""".toRegex()
        val regexPageRange = """^(\d+)-(\d+)$""".toRegex()

        // Initialize a result variable
        var isValid = false

        // Check for a single page number
        if (regexSinglePage.matches(value)) {
            val pageNumber = value.toInt()
            isValid = pageNumber >= 1 // Valid if single page number > 0
        } else {
            // Check for a range A-B
            val matchResult = regexPageRange.matchEntire(value)
            if (matchResult != null) {
                val (a, b) = matchResult.destructured
                val pageA = a.toInt()
                val pageB = b.toInt()
                // Validate the range
                isValid = pageA >= 1 && pageB >= 1 && pageA < pageB
            }
        }

        return isValid
    }
}
