package org.dataland.datalandbackendutils.utils

import io.swagger.v3.oas.annotations.Parameter

/** * DataType annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = BackendOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
    required = false,
)
annotation class DataTypeParameterNonRequired

/** * CountryCode annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = BackendOpenApiDescriptionsAndExamples.COUNTRY_CODE_DESCRIPTION,
    example = BackendOpenApiDescriptionsAndExamples.COUNTRY_CODE_EXAMPLE,
    required = false,
)
annotation class CountryCodeParameterNonRequired

/** * Sector annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = BackendOpenApiDescriptionsAndExamples.SECTOR_DESCRIPTION,
    example = BackendOpenApiDescriptionsAndExamples.SECTOR_EXAMPLE,
    required = false,
)
annotation class SectorParameterNonRequired

/** * CompanyId annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    name = "companyId",
    description = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
    example = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    required = true,
)
annotation class CompanyIdParameterRequired

/** * IdentifierType annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    name = "identifierType",
    description = BackendOpenApiDescriptionsAndExamples.IDENTIFIER_TYPE_DESCRIPTION,
    required = true,
)
annotation class IdentifierTypeParameterRequired

/** * Identifier annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    name = "identifier",
    description = BackendOpenApiDescriptionsAndExamples.SINGLE_IDENTIFIER_DESCRIPTION,
    example = BackendOpenApiDescriptionsAndExamples.SINGLE_IDENTIFIER_EXAMPLE,
    required = true,
)
annotation class IdentifierParameterRequired

/** * CompanyId annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
    example = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    required = false,
)
annotation class CompanyIdParameterNonRequired

/** * ReportingPeriod annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
    example = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    required = false,
)
annotation class ReportingPeriodParameterNonRequired

/** * DataId annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    name = "dataId",
    description = BackendOpenApiDescriptionsAndExamples.DATA_ID_DESCRIPTION,
    example = BackendOpenApiDescriptionsAndExamples.DATA_ID_EXAMPLE,
    required = true,
)
annotation class DataIdParameterRequired
