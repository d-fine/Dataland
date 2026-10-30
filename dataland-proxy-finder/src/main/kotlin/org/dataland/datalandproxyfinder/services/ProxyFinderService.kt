package org.dataland.datalandproxyfinder.services

import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestion
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Finds proxy companies by delegating to a [ProxySuggestionProvider] and validating its suggestion.
 */
@Service
class ProxyFinderService(
    private val datalandCompanyService: DatalandCompanyService,
    private val proxySuggestionProvider: ProxySuggestionProvider,
    private val proxySuggestionValidator: ProxySuggestionValidator,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Searches a proxy company for the given company.
     * @param companyId the Dataland company ID of the company for which a proxy is searched
     * @return a validated proxy suggestion
     */
    fun findProxy(companyId: String): ProxySuggestion {
        val company = datalandCompanyService.getCompany(companyId)
        val suggestion = proxySuggestionProvider.suggestProxy(company)
        proxySuggestionValidator.validate(company.companyId, suggestion)
        logger.info("Suggested proxy company ${suggestion.proxyCompanyId} for company $companyId.")
        return suggestion
    }
}
