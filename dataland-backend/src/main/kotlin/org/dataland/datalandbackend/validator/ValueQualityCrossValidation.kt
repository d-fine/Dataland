package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.model.datapoints.interfaces.DataPointWithSource
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import kotlin.reflect.KClass

/**
 * Constraint Annotation for cross-validating fields "Value" and "Quality" of ExtendedDataPoints
 */
@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = [ValueQualityCrossValidator::class])
annotation class QualityAndValue(
    val message: String = "provided quality and value do not fit.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator Class containing logic for QualityAndValue Constraint Annotation
 */
class ValueQualityCrossValidator : ConstraintValidator<QualityAndValue, DataPointWithSource<*>> {
    /**
     * Currently, only checks if value is null. In this case, quality must either be null or NoDataFound
     */
    override fun isValid(
        dataPoint: DataPointWithSource<*>?,
        context: ConstraintValidatorContext?,
    ): Boolean =
        when {
            dataPoint?.quality == QualityOptions.NoDataFound && dataPoint?.value != null -> false
            dataPoint?.quality != QualityOptions.NoDataFound && dataPoint?.quality != null && dataPoint?.value == null -> false
            else -> true
        }
}
