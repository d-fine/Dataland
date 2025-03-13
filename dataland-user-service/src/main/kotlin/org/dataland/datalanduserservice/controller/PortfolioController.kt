package org.dataland.datalanduserservice.controller

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalanduserservice.api.PortfolioApi
import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.PortfolioPayload
import org.dataland.datalanduserservice.model.PortfolioResponse
import org.dataland.datalanduserservice.service.PortfolioService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * RestController for the Portfolio API
 */
@RestController
class PortfolioController
    @Autowired
    constructor(
        private val companyDataControllerApi: CompanyDataControllerApi,
        private val portfolioService: PortfolioService,
    ) : PortfolioApi {
        override fun getAllPortfoliosForCurrentUser(): ResponseEntity<List<PortfolioResponse>> =
            ResponseEntity.ok(portfolioService.getAllPortfoliosForUser())

        override fun getPortfolio(portfolioId: String): ResponseEntity<PortfolioResponse> =
            ResponseEntity.ok(portfolioService.getPortfolioForUser(portfolioId))

        override fun patchPortfolio(
            portfolioId: String,
            companyId: String,
        ): ResponseEntity<PortfolioResponse> {
            isCompanyIdValid(companyId)
            return ResponseEntity.ok(portfolioService.addCompany(portfolioId, companyId))
        }

        override fun createPortfolio(portfolio: PortfolioPayload): ResponseEntity<PortfolioResponse> {
            if (portfolioService.existsPortfolioWithNameForUser(portfolio.portfolioName)) {
                throw ConflictApiException(
                    message = "Conflicting input detected.",
                    summary =
                        "Conflicting input detected for portfolio with portfolioName ${portfolio.portfolioName}." +
                            " Please ensure that portfolio names are unique.",
                )
            }

            portfolio.companyIds.forEach { isCompanyIdValid(it) }
            return ResponseEntity(portfolioService.createPortfolio(portfolio), HttpStatus.CREATED)
        }

        override fun replacePortfolio(
            portfolioId: String,
            portfolio: PortfolioPayload,
        ): ResponseEntity<PortfolioResponse> {
            if (!portfolioService.existsPortfolioForUser(portfolioId)) {
                throw PortfolioNotFoundApiException(portfolioId)
            }
            if (portfolioService.existsPortfolioWithNameForUser(portfolio.portfolioName)) {
                throw ConflictApiException(
                    message = "Conflicting input detected.",
                    summary =
                        "Conflicting input detected for portfolio with portfolioName ${portfolio.portfolioName}." +
                            " Please ensure that portfolio names are unique.",
                )
            }

            portfolio.companyIds.forEach { isCompanyIdValid(it) }
            return ResponseEntity.ok(portfolioService.replacePortfolio(portfolio, portfolioId))
        }

        override fun deletePortfolio(portfolioId: String): ResponseEntity<Unit> =
            ResponseEntity(portfolioService.deletePortfolio(portfolioId), HttpStatus.NO_CONTENT)

        override fun removeCompanyFromPortfolio(
            portfolioId: String,
            companyId: String,
        ): ResponseEntity<Unit> = ResponseEntity(portfolioService.removeCompanyFromPortfolio(portfolioId, companyId), HttpStatus.NO_CONTENT)

        /**
         * Checks if passed companyId is valid by calling respective HEAD endpoint in backend companyDataController
         * @param companyId
         * @return returns true if companyId is valid
         */
        private fun isCompanyIdValid(companyId: String): Boolean {
            try {
                companyDataControllerApi.isCompanyIdValid(companyId)
                return true
            } catch (exception: ClientException) {
                if (exception.statusCode == HttpStatus.NOT_FOUND.value()) {
                    throw ResourceNotFoundApiException(
                        summary = "Company with CompanyId $companyId not found.",
                        message = "Company with CompanyId $companyId not found.",
                    )
                } else {
                    throw exception
                }
            }
        }
    }
