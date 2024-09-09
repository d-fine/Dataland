package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Annotation for validating a page range as a string.
 */
@Target(AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PageRangeValidator::class])
annotation class PageRange(
    val message: String = "Input validation failed: Invalid page range format. Valid examples: '1', '2-5'",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator class for validating a page range in the form of "A-B" or a single page number.
 */
class PageRangeValidator : ConstraintValidator<PageRange, String> {

    override fun initialize(constraintAnnotation: PageRange) {
        // No initialization needed
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true // Consider null as valid

        val regexSinglePage = """^[1-9]\d*$""".toRegex()
        val regexPageRange = """^([1-9]\d*)-([1-9]\d*)$""".toRegex()

        return if (regexSinglePage.matches(value)) {
            true // Valid single page number
        } else {
            // Check for a range A-B with A < B
            val matchResult = regexPageRange.matchEntire(value)
            if (matchResult != null) {
                val (a, b) = matchResult.destructured
                val pageA = a.toInt()
                val pageB = b.toInt()
                pageA < pageB // A and B must be >= 1 and A < B
            } else {
                false // Invalid format
            }
        }
    }
}
