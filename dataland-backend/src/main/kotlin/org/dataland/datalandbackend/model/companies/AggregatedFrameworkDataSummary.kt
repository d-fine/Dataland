package org.dataland.datalandbackend.model.companies

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.utils.CompanyControllerDescriptionsAndExamples

/**
 * --- API model ---
 * A class that stores aggregated information on the datasets of a company of a specific data type
 */
data class AggregatedFrameworkDataSummary(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.NUMBER_OF_PROVIDED_REPORTING_PERIODS_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.NUMBER_OF_PROVIDED_REPORTING_PERIODS_EXAMPLE,
    )
    val numberOfProvidedReportingPeriods: Long,
)
