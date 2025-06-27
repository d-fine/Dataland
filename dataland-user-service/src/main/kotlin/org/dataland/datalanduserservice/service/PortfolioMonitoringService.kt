package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.repository.PortfolioRepository
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
        private val portfolioBulkDataRequestService: PortfolioBulkDataRequestService,
        private val portfolioRepository: PortfolioRepository,
    ) {
        private val logger = LoggerFactory.getLogger(PortfolioService::class.java)

        /**
         * Patches the monitoring of an existing portfolio.
         */
        @Transactional
        fun patchMonitoring(
            portfolioId: String,
            portfolio: BasePortfolio,
            correlationId: String,
        ): BasePortfolio {
            logger.info(
                "Patch monitoring status of portfolio with portfolioId: $portfolioId for user with userId: ${portfolio.userId}." +
                    " CorrelationId: $correlationId.",
            )

            val originalPortfolio =
                portfolioRepository
                    .getPortfolioByUserIdAndPortfolioId(portfolio.userId, UUID.fromString(portfolioId))
                    ?.toBasePortfolio()
                    ?: throw PortfolioNotFoundApiException(portfolioId)

            val updatedPortfolioEntity =
                originalPortfolio.toPortfolioEntity(
                    portfolioId,
                    originalPortfolio.creationTimestamp,
                    portfolio.lastUpdateTimestamp,
                    portfolio.isMonitored,
                    portfolio.startingMonitoringPeriod,
                    portfolio.monitoredFrameworks,
                )

            val updatedPortfolio = updatedPortfolioEntity.toBasePortfolio()

            portfolioBulkDataRequestService.publishBulkDataRequestMessageIfMonitored(updatedPortfolio)

            return portfolioRepository
                .save(updatedPortfolioEntity)
                .toBasePortfolio()
        }
    }
