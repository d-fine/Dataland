package org.dataland.datalandproxyfinder.services

import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestion
import org.dataland.datalandproxyfinder.model.ProxySearchCompany

/**
 * Produces proxy suggestions for companies. Implementations may be mocked or backed by an AI model; the returned
 * suggestion is validated by the caller.
 */
interface ProxySuggestionProvider {
    /**
     * Suggests a proxy company for the given company.
     * @param company the company for which a proxy company is searched
     * @return the proxy suggestion
     */
    fun suggestProxy(company: ProxySearchCompany): ProxySuggestion
}
