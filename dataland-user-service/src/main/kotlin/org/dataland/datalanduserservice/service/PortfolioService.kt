package org.dataland.datalanduserservice.service

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.Portfolio
import org.dataland.datalanduserservice.model.PortfolioResponse
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
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
        fun existsPortfolioForUser(portfolioId: String): Boolean {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()

            logger.info(
                "Check if portfolio with portfolioId: $portfolioId exists for user with userId: $userId." +
                    " CorrelationId: $correlationId.",
            )
            return portfolioRepository.existsByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
        }

        /**
         * Checks if a portfolio with the same name exists for user
         */
        @Transactional(readOnly = true)
        fun existsPortfolioWithNameForUser(portfolioName: String): Boolean {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()

            logger.info(
                "Check if portfolio with portfolioName: $portfolioName exists exists for user with userId: $userId." +
                    " CorrelationId: $correlationId.",
            )
            return portfolioRepository.existsByUserIdAndPortfolioName(userId, portfolioName)
        }

        /**
         * Retrieve all portfolios from repo for user
         */
        @Transactional(readOnly = true)
        fun getAllPortfoliosForUser(): List<PortfolioResponse> {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            logger.info("Retrieve all portfolios for user with userId: $userId. CorrelationId: $correlationId.")
            return portfolioRepository.getAllByUserId(userId).map { it.toPortfolioResponse() }
        }

        /**
         * Retrieve portfolio for user by portfolioId
         */
        @Transactional(readOnly = true)
        fun getPortfolioForUser(portfolioId: String): PortfolioResponse {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            logger.info("Retrieve portfolio with portfolioId: $portfolioId for user with userId: $userId. CorrelationId: $correlationId.")
            return portfolioRepository
                .getPortfolioByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
                ?.toPortfolioResponse() ?: throw PortfolioNotFoundApiException(portfolioId)
        }

        /**
         * Add a company to an existing portfolio
         */
        @Transactional
        fun addCompany(
            portfolioId: String,
            companyId: String,
        ): PortfolioResponse {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()

            logger.info(
                "Add company with companyId: $companyId to portfolio with portfolioId: $portfolioId for user" +
                    " with userId: $userId. CorrelationId: $correlationId.",
            )
            val portfolio =
                portfolioRepository.getPortfolioByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
                    ?: throw PortfolioNotFoundApiException(portfolioId)
            portfolio.companyIds.add(companyId)
            portfolio.lastUpdateTimestamp = Instant.now().toEpochMilli()
            return portfolio.toPortfolioResponse()
        }

        /**
         * Creates a new portfolio.
         */
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        fun createPortfolio(portfolio: Portfolio): PortfolioResponse {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()

            logger.info(
                "Create new portfolio for user with userId: $userId. CorrelationId: $correlationId.",
            )
            return this.savePortfolio(userId, portfolio)
        }

        /**
         * Replace an existing portfolio.
         */
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        fun replacePortfolio(
            portfolio: Portfolio,
            portfolioId: String,
        ): PortfolioResponse {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()

            logger.info(
                "Replace portfolio with portfolioId: $portfolioId for user with userId: $userId. CorrelationId: $correlationId.",
            )
            val originalPortfolio =
                portfolioRepository.getPortfolioByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
                    ?: throw PortfolioNotFoundApiException(portfolioId)
            return this.savePortfolio(
                userId,
                portfolio,
                portfolioId = portfolioId,
                creationTimestamp = originalPortfolio.creationTimestamp,
            )
        }

        /**
         * Remove a company from the portfolio entity
         */
        @Transactional
        fun removeCompanyFromPortfolio(
            portfolioId: String,
            companyId: String,
        ) {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            logger.info(
                "Remove company with companyId: $companyId from portfolio with portfolioId: $portfolioId for user" +
                    " with userId: $userId. CorrelationId: $correlationId.",
            )
            val portfolio =
                portfolioRepository.getPortfolioByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
                    ?: throw PortfolioNotFoundApiException(portfolioId)
            if (portfolio.companyIds.size == 1) {
                throw InvalidInputApiException(
                    summary = "Removing the last company is not permitted.",
                    message = "Removing the last company from a portfolio is not permitted.",
                )
            }
            portfolio.companyIds.remove(companyId)
            portfolio.lastUpdateTimestamp = Instant.now().toEpochMilli()
        }

        /**
         * Delete portfolio for user by portfolio Id
         */
        @Transactional
        fun deletePortfolio(portfolioId: String) {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
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
            creationTimestamp: Long? = null,
        ): PortfolioResponse {
            val portfolioEntity =
                PortfolioEntity(
                    portfolioId = portfolioId?.let { UUID.fromString(it) } ?: UUID.randomUUID(),
                    portfolioName = portfolio.portfolioName,
                    userId = userId,
                    creationTimestamp = creationTimestamp ?: Instant.now().toEpochMilli(),
                    lastUpdateTimestamp = Instant.now().toEpochMilli(),
                    companyIds = portfolio.companyIds.toMutableSet(),
                    dataTypes = portfolio.dataTypes.toMutableSet(),
                )
            return portfolioRepository.save(portfolioEntity).toPortfolioResponse()
        }
    }
