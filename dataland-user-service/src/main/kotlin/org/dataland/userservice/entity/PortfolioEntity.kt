package org.dataland.userservice.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.dataland.userservice.model.Portfolio
import org.dataland.userservice.model.PortfolioResponse
import java.util.UUID

/**
 *
 */
@Entity
@Table(name = "portfolios")
data class PortfolioEntity(
    @Id
    @Column(name = "portfolio_id")
    val portfolioId: UUID,
    val portfolioName: String,
    val userId: String,
    val creationTimestamp: Long,
    val lastUpdateTimestamp: Long,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_ids", joinColumns = [JoinColumn(name = "portfolio_id")])
    @Column(name = "company_ids")
    @OrderBy("asc")
    val companyIds: Set<String>,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "data_types", joinColumns = [JoinColumn(name = "portfolio_id")])
    @Column(name = "data_types")
    @OrderBy("asc")
    val dataTypes: Set<String>,
) {
    /**
     * create PortfolioResponse from entity
     */
    fun toPortfolioResponse(): Portfolio =
        PortfolioResponse(
            portfolioId.toString(),
            portfolioName,
            userId,
            creationTimestamp,
            lastUpdateTimestamp,
            companyIds,
            dataTypes,
        )
}
