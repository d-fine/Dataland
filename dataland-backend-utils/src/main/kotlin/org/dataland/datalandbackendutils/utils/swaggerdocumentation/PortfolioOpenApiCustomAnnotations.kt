package org.dataland.datalandbackendutils.utils.swaggerdocumentation

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Boolean flag that indicates whether the portfolio is monitored.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Schema(
    description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_IS_MONITORED_DESCRIPTION,
    example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_IS_MONITORED_EXAMPLE,
)
annotation class PortfolioIsMonitored

/**
 * The reporting period from which the companies in the portfolio are actively monitored for data updates.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Schema(
    description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_STARTING_MONITORING_PERIOD_DESCRIPTION,
    example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_STARTING_MONITORING_PERIOD_EXAMPLE,
)
annotation class PortfolioStartingMonitoringPeriod

/**
 * A list of frameworks for which the companies in the portfolio are actively monitored.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@ArraySchema(
    arraySchema =
        Schema(
            type = "string",
            description = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_MONITORED_FRAMEWORKS_DESCRIPTION,
            example = UsersOpenApiDescriptionsAndExamples.PORTFOLIO_MONITORED_FRAMEWORKS_EXAMPLE,
        ),
)
annotation class PortfolioMonitoredFrameworks
