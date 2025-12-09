package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

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

        fun getAllSharedPortfoliosForCurrentUser(): List<BasePortfolio> {
            val userId = DatalandAuthentication.fromContext().userId
            return portfolioRepository
                .findAllBySharedUserIdsContaining(userId)
                .map { it.toBasePortfolio() }
        }
    }
