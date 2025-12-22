package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.BasePortfolioName
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service to manage Sharing-related business logic
 */
@Service
class PortfolioSharingService
    @Autowired
    constructor(
        private val portfolioRepository: PortfolioRepository,
        private val portfolioService: PortfolioService,
    ) {
        private val logger = LoggerFactory.getLogger(PortfolioService::class.java)

        /**
         * Get all portfolios shared with the current user.
         * @return a list of BasePortfolio objects representing the shared portfolios
         */
        @Transactional(readOnly = true)
        fun getAllSharedPortfoliosForCurrentUser(): List<BasePortfolio> {
            val userId = DatalandAuthentication.fromContext().userId
            return portfolioRepository
                .findAllBySharedUserIdsContaining(userId)
                .map { it.toBasePortfolio() }
        }

        /**
         * Get the names and IDs of all portfolios shared with the current user.
         * @return a list of BasePortfolioName objects representing the shared portfolios
         */
        @Transactional(readOnly = true)
        fun getAllSharedPortfolioNamesForCurrentUser(): List<BasePortfolioName> =
            getAllSharedPortfoliosForCurrentUser()
                .map {
                    BasePortfolioName(it.portfolioId, it.portfolioName)
                }

        /**
         * Patch sharing settings for a portfolio.
         * @param portfolioId the ID of the portfolio to update
         * @param portfolio the BasePortfolio object containing the updated sharing settings
         * @param correlationId a unique identifier for tracking the request
         * @return the updated BasePortfolio object
         * @throws PortfolioNotFoundApiException if the portfolio with the given ID does not exist
         */
        @Transactional
        fun patchSharing(
            portfolioId: UUID,
            portfolio: BasePortfolio,
            correlationId: String,
        ): BasePortfolio {
            logger.info(
                "Patching sharing settings for portfolio with portfolioId: $portfolioId by user with userId: $portfolio.userId." +
                    " CorrelationId: $correlationId.",
            )

            val originalPortfolio = portfolioService.getPortfolio(portfolioId.toString())

            val updatedPortfolioEntity =
                originalPortfolio.toPortfolioEntity(
                    portfolioId = portfolioId.toString(),
                    creationTimestamp = originalPortfolio.creationTimestamp,
                    lastUpdateTimestamp = portfolio.lastUpdateTimestamp,
                    sharedUserIds = portfolio.sharedUserIds,
                )

            return portfolioRepository.save(updatedPortfolioEntity).toBasePortfolio()
        }

        /**
         * Delete the current user from the sharing list of a portfolio.
         * @param portfolioId the ID of the portfolio to update
         * @param correlationId a unique identifier for tracking the request
         * @throws PortfolioNotFoundApiException if the portfolio with the given ID does not exist
         */
        @Transactional
        fun deleteCurrentUserFromSharing(
            portfolioId: UUID,
            correlationId: String,
        ) {
            val userId = DatalandAuthentication.fromContext().userId
            logger.info(
                "Removing user with userId: $userId from sharing of portfolio with portfolioId: $portfolioId." +
                    " CorrelationId: $correlationId.",
            )

            val originalPortfolio = portfolioService.getPortfolio(portfolioId.toString())

            val updatedSharedUserIds =
                originalPortfolio.sharedUserIds.toMutableSet().apply {
                    remove(userId)
                }

            val updatedPortfolioEntity =
                originalPortfolio.toPortfolioEntity(
                    portfolioId = portfolioId.toString(),
                    creationTimestamp = originalPortfolio.creationTimestamp,
                    lastUpdateTimestamp = originalPortfolio.lastUpdateTimestamp, // should timestamp be updated or not?
                    sharedUserIds = updatedSharedUserIds,
                )

            portfolioRepository.save(updatedPortfolioEntity)
        }
    }
