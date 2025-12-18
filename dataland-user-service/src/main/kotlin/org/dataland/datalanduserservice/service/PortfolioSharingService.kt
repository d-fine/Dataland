package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.BasePortfolioName
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service to manage Sharing-related business logic
 */
@Service
class PortfolioSharingService
    @Autowired
    constructor(
        private val portfolioRepository: PortfolioRepository,
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
    }
