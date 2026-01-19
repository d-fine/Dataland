package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.model.DataType
import kotlin.reflect.KClass

/**
 * Annotation to validate that a data type is valid
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Constraint(
    validatedBy = [
        DataTypeValidator::class,
    ],
)
annotation class DataTypeIsValid(
    val message: String =
        "Input validation failed: Not a valid framework.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator to check that a data type is valid
 */
class DataTypeValidator : ConstraintValidator<DataTypeIsValid, String> {
    override fun isValid(
        dataType: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (dataType == null) return true

        return DataType.isDataType(dataType)
    }
}
