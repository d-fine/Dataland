package org.dataland.datalanduserservice.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.converter.DataTypeEnumConverter
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
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "data_types", joinColumns = [JoinColumn(name = "portfolio_id")])
    @Column(name = "data_types")
    @OrderBy("asc")
    @Convert(converter = DataTypeEnumConverter::class)
    val frameworks: MutableSet<DataTypeEnum>,
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
            frameworks,
        )
}
