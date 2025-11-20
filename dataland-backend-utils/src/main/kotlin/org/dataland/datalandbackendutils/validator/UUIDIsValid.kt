package org.dataland.datalandbackendutils.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackendutils.utils.ValidationUtils
import kotlin.reflect.KClass

/**
 * Annotation to validate that a company exists on Dataland
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Constraint(
    validatedBy = [
        UUIDIsValidValidatorClass::class,
    ],
)
annotation class UUIDIsValid(
    val message: String = "Input validation failed: Not a valid UUID.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator to check that a company exists on Dataland
 */
class UUIDIsValidValidatorClass : ConstraintValidator<UUIDIsValid, String> {
    override fun isValid(
        companyId: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (companyId == null) return true
        return ValidationUtils.isUuid(companyId)
    }
}
