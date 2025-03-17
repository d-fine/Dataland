package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackend.utils.isGreaterOrEqual
import org.dataland.datalandbackend.utils.validateMinimumValueConstraint
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * Annotation for the minimum validation of a number field or a datapoint holding a number
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [FieldMinimumValidator::class, DataPointMinimumValidator::class])
annotation class MinimumValue(
    val minimumValue: Long = 0,
    val message: String =
        "Input validation failed: A base data point holding a number" +
            " is smaller than the set minimum.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the minimum validation logic for a field of a number type
 */
class FieldMinimumValidator : ConstraintValidator<MinimumValue, Number> {
    private var minimumValue by Delegates.notNull<Long>()

    override fun initialize(constraintAnnotation: MinimumValue) {
        this.minimumValue = constraintAnnotation.minimumValue
    }

    override fun isValid(
        value: Number?,
        context: ConstraintValidatorContext?,
    ): Boolean = isGreaterOrEqual(value, minimumValue)
}

/**
 * Class holding the minimum validation logic for a field of a datapoint type with number value
 */
class DataPointMinimumValidator : ConstraintValidator<MinimumValue, BaseDataPoint<*>> {
    private var minimumValue by Delegates.notNull<Long>()

    override fun initialize(constraintAnnotation: MinimumValue) {
        this.minimumValue = constraintAnnotation.minimumValue
    }

    override fun isValid(
        dataPoint: BaseDataPoint<*>?,
        context: ConstraintValidatorContext?,
    ): Boolean = validateMinimumValueConstraint(dataPoint, minimumValue)
}
