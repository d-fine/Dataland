package org.dataland.datalanduserservice.utils

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalanduserservice.model.PortfolioMonitoringPatch
import kotlin.reflect.KClass

/**
 * Annotation for the validation of Base- and ExtendedDataPoints holding an existing document
 */
@Target(AnnotationTarget.CLASS)
@Constraint(
    validatedBy = [
        MonitoringPatchValidator::class,
    ],
)
annotation class MonitoringPatchIsValid(
    val message: String = "This Monitoring Configuration is not valid.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for an BaseDocumentReference. It checks if the referenced document is valid
 */
class MonitoringPatchValidator : ConstraintValidator<MonitoringPatchIsValid, PortfolioMonitoringPatch> {
    override fun isValid(
        value: PortfolioMonitoringPatch?,
        context: ConstraintValidatorContext,
    ): Boolean {
        val parametersNotSetIfMonitored = (
            value?.isMonitored == true &&
                (
                    (
                        value.monitoredFrameworks == emptySet<String>() ||
                            value.startingMonitoringPeriod == null ||
                            value.startingMonitoringPeriod == ""
                    )
                )
        )
        val parametersSetIfNotMonitored = (
            value?.isMonitored == false &&
                (
                    (value.monitoredFrameworks != emptySet<String>() || !value.startingMonitoringPeriod.isNullOrEmpty())
                )
        )
        return !(parametersNotSetIfMonitored || parametersSetIfNotMonitored)
    }
}
