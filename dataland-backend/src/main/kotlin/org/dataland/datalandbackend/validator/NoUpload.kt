package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Annotation for the validation that a uploaded data point is null
 */
@Target(AnnotationTarget.FIELD)
@Constraint(
    validatedBy = [
        NoUploadValidator::class,
    ],
)
annotation class NoUpload(
    val message: String = "Input validation failed: This field must not be uploaded and has to be 'null'.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class that implements isValid, and check whether the uploaded data point is null
 */
class NoUploadValidator : ConstraintValidator<NoUpload, Any> {
    override fun isValid(
        value: Any?,
        context: ConstraintValidatorContext?,
    ): Boolean = (value == null)
}
