package org.dataland.datalandproxyfinder.services

import org.dataland.datalandbackendutils.exceptions.InvalidProxySuggestionApiException
import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestion
import org.springframework.stereotype.Service

/**
 * Validates proxy suggestions before they are returned to the caller.
 */
@Service
class ProxySuggestionValidator(
    private val datalandCompanyService: DatalandCompanyService,
) {
    companion object {
        const val SELF_REFERENCE_SUMMARY = "Suggested proxy company is the company itself."
        const val UNKNOWN_PROXY_COMPANY_SUMMARY = "Suggested proxy company does not exist."
        const val COMPANY_MISMATCH_SUMMARY = "Proxy suggestion refers to a different company."
        const val INVALID_CONFIDENCE_SUMMARY = "Proxy suggestion has an invalid confidence."
        private const val MIN_CONFIDENCE = 0.0
        private const val MAX_CONFIDENCE = 1.0
    }

    /**
     * Validates that the suggestion belongs to the given company, that the suggested proxy company exists on Dataland
     * and that it is not the company itself.
     * @param companyId the company for which the proxy was searched
     * @param suggestion the suggestion to validate
     * @throws InvalidProxySuggestionApiException if any validation rule is violated
     */
    fun validate(
        companyId: String,
        suggestion: ProxySuggestion,
    ) {
        val (summary, message) = findViolation(companyId, suggestion) ?: return
        throw InvalidProxySuggestionApiException(summary = summary, message = message)
    }

    /**
     * Returns the summary and message of the first violated validation rule or null if the suggestion is valid.
     * The rules are checked in order, so the backend is only queried if all local checks pass.
     */
    private fun findViolation(
        companyId: String,
        suggestion: ProxySuggestion,
    ): Pair<String, String>? =
        when {
            !suggestion.companyId.equals(companyId, ignoreCase = true) ->
                COMPANY_MISMATCH_SUMMARY to
                    "The proxy suggestion refers to company ${suggestion.companyId} instead of $companyId."

            suggestion.proxyCompanyId.equals(companyId, ignoreCase = true) ->
                SELF_REFERENCE_SUMMARY to
                    "The suggested proxy company ${suggestion.proxyCompanyId} is the company itself."

            suggestion.confidence !in MIN_CONFIDENCE..MAX_CONFIDENCE ->
                INVALID_CONFIDENCE_SUMMARY to
                    "The confidence ${suggestion.confidence} is not between $MIN_CONFIDENCE and $MAX_CONFIDENCE."

            !datalandCompanyService.companyExists(suggestion.proxyCompanyId) ->
                UNKNOWN_PROXY_COMPANY_SUMMARY to
                    "The suggested proxy company ${suggestion.proxyCompanyId} does not exist on Dataland."

            else -> null
        }
}
