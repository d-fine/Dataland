package org.dataland.datalanduserservice.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.dataland.datalanduserservice.model.ProxyCompanyReplacementUpload
import org.dataland.datalanduserservice.service.PortfolioService
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * Validator class to validate user input within controller.
 */
@Service
class Validator
    @Autowired
    constructor(
        private val companyDataControllerApi: CompanyDataControllerApi,
        private val portfolioService: PortfolioService,
    ) {
        /**
         * Validates an uploaded portfolio on creation (POST).
         * Checks that provided portfolio name is unique and that companyIds are valid.
         */
        fun validatePortfolioCreation(
            portfolioUpload: PortfolioUpload,
            correlationId: String,
        ) {
            if (portfolioService.existsPortfolioWithNameForUser(portfolioUpload.portfolioName, correlationId)) {
                throw ConflictApiException(
                    message = "Conflicting input detected.",
                    summary =
                        "Conflicting input detected for portfolio with portfolioName $portfolioUpload.portfolioName." +
                            " Please ensure that portfolio names are unique. CorrelationId: $correlationId",
                )
            }
            portfolioUpload.identifiers.forEach { isCompanyIdValid(it, correlationId) }
            validateProxyCompanyReplacements(portfolioUpload.identifiers, portfolioUpload.proxyCompanyReplacements, correlationId)
        }

        /**
         * Validates an uploaded portfolio on replacement (PUT).
         * Checks that portfolio to be replaced exists; checks that replacing portfolio has a unique name; checks that
         * companyIds are valid.
         */
        fun validatePortfolioReplacement(
            portfolioId: String,
            portfolioUpload: PortfolioUpload,
            correlationId: String,
        ) {
            val isAdmin = DatalandAuthentication.fromContext().roles.contains(DatalandRealmRole.ROLE_ADMIN)
            if (!portfolioService.existsPortfolioForUser(portfolioId, correlationId) && !isAdmin) {
                throw PortfolioNotFoundApiException(portfolioId)
            }
            val portfolioToBeReplaced = portfolioService.getPortfolio(portfolioId, correlationId)
            if (portfolioService.existsPortfolioWithNameForUser(portfolioUpload.portfolioName, correlationId) &&
                portfolioToBeReplaced.portfolioName != portfolioUpload.portfolioName
            ) {
                throw ConflictApiException(
                    message = "Conflicting input detected.",
                    summary =
                        "Conflicting input detected for portfolio with portfolioName $portfolioUpload.portfolioName." +
                            " Please ensure that portfolio names are unique. CorrelationId: $correlationId",
                )
            }
            portfolioUpload.identifiers.forEach { isCompanyIdValid(it, correlationId) }
            validateProxyCompanyReplacements(portfolioUpload.identifiers, portfolioUpload.proxyCompanyReplacements, correlationId)
        }

        /**
         * Validates the proxy company replacements of an uploaded portfolio.
         * Checks that each proxy company is contained in the portfolio's (validated) identifiers, that each proxied
         * company is a valid company, and that no proxied company is targeted by more than one replacement entry.
         * @param validCompanyIds the set of company IDs that are (or will be) contained in the portfolio
         * @param proxyCompanyReplacements the list of proxy company replacements to validate
         * @param correlationId the correlationId used for logging and error messages
         */
        fun validateProxyCompanyReplacements(
            validCompanyIds: Set<String>,
            proxyCompanyReplacements: List<ProxyCompanyReplacementUpload>,
            correlationId: String,
        ) {
            val duplicateProxiedCompanyIds =
                proxyCompanyReplacements
                    .groupingBy { it.proxiedCompanyId }
                    .eachCount()
                    .filter { it.value > 1 }
                    .keys

            if (duplicateProxiedCompanyIds.isNotEmpty()) {
                throw ConflictApiException(
                    message = "Conflicting input detected.",
                    summary =
                        "Conflicting input detected: the following companies are proxied by more than one entry: " +
                            "$duplicateProxiedCompanyIds. CorrelationId: $correlationId",
                )
            }

            proxyCompanyReplacements.forEach { proxyCompanyReplacement ->
                if (proxyCompanyReplacement.proxyCompanyId !in validCompanyIds) {
                    throw ResourceNotFoundApiException(
                        summary = "Proxy company with CompanyId ${proxyCompanyReplacement.proxyCompanyId} not found in portfolio.",
                        message =
                            "Proxy company with CompanyId ${proxyCompanyReplacement.proxyCompanyId} is not contained " +
                                "in the portfolio. CorrelationId: $correlationId",
                    )
                }
                isCompanyIdValid(proxyCompanyReplacement.proxiedCompanyId, correlationId)
            }
        }

        /**
         * Checks if passed companyId is valid by calling respective HEAD endpoint in backend companyDataController
         * @param companyId
         * @return returns true if companyId is valid
         */
        private fun isCompanyIdValid(
            companyId: String,
            correlationId: String,
        ): Boolean {
            try {
                companyDataControllerApi.isCompanyIdValid(companyId)
                return true
            } catch (exception: ClientException) {
                if (exception.statusCode == HttpStatus.NOT_FOUND.value()) {
                    throw ResourceNotFoundApiException(
                        summary = "Company with CompanyId $companyId not found.",
                        message = "Company with CompanyId $companyId not found. CorrelationId: $correlationId",
                    )
                } else {
                    throw exception
                }
            }
        }
    }
