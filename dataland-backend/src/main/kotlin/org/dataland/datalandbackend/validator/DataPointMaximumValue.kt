package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * Annotation for the validation of an ExtendedDataPoint<*> holding a number
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [DataPointMaximumValidator::class])
annotation class DataPointMaximumValue(
    val maximumValue: Long = 100,
    val message: String = "Input validation failed: A base data point holding a number is larger than the set maximum.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for a class that implements BaseDataPoint<*> Interface featuring a number
 */
class DataPointMaximumValidator : ConstraintValidator<DataPointMaximumValue, BaseDataPoint<*>> {
    private var maximumValue by Delegates.notNull<Long>()

    override fun initialize(constraintAnnotation: DataPointMaximumValue) {
        this.maximumValue = constraintAnnotation.maximumValue
    }

    override fun isValid(dataPoint: BaseDataPoint<*>?, context: ConstraintValidatorContext?): Boolean {
        return if (dataPoint == null) {
            true
        } else {
            when (val datapointValue = dataPoint.value) {
                null -> true
                is BigDecimal -> datapointValue <= BigDecimal.valueOf(maximumValue)
                is BigInteger -> datapointValue <= BigInteger.valueOf(maximumValue)
                is Long -> datapointValue <= maximumValue
                else -> throw InvalidInputApiException(
                    "This validator is used for a wrong type",
                    "Type $datapointValue inside BaseDataPoint is not handled by number validator",
                )
            }
        }
    }
}
