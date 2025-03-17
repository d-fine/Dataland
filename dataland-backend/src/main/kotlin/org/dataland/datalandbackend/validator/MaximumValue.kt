package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackend.utils.isLessOrEqual
import org.dataland.datalandbackend.utils.validateMaximumValueConstraint
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * Annotation for the maximum validation of a number field or a datapoint holding a number
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [FieldMaximumValidator::class, DataPointMaximumValidator::class])
annotation class MaximumValue(
    val maximumValue: Long = 100,
    val message: String = "Input validation failed: The value of the field is a larger number than the set maximum.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the maximum validation logic for a field of a number type
 */
class FieldMaximumValidator : ConstraintValidator<MaximumValue, Number> {
    private var maximumValue by Delegates.notNull<Long>()

    override fun initialize(constraintAnnotation: MaximumValue) {
        this.maximumValue = constraintAnnotation.maximumValue
    }

    override fun isValid(
        value: Number?,
        context: ConstraintValidatorContext?,
    ): Boolean = isLessOrEqual(value, maximumValue)
}

/**
 * Class holding the maximum validation logic for a field of a datapoint type with number value
 */
class DataPointMaximumValidator : ConstraintValidator<MaximumValue, BaseDataPoint<*>> {
    private var maximumValue by Delegates.notNull<Long>()

    override fun initialize(constraintAnnotation: MaximumValue) {
        this.maximumValue = constraintAnnotation.maximumValue
    }

    override fun isValid(
        dataPoint: BaseDataPoint<*>?,
        context: ConstraintValidatorContext?,
    ): Boolean = validateMaximumValueConstraint(dataPoint, maximumValue)
}
