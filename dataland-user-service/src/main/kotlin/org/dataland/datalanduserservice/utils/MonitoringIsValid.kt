package org.dataland.datalanduserservice.utils

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalanduserservice.model.PortfolioMonitoring
import kotlin.reflect.KClass

/**
 * Annotation for the validation of Portfolio Monitoring
 */
@Target(AnnotationTarget.CLASS)
@Constraint(
    validatedBy = [
        PortfolioMonitoringValidator::class,
    ],
)
annotation class MonitoringIsValid(
    val message: String = "This Monitoring Configuration is not valid.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for Portfolio Monitoring.
 */
class PortfolioMonitoringValidator : ConstraintValidator<MonitoringIsValid, PortfolioMonitoring> {
    override fun isValid(
        value: PortfolioMonitoring?,
        context: ConstraintValidatorContext,
    ): Boolean {
        val parametersNotSetIfMonitored = (
            value?.isMonitored == true &&
                (
                    value.monitoredFrameworks == emptySet<String>() || value.startingMonitoringPeriod.isNullOrEmpty()
                )
        )
        val parametersSetIfNotMonitored = (
            value?.isMonitored == false &&
                (
                    value.monitoredFrameworks != emptySet<String>() || !value.startingMonitoringPeriod.isNullOrEmpty()
                )
        )
        return !(parametersNotSetIfMonitored || parametersSetIfNotMonitored)
    }
}
