package org.dataland.userservice.controller

import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.userservice.api.PortfolioApi
import org.dataland.userservice.model.Portfolio
import org.dataland.userservice.service.PortfolioService
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
        private val portfolioService: PortfolioService,
    ) : PortfolioApi {
        override fun getAllPortfoliosForCurrentUser(): ResponseEntity<List<Portfolio>> {
            val userId = DatalandAuthentication.fromContext().userId
            return ResponseEntity.ok(portfolioService.getAllPorfoliosForUser(userId))
        }

        override fun getPortfolio(portfolioId: String): ResponseEntity<Portfolio> {
            val userId = DatalandAuthentication.fromContext().userId
            return ResponseEntity.ok(portfolioService.getPortfolioForUser(userId, portfolioId))
        }

        override fun patchPortfolio(
            portfolioId: String,
            companyId: String,
        ): ResponseEntity<Portfolio> {
            TODO("Not yet implemented")
        }

        override fun postPortfolio(portfolio: Portfolio): ResponseEntity<Portfolio> =
            ResponseEntity(portfolioService.savePortfolio(portfolio), HttpStatus.CREATED)

        override fun replacePortfolio(
            portfolioId: String,
            portfolio: Portfolio,
        ): ResponseEntity<Portfolio> {
            TODO("Not yet implemented")
        }

        override fun deletePortfolio(portfolioId: String): ResponseEntity<Unit> {
            TODO("Not yet implemented")
        }

        override fun removeCompanyFromPortfolio(
            portfolioId: String,
            companyId: String,
        ): ResponseEntity<Unit> {
            TODO("Not yet implemented")
        }
    }
