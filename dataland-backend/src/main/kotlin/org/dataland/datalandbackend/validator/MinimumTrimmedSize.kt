package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackendutils.exceptions.SEARCHSTRING_TOO_SHORT_VALIDATION_MESSAGE
import kotlin.reflect.KClass

/**
 * A custom validation annotation that checks the length of a string after trimming whitespace.
 *
 * Validates that the trimmed string's length is at least [min].
 *
 * @property min The minimum allowed length of the trimmed string.
 * @property message The error message to display if validation fails.
 * @property groups Allows specification of validation groups.
 * @property payload Can be used by clients to assign custom payload objects to a constraint.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [TrimmedSizeValidator::class])
annotation class MinimumTrimmedSize(
    val min: Int = 0,
    val message: String = "$SEARCHSTRING_TOO_SHORT_VALIDATION_MESSAGE: {min}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * A validator that implements the validation logic for the [MinimumTrimmedSize] annotation.
 *
 * Checks if the trimmed string length is at least [min].
 */
class TrimmedSizeValidator : ConstraintValidator<MinimumTrimmedSize, String?> {
    private var min: Int = 0

    /**
     * Initializes the validator with the annotation parameters.
     *
     * @param constraintAnnotation The [MinimumTrimmedSize] annotation instance.
     */
    override fun initialize(constraintAnnotation: MinimumTrimmedSize) {
        min = constraintAnnotation.min
    }

    /**
     * Validates the given string after trimming whitespace.
     *
     * @param value The string value to validate.
     * @param context The context in which the constraint is evaluated.
     * @return `true` if the trimmed string's length is within bounds or if the value is null or empty; `false` otherwise.
     */
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext,
    ): Boolean {
        if (value.isNullOrEmpty()) return true
        return value.trim().length >= min
    }
}
