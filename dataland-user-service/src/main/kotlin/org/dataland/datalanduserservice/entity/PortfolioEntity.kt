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
    var lastUpdateTimestamp: Long,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_ids", joinColumns = [JoinColumn(name = "portfolio_id")])
    @Column(name = "company_ids")
    @OrderBy("asc")
    val companyIds: MutableSet<String>,
    var isMonitored: Boolean? = false,
    var startingMonitoringPeriod: String?,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "frameworks", joinColumns = [JoinColumn(name = "portfolio_id")])
    @Column(name = "frameworks")
    @OrderBy("asc")
    var monitoredFrameworks: MutableSet<String>?,
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
