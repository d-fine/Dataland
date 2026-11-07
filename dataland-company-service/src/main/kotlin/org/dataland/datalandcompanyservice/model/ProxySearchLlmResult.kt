package org.dataland.datalandcompanyservice.model

/**
 * The outcome of the proxy research as reported by the LLM.
 */
enum class ProxySearchStatus {
    FOUND,
    SELF,
    AMBIGUOUS,
    UNCLEAR,
}

/**
 * A source cited by the LLM.
 * @param title a short title of the source
 * @param url the URL of the source
 * @param type the kind of source, e.g. official_filing, company_website, register or third_party
 */
data class ProxySearchLlmSource(
    val title: String,
    val url: String,
    val type: String? = null,
)

/**
 * Internal structured output of the LLM. It is mapped to the public ProxySuggestion by the provider.
 * @param status the outcome of the research
 * @param parentName the official legal name of the identified parent
 * @param parentCompanyId the Dataland company ID of the parent, as resolved via the Dataland tools
 * @param confidence the confidence in the result, between 0 and 1
 * @param reasoning a human-readable explanation
 * @param sources the sources supporting the conclusion
 * @param candidates candidate entities, filled if the status is AMBIGUOUS
 */
data class ProxySearchLlmResult(
    val status: ProxySearchStatus,
    val parentName: String? = null,
    val parentCompanyId: String? = null,
    val confidence: Double? = null,
    val reasoning: String? = null,
    val sources: List<ProxySearchLlmSource> = emptyList(),
    val candidates: List<String> = emptyList(),
)

/**
 * A company on Dataland as returned to the LLM by the tool functions.
 */
data class DatalandCompanyCandidate(
    val companyId: String,
    val companyName: String,
)
