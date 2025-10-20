package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.BasePortfolioName
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
        private val portfolioBulkDataRequestService: PortfolioBulkDataRequestService,
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
            return portfolioRepository.getAllByUserIdOrderByCreationTimestampAsc(userId).map { it.toBasePortfolio() }
        }

        /**
         * Retrieve portfolio by portfolioId. Unless the user calling this is an admin, it only returns
         * the portfolio if it belongs to the calling user.
         */
        @Transactional(readOnly = true)
        fun getPortfolio(portfolioId: String): BasePortfolio {
            val userId = DatalandAuthentication.fromContext().userId
            val userIsAdmin = DatalandAuthentication.fromContext().roles.contains(DatalandRealmRole.ROLE_ADMIN)
            val correlationId = UUID.randomUUID().toString()
            logger.info(
                "Retrieve portfolio with portfolioId: $portfolioId for user with userId: $userId." +
                    " CorrelationId: $correlationId.",
            )
            return if (userIsAdmin) {
                portfolioRepository
                    .getPortfolioByPortfolioId(UUID.fromString(portfolioId))
                    ?.toBasePortfolio() ?: throw PortfolioNotFoundApiException(portfolioId)
            } else {
                portfolioRepository
                    .getPortfolioByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
                    ?.toBasePortfolio() ?: throw PortfolioNotFoundApiException(portfolioId)
            }
        }

        /**
         * Retrieve all portfolios for a user specified by his or her userId. Should only be called
         * by admin users.
         */
        @Transactional(readOnly = true)
        fun getAllPortfoliosForUserById(userId: String): List<BasePortfolio> {
            val adminId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            logger.info(
                "By order of admin with userId $adminId, retrieve all portfolios for user with userId: $userId." +
                    " CorrelationId: $correlationId.",
            )
            return portfolioRepository.getAllByUserIdOrderByCreationTimestampAsc(userId).map { it.toBasePortfolio() }
        }

        /**
         * Retrieve a paginated list of all portfolios that exist on Dataland. Should only be called
         * by admin users.
         */
        @Transactional(readOnly = true)
        fun getAllPortfolios(
            chunkSize: Int,
            chunkIndex: Int,
        ): List<BasePortfolio> {
            val adminId = DatalandAuthentication.fromContext().userId
            val correlationId = UUID.randomUUID().toString()
            logger.info(
                "By order of admin with userId $adminId, retrieve the chunk with index $chunkIndex and size " +
                    "up to $chunkSize of all portfolios on Dataland. CorrelationId: $correlationId.",
            )
            return portfolioRepository
                .findAll(
                    PageRequest.of(chunkIndex, chunkSize, Sort.by("lastUpdateTimestamp").descending()),
                ).content
                .map { it.toBasePortfolio() }
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

            portfolioBulkDataRequestService.postBulkDataRequestMessageIfMonitored(portfolio)

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

            val updatedPortfolioEntity =
                portfolio.toPortfolioEntity(
                    portfolioId,
                    originalPortfolio.creationTimestamp,
                    portfolio.lastUpdateTimestamp,
                    portfolio.isMonitored,
                    portfolio.startingMonitoringPeriod,
                    portfolio.monitoredFrameworks,
                )

            portfolioBulkDataRequestService.postBulkDataRequestMessageIfMonitored(updatedPortfolioEntity.toBasePortfolio())

            return portfolioRepository.save(updatedPortfolioEntity).toBasePortfolio()
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
    }
