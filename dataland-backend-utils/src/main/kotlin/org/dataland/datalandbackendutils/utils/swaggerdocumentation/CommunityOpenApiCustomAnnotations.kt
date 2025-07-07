package org.dataland.datalandbackendutils.utils.swaggerdocumentation

import io.swagger.v3.oas.annotations.Parameter

/** * DataType annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
    required = false,
)
annotation class DataTypeParameterNonRequired

/** * UserId annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.USER_ID_DESCRIPTION,
    example = CommunityManagerOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
    required = false,
)
annotation class UserIdParameterNonRequired

/** * UserEmailAddress annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.USER_EMAIL_ADDRESS_DESCRIPTION,
    example = CommunityManagerOpenApiDescriptionsAndExamples.USER_EMAIL_ADDRESS_EXAMPLE,
    required = false,
)
annotation class UserEmailAddressParameterNonRequired

/** * AdminComment annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.ADMIN_COMMENT_DESCRIPTION,
    example = CommunityManagerOpenApiDescriptionsAndExamples.ADMIN_COMMENT_EXAMPLE,
    required = false,
)
annotation class AdminCommentParameterNonRequired

/** * RequestStatus annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.REQUEST_STATUS_DESCRIPTION,
    required = false,
)
annotation class RequestStatusParameterNonRequired

/** * AccessStatus annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.ACCESS_STATUS_DESCRIPTION,
    required = false,
)
annotation class AccessStatusParameterNonRequired

/** * RequestPriority annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.REQUEST_PRIORITY_DESCRIPTION,
    required = false,
)
annotation class RequestPriorityParameterNonRequired

/** * ReportingPeriod annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
    example = CommunityManagerOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    required = false,
)
annotation class ReportingPeriodParameterNonRequired

/** * DatalandCompanyId annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
    example = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    required = false,
)
annotation class CompanyIdParameterNonRequired

/** * DataRequestId annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.DATA_REQUEST_ID_DESCRIPTION,
    example = CommunityManagerOpenApiDescriptionsAndExamples.DATA_REQUEST_ID_EXAMPLE,
    required = true,
)
annotation class DataRequestIdParameterRequired

/** * CompanyId annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
    example = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    required = true,
)
annotation class CompanyIdParameterRequired

/** * CompanyRole annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ROLE_DESCRIPTION,
    required = true,
)
annotation class CompanyRoleParameterRequired

/** * UserId annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.USER_ID_DESCRIPTION,
    example = CommunityManagerOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
    required = true,
)
annotation class UserIdParameterRequired
