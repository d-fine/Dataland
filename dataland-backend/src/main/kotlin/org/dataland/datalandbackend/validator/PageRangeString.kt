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
    val message: String = """
Input validation failed: The page range format is invalid. The input must adhere to a specific structure where only 
positive integers without leading zeros are allowed. There are two valid formats:

1. **Single Page Format**: The input can be a single page number, represented by any positive integer. For example, 
entering '1' would represent a request for only page 1. No negative numbers, zero, or leading zeros (e.g., '01') are 
permitted. This format is useful when you need to refer to a single page.

2. **Page Range Format**: The input can specify a range of pages using two positive integers separated by a 
hyphen ('-'). For example, '2-5' would represent pages 2 through 5, including page 2 and page 5. In this format:
   - The first number must be smaller than or equal to the second number (i.e., '2-5' is valid, but '5-2' is not).
   - Both numbers must be positive integers, without leading zeros.
   - The range must be continuous, meaning you cannot input something like '2-5,7-10'. Only one range per input is 
   allowed.

Examples of valid inputs include:
- '1': This indicates a request for page 1 only.
- '2-4': This indicates a request for pages 2, 3, and 4.

Invalid examples include:
- '0': Page numbers cannot be zero.
- '-3': Negative page numbers are not allowed.
- '01': Leading zeros are not permitted.
- '5-3': The first number must be less than or equal to the second.
- '2-5,7-10': Multiple ranges are not allowed.
 
Please adjust your input to follow these guidelines and try again.
""",
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
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
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
