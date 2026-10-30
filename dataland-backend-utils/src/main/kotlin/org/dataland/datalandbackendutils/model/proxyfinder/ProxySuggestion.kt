package org.dataland.datalandbackendutils.model.proxyfinder

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.ProxyFinderOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * A suggestion of a proxy company for a given company, as produced by the proxy finder.
 *
 * This model is the stable contract between the proxy finder and its consumers. It is independent of how the
 * suggestion is produced (mocked or AI-based).
 *
 * @param companyId the company for which a proxy was searched
 * @param proxyCompanyId the suggested proxy company
 * @param confidence the confidence in the suggestion, between 0 and 1
 * @param reasoning a human-readable explanation of the suggestion
 * @param sources the sources on which the suggestion is based
 */
data class ProxySuggestion(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = ProxyFinderOpenApiDescriptionsAndExamples.PROXY_SUGGESTION_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = ProxyFinderOpenApiDescriptionsAndExamples.PROXY_SUGGESTION_PROXY_COMPANY_ID_DESCRIPTION,
        example = ProxyFinderOpenApiDescriptionsAndExamples.PROXY_SUGGESTION_PROXY_COMPANY_ID_EXAMPLE,
    )
    val proxyCompanyId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = ProxyFinderOpenApiDescriptionsAndExamples.PROXY_SUGGESTION_CONFIDENCE_DESCRIPTION,
        example = ProxyFinderOpenApiDescriptionsAndExamples.PROXY_SUGGESTION_CONFIDENCE_EXAMPLE,
        minimum = "0",
        maximum = "1",
    )
    val confidence: Double,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = ProxyFinderOpenApiDescriptionsAndExamples.PROXY_SUGGESTION_REASONING_DESCRIPTION,
        example = ProxyFinderOpenApiDescriptionsAndExamples.PROXY_SUGGESTION_REASONING_EXAMPLE,
    )
    val reasoning: String,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema = Schema(description = ProxyFinderOpenApiDescriptionsAndExamples.PROXY_SUGGESTION_SOURCES_DESCRIPTION),
    )
    val sources: List<ProxySuggestionSource>,
)
