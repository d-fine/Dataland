package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.BasePortfolioName
import org.dataland.datalanduserservice.model.PortfolioMonitoringPatch
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
            portfolioId: String,
            correlationId: String,
        ): Boolean {
            val userId = DatalandAuthentication.fromContext().userId

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
        fun existsPortfolioWithNameForUser(
            portfolioName: String,
            correlationId: String,
        ): Boolean {
            val userId = DatalandAuthentication.fromContext().userId

            logger.info(
                "Check if portfolio with portfolioName: $portfolioName exists for user with userId: $userId." +
                    " CorrelationId: $correlationId.",
            )
            return portfolioRepository.existsByUserIdAndPortfolioName(userId, portfolioName)
        }

        /**
         * Retrieve all portfolios from repo for user
         */
        @Transactional(readOnly = true)
        fun getAllPortfoliosForUser(): List<BasePortfolio> {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            logger.info("Retrieve all portfolios for user with userId: $userId. CorrelationId: $correlationId.")
            return portfolioRepository.getAllByUserId(userId).map { it.toBasePortfolio() }
        }

        /**
         * Retrieve portfolio for user by portfolioId
         */
        @Transactional(readOnly = true)
        fun getPortfolioForUser(portfolioId: String): BasePortfolio {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            logger.info(
                "Retrieve portfolio with portfolioId: $portfolioId for user with userId: $userId." +
                    " CorrelationId: $correlationId.",
            )
            return portfolioRepository
                .getPortfolioByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
                ?.toBasePortfolio() ?: throw PortfolioNotFoundApiException(portfolioId)
        }

        /**
         * Creates a new portfolio.
         */
        @Transactional
        fun createPortfolio(
            portfolio: BasePortfolio,
            correlationId: String,
        ): BasePortfolio {
            logger.info(
                "Create new portfolio for user with userId: ${portfolio.userId}.CorrelationId: $correlationId.",
            )
            return portfolioRepository.save(portfolio.toPortfolioEntity()).toBasePortfolio()
        }

        /**
         * Replace an existing portfolio.
         */
        @Transactional
        fun replacePortfolio(
            portfolioId: String,
            portfolio: BasePortfolio,
            correlationId: String,
        ): BasePortfolio {
            logger.info(
                "Replace portfolio with portfolioId: $portfolioId for user with userId: ${portfolio.userId}." +
                    " CorrelationId: $correlationId.",
            )
            val originalPortfolio =
                portfolioRepository.getPortfolioByUserIdAndPortfolioId(portfolio.userId, UUID.fromString(portfolioId))
                    ?: throw PortfolioNotFoundApiException(portfolioId)
            return portfolioRepository
                .save(portfolio.toPortfolioEntity(portfolioId, originalPortfolio.creationTimestamp))
                .toBasePortfolio()
        }

        /**
         * Replace an existing portfolio.TEST
         */
        @Transactional
        fun replacePortfolioTEST(
            portfolioId: String,
            portfolioUpload: PortfolioUpload,
            correlationId: String,
        ): BasePortfolio {
            logger.info(
                "Replace portfolio with portfolioId: $portfolioId for user with userId: ${UUID.randomUUID()}." +
                    " CorrelationId: $correlationId.",
            )
            val originalPortfolio =
                portfolioRepository.getPortfolioByUserIdAndPortfolioId(UUID.randomUUID().toString(), UUID.fromString(portfolioId))
                    ?: throw PortfolioNotFoundApiException(portfolioId)

            val portfolio = BasePortfolio.keepMonitoringInvariant(originalPortfolio.toBasePortfolio(), portfolioUpload)

            return portfolioRepository
                .save(portfolio.toPortfolioEntity(portfolioId, originalPortfolio.creationTimestamp))
                .toBasePortfolio()
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
         * Retrieve portfolio names for user
         */
        @Transactional(readOnly = true)
        fun getAllPortfolioNamesForCurrentUser(): List<BasePortfolioName> =
            getAllPortfoliosForUser().map {
                BasePortfolioName(it.portfolioId, it.portfolioName)
            }

        /**
         * Patches the monitoring of an existing portfolio.
         */
        @Transactional
        fun patchMonitoring(
            portfolioId: String,
            patch: PortfolioMonitoringPatch,
        ) {
            val userId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()

            logger.info(
                "Patch monitoring for portfolio with portfolioId: $portfolioId for user with userId: $userId. " +
                    "CorrelationId: $correlationId. Patch: $patch",
            )

            val portfolioEntity =
                portfolioRepository.getPortfolioByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
                    ?: throw PortfolioNotFoundApiException(portfolioId)

            var updated = false

            patch.startingMonitoringPeriod?.let {
                portfolioEntity.startingMonitoringPeriod = it
                updated = true
            }

            patch.monitoredFrameworks?.let {
                portfolioEntity.monitoredFrameworks = it.toMutableSet()
                updated = true
            }

            if (updated) {
                portfolioEntity.lastUpdateTimestamp = System.currentTimeMillis()
                portfolioRepository.save(portfolioEntity)
                logger.info("Monitoring successfully patched for portfolioId: $portfolioId. CorrelationId: $correlationId.")
            } else {
                logger.info("No monitoring fields were updated for portfolioId: $portfolioId. CorrelationId: $correlationId.")
            }
        }
    }
