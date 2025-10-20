package org.dataland.datalanduserservice.repository

import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Portfolio Repository
 */
@Repository
interface PortfolioRepository : JpaRepository<PortfolioEntity, String> {
    /**
     * Retrieve all portfolios for userId
     */
    fun getAllByUserIdOrderByCreationTimestampAsc(userId: String): List<PortfolioEntity>

    /**
     * Get specific portfolio by portfolioId
     */
    fun getPortfolioByPortfolioId(portfolioId: UUID): PortfolioEntity?

    /**
     * Get specific portfolio by portfolioId for userId
     */
    fun getPortfolioByUserIdAndPortfolioId(
        userId: String,
        portfolioId: UUID,
    ): PortfolioEntity?

    /**
     * Return a paginated chunk of all portfolios that exist on Dataland.
     */
    override fun findAll(pageable: Pageable): Page<PortfolioEntity>

    /**
     * Delete specific portfolio by portfolioId for userId
     */
    fun deleteByUserIdAndPortfolioId(
        userId: String,
        portfolioId: UUID,
    )

    /**
     * Checks if specific portfolio with portfolioId for userId exists
     */
    fun existsByUserIdAndPortfolioId(
        userId: String,
        portfolioId: UUID,
    ): Boolean

    /**
     * Checks if specific portfolio with portfolioName for userId exists
     */
    fun existsByUserIdAndPortfolioName(
        userId: String,
        portfolioName: String,
    ): Boolean
}
