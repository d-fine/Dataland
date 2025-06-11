package org.dataland.datalanduserservice.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.dataland.datalanduserservice.model.BasePortfolio
import java.util.UUID

/**
 *
 */
@Entity
@Table(
    name = "portfolios",
    uniqueConstraints = [
        UniqueConstraint(name = "unique_portfolio_name_per_user", columnNames = ["user_id", "portfolio_name"]),
    ],
)
data class PortfolioEntity(
    @Id
    @Column(name = "portfolio_id")
    val portfolioId: UUID,
    @Column(name = "portfolio_name")
    val portfolioName: String,
    @Column(name = "user_id")
    val userId: String,
    val creationTimestamp: Long,
    val lastUpdateTimestamp: Long,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_ids", joinColumns = [JoinColumn(name = "portfolio_id")])
    @Column(name = "company_ids")
    @OrderBy("asc")
    val companyIds: MutableSet<String>,
    val isMonitored: Boolean? = false,
    val startingMonitoringPeriod: String?,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "portfolio_monitored_frameworks", joinColumns = [JoinColumn(name = "portfolio_id")])
    @Column(name = "frameworks")
    @OrderBy("asc")
    val monitoredFrameworks: Set<String>?,
) {
    /**
     * create PortfolioResponse from entity
     */
    fun toBasePortfolio(): BasePortfolio =
        BasePortfolio(
            portfolioId.toString(),
            portfolioName,
            userId,
            creationTimestamp,
            lastUpdateTimestamp,
            companyIds,
            isMonitored,
            startingMonitoringPeriod,
            monitoredFrameworks,
        )
}
