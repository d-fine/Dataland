package org.dataland.datalandbackendutils.model.proxyfinder

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.ProxyFinderOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * A single source backing a proxy suggestion.
 *
 * @param title a short human-readable title of the source
 * @param url the URL under which the source can be found
 */
data class ProxySuggestionSource(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = ProxyFinderOpenApiDescriptionsAndExamples.PROXY_SUGGESTION_SOURCE_TITLE_DESCRIPTION,
        example = ProxyFinderOpenApiDescriptionsAndExamples.PROXY_SUGGESTION_SOURCE_TITLE_EXAMPLE,
    )
    val title: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = ProxyFinderOpenApiDescriptionsAndExamples.PROXY_SUGGESTION_SOURCE_URL_DESCRIPTION,
        example = ProxyFinderOpenApiDescriptionsAndExamples.PROXY_SUGGESTION_SOURCE_URL_EXAMPLE,
    )
    val url: String,
)
