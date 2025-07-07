package org.dataland.datalandcommunitymanager.utils

import io.swagger.v3.oas.annotations.Parameter

/** * DataType annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
    required = false,
)
annotation class DataTypeParameter

/** * UserId annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.USER_ID_DESCRIPTION,
    example = CommunityManagerOpenApiDescriptionsAndExamples.USER_ID_EXAMPLE,
    required = false,
)
annotation class UserIdParameter

/** * UserEmailAddress annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.USER_EMAIL_ADDRESS_DESCRIPTION,
    example = CommunityManagerOpenApiDescriptionsAndExamples.USER_EMAIL_ADDRESS_EXAMPLE,
    required = false,
)
annotation class UserEmailAddressParameter

/** * AdminComment annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.ADMIN_COMMENT_DESCRIPTION,
    example = CommunityManagerOpenApiDescriptionsAndExamples.ADMIN_COMMENT_EXAMPLE,
    required = false,
)
annotation class AdminCommentParameter

/** * RequestStatus annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.REQUEST_STATUS_DESCRIPTION,
    required = false,
)
annotation class RequestStatusParameter

/** * AccessStatus annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.ACCESS_STATUS_DESCRIPTION,
    required = false,
)
annotation class AccessStatusParameter

/** * RequestPriority annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.REQUEST_PRIORITY_DESCRIPTION,
    required = false,
)
annotation class RequestPriorityParameter

/** * ReportingPeriod annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
    example = CommunityManagerOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    required = false,
)
annotation class ReportingPeriodParameter

/** * DatalandCompanyId annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
    example = CommunityManagerOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    required = false,
)
annotation class DatalandCompanyIdParameter
