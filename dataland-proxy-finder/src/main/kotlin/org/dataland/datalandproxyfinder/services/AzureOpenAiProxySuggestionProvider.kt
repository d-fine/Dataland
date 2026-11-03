package org.dataland.datalandproxyfinder.services

import org.dataland.datalandbackendutils.exceptions.InvalidProxySuggestionApiException
import org.dataland.datalandbackendutils.exceptions.ServiceUnavailableApiException
import org.dataland.datalandbackendutils.exceptions.SingleApiException
import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestion
import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestionSource
import org.dataland.datalandproxyfinder.model.ProxySearchCompany
import org.dataland.datalandproxyfinder.model.ProxySearchLlmResult
import org.dataland.datalandproxyfinder.model.ProxySearchStatus
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

/**
 * Proxy suggestion provider that asks an LLM deployed in Azure OpenAI (Responses API) for the proxy company.
 * The LLM resolves companies to Dataland company IDs via [DatalandCompanyTools]. The returned suggestion is
 * validated by the caller.
 */
@Service
@ConditionalOnProperty(name = ["dataland.proxy-finder.provider"], havingValue = "azure")
class AzureOpenAiProxySuggestionProvider(
    private val proxySearchLlmClient: ProxySearchLlmClient,
) : ProxySuggestionProvider {
    companion object {
        const val NO_SUITABLE_PROXY_SUMMARY = "No suitable proxy company found."
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun suggestProxy(company: ProxySearchCompany): ProxySuggestion = mapToSuggestion(company, callLlm(company))

    @Suppress("TooGenericExceptionCaught") // any failure of the LLM call must be reported as 503
    private fun callLlm(company: ProxySearchCompany): ProxySearchLlmResult =
        try {
            proxySearchLlmClient.research(company.companyName)
        } catch (exception: SingleApiException) {
            throw exception
        } catch (exception: RuntimeException) {
            logger.error("Calling the LLM failed for company ${company.companyId}.", exception)
            throw unavailable("The AI service could not process the request.", exception)
        }

    private fun unavailable(
        reason: String,
        cause: Throwable? = null,
    ) = ServiceUnavailableApiException(
        summary = "The proxy search is currently unavailable.",
        message = "$reason Please try again later.",
        cause = cause,
    )

    private fun mapToSuggestion(
        company: ProxySearchCompany,
        result: ProxySearchLlmResult,
    ): ProxySuggestion {
        val proxyCompanyId = result.parentCompanyId
        if (result.status != ProxySearchStatus.FOUND || proxyCompanyId.isNullOrBlank()) {
            throw InvalidProxySuggestionApiException(
                summary = NO_SUITABLE_PROXY_SUMMARY,
                message = explainMissingProxy(company, result),
            )
        }
        return ProxySuggestion(
            companyId = company.companyId,
            proxyCompanyId = proxyCompanyId,
            confidence = (result.confidence ?: 0.0).coerceIn(0.0, 1.0),
            reasoning = result.reasoning.orEmpty(),
            sources = result.sources.map { ProxySuggestionSource(title = it.title, url = it.url) },
        )
    }

    /**
     * Builds a human-readable explanation why no proxy company could be suggested, including the reasoning of the LLM.
     */
    private fun explainMissingProxy(
        company: ProxySearchCompany,
        result: ProxySearchLlmResult,
    ): String {
        val candidates =
            result.candidates
                .takeIf { it.isNotEmpty() }
                ?.joinToString(prefix = " (candidates: ", postfix = ")")
                .orEmpty()
        val outcome =
            when (result.status) {
                ProxySearchStatus.SELF ->
                    "The AI considers \"${company.companyName}\" to be its own ultimate parent, so there is no proxy company."
                ProxySearchStatus.AMBIGUOUS ->
                    "The AI could not resolve \"${company.companyName}\" to a single legal entity$candidates."
                ProxySearchStatus.UNCLEAR ->
                    "The AI could not reliably determine the ultimate parent of \"${company.companyName}\" " +
                        "or the parent does not exist on Dataland."
                ProxySearchStatus.FOUND ->
                    "The AI identified \"${result.parentName.orEmpty()}\" as the ultimate parent of " +
                        "\"${company.companyName}\", but could not find it on Dataland."
            }
        val reasoning = if (result.reasoning.isNullOrBlank()) "" else " Reasoning of the AI: ${result.reasoning}"
        return outcome + reasoning
    }
}
