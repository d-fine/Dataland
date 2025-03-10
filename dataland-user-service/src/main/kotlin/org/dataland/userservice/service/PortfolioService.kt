package org.dataland.userservice.service

import org.dataland.userservice.entity.PortfolioEntity
import org.dataland.userservice.exceptions.PortfolioNotFoundApiException
import org.dataland.userservice.model.Portfolio
import org.dataland.userservice.repository.PortfolioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
        private val logger = LoggerFactory.getLogger(PortfolioService::class.java)

        /**
         * Checks if the portfolio belongs to the user
         */
        @Transactional(readOnly = true)
        fun existsPortfolioForUser(
            userId: String,
            portfolioId: String,
            correlationId: String,
        ): Boolean {
            logger.info(
                "Check if portfolio with portfolioId: $portfolioId exists for user with userId: $userId." +
                    " CorrelationId: $correlationId.",
            )
            return portfolioRepository.existsByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
        }

        /**
         * Retrieve all portfolios from repo for user
         */
        @Transactional(readOnly = true)
        fun getAllPortfoliosForUser(
            userId: String,
            correlationId: String,
        ): List<Portfolio> {
            logger.info("Retrieve all portfolios for user with userId: $userId. CorrelationId: $correlationId.")
            return portfolioRepository.getAllByUserId(userId).map { it.toPortfolioResponse() }
        }

        /**
         * Retrieve portfolio for user by portfolioId
         */
        @Transactional(readOnly = true)
        fun getPortfolioForUser(
            userId: String,
            portfolioId: String,
            correlationId: String,
        ): Portfolio {
            logger.info("Retrieve portfolio with portfolioId: $portfolioId for user with userId: $userId. CorrelationId: $correlationId.")
            return portfolioRepository
                .getPortfolioByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
                ?.toPortfolioResponse() ?: throw PortfolioNotFoundApiException(portfolioId, correlationId)
        }

        /**
         * Add a company to an existing portfolio
         */
        @Transactional
        fun addCompany(
            userId: String,
            portfolioId: String,
            companyId: String,
            correlationId: String,
        ): Portfolio {
            logger.info(
                "Add company with companyId: $companyId to portfolio with portfolioId: $portfolioId for user" +
                    " with userId: $userId. CorrelationId: $correlationId.",
            )
            val portfolio =
                portfolioRepository.getPortfolioByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
                    ?: throw PortfolioNotFoundApiException(portfolioId, correlationId)
            portfolio.companyIds.add(companyId)
            return portfolio.toPortfolioResponse()
        }

        /**
         * Creates a new portfolio.
         */
        @Transactional
        fun createPortfolio(
            userId: String,
            portfolio: Portfolio,
            correlationId: String,
        ): Portfolio {
            logger.info(
                "Create new portfolio for user with userId: $userId. CorrelationId: $correlationId.",
            )
            return this.savePortfolio(userId, portfolio)
        }

        /**
         * Replace an existing portfolio.
         */
        @Transactional
        fun replacePortfolio(
            userId: String,
            portfolio: Portfolio,
            portfolioId: String,
            correlationId: String,
        ): Portfolio {
            logger.info(
                "Replace portfolio with portfolioId: $portfolioId for user with userId: $userId. CorrelationId: $correlationId.",
            )
            return this.savePortfolio(
                userId,
                portfolio,
                portfolioId = portfolioId,
                lastUpdateTimestamp = Instant.now().toEpochMilli(),
            )
        }

        /**
         * Remove a company from the portfolio entity
         */
        @Transactional
        fun removeCompanyFromPortfolio(
            userId: String,
            portfolioId: String,
            companyId: String,
            correlationId: String,
        ) {
            logger.info(
                "Remove company with companyId: $companyId from portfolio with portfolioId: $portfolioId for user" +
                    " with userId: $userId. CorrelationId: $correlationId.",
            )
            val portfolio =
                portfolioRepository.getPortfolioByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
                    ?: throw PortfolioNotFoundApiException(portfolioId, correlationId)
            portfolio.companyIds.remove(companyId)
        }

        /**
         * Delete portfolio for user by portfolio Id
         */
        @Transactional
        fun deletePortfolio(
            userId: String,
            portfolioId: String,
            correlationId: String,
        ) {
            logger.info(
                "Delete portfolio with portfolioId: $portfolioId for user with userId: $userId. CorrelationId: $correlationId.",
            )
            return portfolioRepository.deleteByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
        }

        /**
         * Save portfolio entity in repository; used for creation (POST) and replacing (PUT)
         */
        private fun savePortfolio(
            userId: String,
            portfolio: Portfolio,
            portfolioId: String? = null,
            lastUpdateTimestamp: Long? = null,
        ): Portfolio =
            portfolioRepository
                .save(
                    PortfolioEntity(
                        portfolioId =
                            UUID.fromString(portfolioId)
                                ?: UUID.randomUUID(),
                        // TODO testing that this works with null input
                        portfolioName = portfolio.portfolioName,
                        userId = userId,
                        creationTimestamp = Instant.now().toEpochMilli(),
                        lastUpdateTimestamp = lastUpdateTimestamp ?: Instant.now().toEpochMilli(),
                        companyIds = portfolio.companyIds.toMutableSet(),
                        dataTypes = portfolio.dataTypes.toMutableSet(),
                    ),
                ).toPortfolioResponse()
    }
