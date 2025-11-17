package org.dataland.datalandaccountingservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.dataland.datalandaccountingservice.model.BilledRequestEntityId
import java.util.UUID

/**
 * Entity class for billed requests.
 */
@Entity
@Table(
    name = "billed_requests",
    indexes = [
        Index(name = "idx_billed_requests_billed_company_id", columnList = "billed_company_id"),
        Index(name = "idx_billed_requests_data_sourcing_id", columnList = "data_sourcing_id"),
    ],
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["billed_company_id", "requested_company_id", "requested_reporting_period", "requested_framework"]),
    ],
)
@IdClass(BilledRequestEntityId::class)
class BilledRequestEntity(
    @Id
    @Column(name = "billed_company_id")
    val billedCompanyId: UUID,
    @Id
    @Column(name = "data_sourcing_id")
    val dataSourcingId: UUID,
    @Column(name = "requested_company_id")
    val requestedCompanyId: UUID,
    @Column(name = "requested_reporting_period", nullable = false)
    val requestedReportingPeriod: String,
    @Column(name = "requested_framework", nullable = false)
    val requestedFramework: String,
    @Column(name = "timestamp", nullable = false)
    val timestamp: Long,
)
