package org.dataland.userservice.controller

import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.userservice.api.PortfolioApi
import org.dataland.userservice.exceptions.PortfolioNotFoundApiException
import org.dataland.userservice.model.Portfolio
import org.dataland.userservice.service.PortfolioService
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
        private val portfolioService: PortfolioService,
    ) : PortfolioApi {
        override fun getAllPortfoliosForCurrentUser(): ResponseEntity<List<Portfolio>> {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            return ResponseEntity.ok(portfolioService.getAllPortfoliosForUser(userId, correlationId))
        }

        override fun getPortfolio(portfolioId: String): ResponseEntity<Portfolio> {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            return ResponseEntity.ok(portfolioService.getPortfolioForUser(userId, portfolioId, correlationId))
        }

        override fun patchPortfolio(
            portfolioId: String,
            companyId: String,
        ): ResponseEntity<Portfolio> {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            return ResponseEntity.ok(portfolioService.addCompany(userId, portfolioId, companyId, correlationId))
        }

        override fun createPortfolio(portfolio: Portfolio): ResponseEntity<Portfolio> {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            return ResponseEntity(portfolioService.createPortfolio(userId, portfolio, correlationId), HttpStatus.CREATED)
        }

        override fun replacePortfolio(
            portfolioId: String,
            portfolio: Portfolio,
        ): ResponseEntity<Portfolio> {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            if (!portfolioService.existsPortfolioForUser(userId, portfolioId, correlationId)) {
                throw PortfolioNotFoundApiException(portfolioId, correlationId)
            }
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
    }
