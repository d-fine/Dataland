package org.dataland.datalanduserservice.repository

import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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
    fun getAllByUserId(userId: String): List<PortfolioEntity>

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
    @Query(
        "SELECT p FROM PortfolioEntity p ORDER BY p.lastUpdateTimestamp DESC LIMIT :limit OFFSET :offset",
    )
    fun findAllWithPagination(
        @Param("limit") limit: Int,
        @Param("offset") offset: Int,
    ): List<PortfolioEntity>

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
