package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.PortfolioMonitoringPatch
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service to manage Monitoring-related business logic
 */
@Service
class PortfolioMonitoringService
    @Autowired
    constructor(
        private val portfolioRepository: PortfolioRepository,
    ) {
        private val logger = LoggerFactory.getLogger(PortfolioService::class.java)

        /**
         * Patches the monitoring of an existing portfolio.
         */
        @Transactional
        fun patchMonitoring(
            portfolioId: String,
            portfolioMonitoringPatch: PortfolioMonitoringPatch,
            correlationId: String,
        ): BasePortfolio {
            val userId = DatalandAuthentication.fromContext().userId
            logger.info(
                "Patch monitoring of portfolio with portfolioId: $portfolioId for user with userId: $userId." +
                    " CorrelationId: $correlationId.",
            )

            val originalPortfolio =
                portfolioRepository
                    .getPortfolioByUserIdAndPortfolioId(userId, UUID.fromString(portfolioId))
                    ?.toBasePortfolio()
                    ?: throw PortfolioNotFoundApiException(portfolioId)

            return portfolioRepository
                .save(
                    originalPortfolio.toPortfolioEntity(
                        portfolioId,
                        originalPortfolio.creationTimestamp,
                        System.currentTimeMillis(),
                        portfolioMonitoringPatch.isMonitored,
                        portfolioMonitoringPatch.startingMonitoringPeriod,
                        portfolioMonitoringPatch.monitoredFrameworks,
                    ),
                ).toBasePortfolio()
        }
    }
