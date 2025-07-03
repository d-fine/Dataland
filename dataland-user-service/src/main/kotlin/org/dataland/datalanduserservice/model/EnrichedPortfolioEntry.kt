package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.UsersOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * API model for a single entry in the enriched portfolio
 */
data class EnrichedPortfolioEntry(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.COMPANY_NAME_EXAMPLE,
    )
    val companyName: String,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.SECTOR_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.SECTOR_EXAMPLE,
    )
    val sector: String?,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.COUNTRY_CODE_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.COUNTRY_CODE_EXAMPLE,
    )
    val countryCode: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UsersOpenApiDescriptionsAndExamples.COMPANY_COCKPIT_REF_DESCRIPTION,
        example = UsersOpenApiDescriptionsAndExamples.COMPANY_COCKPIT_REF_EXAMPLE,
    )
    val companyCockpitRef: String,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = UsersOpenApiDescriptionsAndExamples.FRAMEWORK_HYPHENATED_NAMES_TO_DATA_REF_DESCRIPTION,
        example = UsersOpenApiDescriptionsAndExamples.FRAMEWORK_HYPHENATED_NAMES_TO_DATA_REF_EXAMPLE,
    )
    val frameworkHyphenatedNamesToDataRef: Map<String, String?>,
    @field:JsonProperty(required = false)
    @field:Schema(
        description = UsersOpenApiDescriptionsAndExamples.AVAILABLE_REPORTING_PERIODS_DESCRIPTION,
        example = UsersOpenApiDescriptionsAndExamples.AVAILABLE_REPORTING_PERIODS_EXAMPLE,
    )
    val availableReportingPeriods: Map<String, String?>,
)
