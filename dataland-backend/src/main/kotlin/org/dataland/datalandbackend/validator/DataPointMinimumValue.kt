package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import java.math.BigDecimal
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * Annotation for the validation of an ExtendedDataPoint<*> holding a number
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [DataPointMinimumValidator::class])
annotation class DataPointMinimumValue(
    val minimumValue: Long = 0,
    val message: String = "Input validation failed: A base data point holding a number" +
        " is smaller than the set minimum.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for a class that implements BaseDataPoint<*> Interface featuring a number
 */
class DataPointMinimumValidator : ConstraintValidator<DataPointMinimumValue, BaseDataPoint<*>> {
    private var minimumValue by Delegates.notNull<Long>()

    override fun initialize(constraintAnnotation: DataPointMinimumValue) {
        this.minimumValue = constraintAnnotation.minimumValue
    }

    override fun isValid(dataPoint: BaseDataPoint<*>?, context: ConstraintValidatorContext?): Boolean {
        return if (dataPoint == null) {
            true
        } else {
            when (dataPoint.value) {
                null -> true
                is BigDecimal -> dataPoint.value as BigDecimal >= BigDecimal.valueOf(minimumValue)
                is Long -> dataPoint.value as Long >= minimumValue
                else -> throw InvalidInputApiException(
                    "This validator is used for a wrong type",
                    "Type ${dataPoint.value} inside BaseDataPoint is not handled by number validator",
                )
            }
        }
    }
}
