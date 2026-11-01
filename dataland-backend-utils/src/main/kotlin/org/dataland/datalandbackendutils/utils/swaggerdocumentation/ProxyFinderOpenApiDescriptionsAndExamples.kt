package org.dataland.datalandbackendutils.utils.swaggerdocumentation

object ProxyFinderOpenApiDescriptionsAndExamples {
    const val PROXY_SUGGESTION_COMPANY_ID_DESCRIPTION =
        "The Dataland company ID of the company for which a proxy company was searched."

    const val PROXY_SUGGESTION_PROXY_COMPANY_ID_DESCRIPTION =
        "The Dataland company ID of the suggested proxy company, whose data may be used as a proxy for the company."
    const val PROXY_SUGGESTION_PROXY_COMPANY_ID_EXAMPLE = "f1fda7ba-4bd6-4f4c-9d6c-6d3b3a4e0c2a"

    const val PROXY_SUGGESTION_CONFIDENCE_DESCRIPTION =
        "The confidence of the proxy finder in the suggestion, as a value between 0 (no confidence) and 1 (full confidence)."
    const val PROXY_SUGGESTION_CONFIDENCE_EXAMPLE = "0.85"

    const val PROXY_SUGGESTION_REASONING_DESCRIPTION =
        "A human-readable explanation of why the proxy company was suggested."
    const val PROXY_SUGGESTION_REASONING_EXAMPLE =
        "The suggested company is the parent company and publishes consolidated sustainability reports."

    const val PROXY_SUGGESTION_SOURCES_DESCRIPTION = "The sources on which the suggestion is based."

    const val PROXY_SUGGESTION_SOURCE_TITLE_DESCRIPTION = "A short human-readable title of the source."
    const val PROXY_SUGGESTION_SOURCE_TITLE_EXAMPLE = "Annual Report 2024"

    const val PROXY_SUGGESTION_SOURCE_URL_DESCRIPTION = "The URL under which the source can be found."
    const val PROXY_SUGGESTION_SOURCE_URL_EXAMPLE = "https://example.com/annual-report-2024.pdf"
}
