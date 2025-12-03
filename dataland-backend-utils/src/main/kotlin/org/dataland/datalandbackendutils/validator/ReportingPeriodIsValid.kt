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
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Constraint(
    validatedBy = [
        ReportingPeriodValidator::class,
    ],
)
annotation class ReportingPeriodIsValid(
    val message: String =
        "Input validation failed: Not a valid reporting period format. " +
            "The reporting period must be in the format YYYY.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator to check that a company exists on Dataland
 */
class ReportingPeriodValidator : ConstraintValidator<ReportingPeriodIsValid, String> {
    override fun isValid(
        reportingPeriod: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (reportingPeriod == null) return true

        return ValidationUtils.isReportingPeriod(reportingPeriod)
    }
}
