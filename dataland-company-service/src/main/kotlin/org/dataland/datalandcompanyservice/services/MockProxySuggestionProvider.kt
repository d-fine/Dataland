package org.dataland.datalandcompanyservice.services

import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestion
import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestionSource
import org.dataland.datalandcompanyservice.model.ProxySearchCompany
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Mocked proxy suggestion provider that returns the same hardcoded suggestion for every input company.
 *
 * Company IDs differ between environments, so the hardcoded proxy company is configured via its LEI
 * (property dataland.company-service.mock.proxy-company-lei) and resolved to its Dataland company ID at request time.
 * This provider will be replaced by an AI-based implementation of [ProxySuggestionProvider].
 */
@Service
class MockProxySuggestionProvider(
    private val datalandCompanyService: DatalandCompanyService,
    @Value("\${dataland.company-service.mock.proxy-company-lei}") private val proxyCompanyLei: String,
) : ProxySuggestionProvider {
    companion object {
        const val MOCK_CONFIDENCE = 0.5
        const val MOCK_REASONING =
            "This is a mocked proxy suggestion. The same proxy company is suggested for every company."
        val MOCK_SOURCES =
            listOf(
                ProxySuggestionSource(
                    title = "Mocked proxy suggestion source",
                    url = "https://dataland.com",
                ),
            )
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun suggestProxy(company: ProxySearchCompany): ProxySuggestion {
        logger.info(
            "Returning mocked proxy suggestion for company '${company.companyName}' (${company.companyId}) " +
                "using proxy company with LEI $proxyCompanyLei.",
        )
        val proxyCompanyId =
            datalandCompanyService.findCompanyIdByLei(proxyCompanyLei)
                ?: throw misconfiguredProxyCompany()
        return ProxySuggestion(
            companyId = company.companyId,
            proxyCompanyId = proxyCompanyId,
            confidence = MOCK_CONFIDENCE,
            reasoning = MOCK_REASONING,
            sources = MOCK_SOURCES,
        )
    }

    /**
     * The configured mock proxy company not existing is a server-side misconfiguration the client cannot fix,
     * so it results in an internal server error rather than an invalid proxy suggestion.
     */
    private fun misconfiguredProxyCompany(): InternalServerErrorApiException {
        val internalMessage =
            "The mocked proxy company with LEI $proxyCompanyLei does not exist on Dataland. " +
                "Prepopulate the fake fixtures or set COMPANY_SERVICE_MOCK_PROXY_COMPANY_LEI " +
                "to the LEI of an existing company."
        logger.error(internalMessage)
        return InternalServerErrorApiException(internalMessage)
    }
}
