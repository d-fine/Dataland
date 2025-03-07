package org.dataland.userservice.service

import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.userservice.entity.PortfolioEntity
import org.dataland.userservice.model.Portfolio
import org.dataland.userservice.repository.PortfolioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/**
 * Service to manage Portfolio-related business logic
 */
@Service
class PortfolioService
    @Autowired
    constructor(
        private val portfolioRepository: PortfolioRepository,
    ) {
        /**
         * Retrieve all portfolios from repo for user
         */
        fun getAllPorfoliosForUser(userId: String): List<Portfolio> =
            portfolioRepository.getAllByUserId(userId).map { it.toPortfolioResponse() }

        /**
         * Retrieve portfolio for user by portfolioId
         */
        fun getPortfolioForUser(
            userId: String,
            portfolioId: String,
        ): Portfolio =
            portfolioRepository
                .getPortfolioByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
                .toPortfolioResponse()

        /**
         * Save portfolio entity in repository
         */
        fun savePortfolio(portfolio: Portfolio): Portfolio {
            val userId = DatalandAuthentication.fromContext().userId
            val portfolioId = UUID.randomUUID()
            val timestamp = Instant.now().toEpochMilli()
            return portfolioRepository
                .save(
                    PortfolioEntity(
                        portfolioId = portfolioId,
                        portfolioName = portfolio.portfolioName,
                        userId = userId,
                        creationTimestamp = timestamp,
                        lastUpdateTimestamp = timestamp,
                        companyIds = portfolio.companyIds,
                        dataTypes = portfolio.dataTypes,
                    ),
                ).toPortfolioResponse()
        }
    }
