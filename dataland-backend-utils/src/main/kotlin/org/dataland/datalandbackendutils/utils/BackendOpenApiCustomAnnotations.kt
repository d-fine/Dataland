package org.dataland.datalandbackendutils.utils

import io.swagger.v3.oas.annotations.Parameter

/** * DataType annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = BackendOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
    required = false,
)
annotation class DataTypeParameter

/** * CountryCode annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = BackendOpenApiDescriptionsAndExamples.COUNTRY_CODE_DESCRIPTION,
    example = BackendOpenApiDescriptionsAndExamples.COUNTRY_CODE_EXAMPLE,
    required = false,
)
annotation class CountryCodeParameter

/** * Sector annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = BackendOpenApiDescriptionsAndExamples.SECTOR_DESCRIPTION,
    example = BackendOpenApiDescriptionsAndExamples.SECTOR_EXAMPLE,
    required = false,
)
annotation class SectorParameter

/** * CompanyId annotation */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    name = "companyId",
    description = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
    example = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    required = true,
)
annotation class CompanyIdParameter
