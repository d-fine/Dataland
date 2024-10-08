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
    val message: String =
        "Valid inputs are a positive number or a range of two ascending positive numbers separated " +
            "by '-'. The numbers must not begin with a zero. Valid examples are '2' or '13-15'.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator class for validating a page range in the form of "A-B" or a single page number.
 */
class PageRangeValidator : ConstraintValidator<PageRange, String> {
    companion object {
        private val regexPage = """^([1-9]\d*)(?:-([1-9]\d*))?$""".toRegex()
    }

    override fun initialize(constraintAnnotation: PageRange) {
        // No initialization needed
    }

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value == null) return true

        val matchResult = regexPage.matchEntire(value)
        return if (matchResult != null) {
            val (a, b) = matchResult.destructured
            if (b.isEmpty()) {
                true
            } else {
                val pageA = a.toInt()
                val pageB = b.toInt()
                pageA < pageB
            }
        } else {
            false
        }
    }
}
