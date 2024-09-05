package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Annotation for validating a page range as a string.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
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

    override fun initialize(constraintAnnotation: PageRange) {
        // No initialization needed
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return true // Consider null as valid

        val regexSinglePage = """^[1-9]\d*$""".toRegex()
        val regexPageRange = """^([1-9]\d*)-([1-9]\d*)$""".toRegex()

        return if (regexSinglePage.matches(value)) {
            // Check for a valid single page number
            val pageNumber = value.toInt()
            pageNumber >= 1 // Valid single page number must be >= 1
        } else {
            // Check for a range A-B with A < B
            val matchResult = regexPageRange.matchEntire(value)
            if (matchResult != null) {
                val (a, b) = matchResult.destructured
                val pageA = a.toInt()
                val pageB = b.toInt()
                pageA >= 1 && pageB >= 1 && pageA < pageB // A and B must be >= 1 and A < B
            } else {
                false // Invalid format
            }
        }
    }
}
