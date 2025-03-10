package org.dataland.datalanduserservice.controller

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalanduserservice.api.PortfolioApi
import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.Portfolio
import org.dataland.datalanduserservice.model.PortfolioResponse
import org.dataland.datalanduserservice.service.PortfolioService
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

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
        override fun getAllPortfoliosForCurrentUser(): ResponseEntity<List<PortfolioResponse>> {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            return ResponseEntity.ok(portfolioService.getAllPortfoliosForUser(userId, correlationId))
        }

        override fun getPortfolio(portfolioId: String): ResponseEntity<PortfolioResponse> {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            return ResponseEntity.ok(portfolioService.getPortfolioForUser(userId, portfolioId, correlationId))
        }

        override fun patchPortfolio(
            portfolioId: String,
            companyId: String,
        ): ResponseEntity<PortfolioResponse> {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()

            isCompanyIdValid(companyId)
            return ResponseEntity.ok(portfolioService.addCompany(userId, portfolioId, companyId, correlationId))
        }

        override fun createPortfolio(portfolio: Portfolio): ResponseEntity<PortfolioResponse> {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()

            portfolio.companyIds.forEach { isCompanyIdValid(it) }
            return ResponseEntity(portfolioService.createPortfolio(userId, portfolio, correlationId), HttpStatus.CREATED)
        }

        override fun replacePortfolio(
            portfolioId: String,
            portfolio: Portfolio,
        ): ResponseEntity<PortfolioResponse> {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            if (!portfolioService.existsPortfolioForUser(userId, portfolioId, correlationId)) {
                throw PortfolioNotFoundApiException(portfolioId, correlationId)
            }

            portfolio.companyIds.forEach { isCompanyIdValid(it) }
            return ResponseEntity.ok(portfolioService.replacePortfolio(userId, portfolio, portfolioId, correlationId))
        }

        override fun deletePortfolio(portfolioId: String): ResponseEntity<Unit> {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            return ResponseEntity(
                portfolioService.deletePortfolio(userId, portfolioId, correlationId),
                HttpStatus.NO_CONTENT,
            )
        }

        override fun removeCompanyFromPortfolio(
            portfolioId: String,
            companyId: String,
        ): ResponseEntity<Unit> {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            return ResponseEntity(
                portfolioService.removeCompanyFromPortfolio(userId, portfolioId, companyId, correlationId),
                HttpStatus.NO_CONTENT,
            )
        }

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
