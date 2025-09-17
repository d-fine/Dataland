package org.dataland.datasourcingservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import java.util.UUID

/**
 * The database entity for storing data sourcing objects.
 */
@Entity
@Table(name = "data_sourcing")
class DataSourcingEntity(
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID,
    @Column(name = "company_id")
    val companyId: UUID,
    @Column(name = "reporting_period")
    val reportingPeriod: String,
    @Column(name = "data_type")
    val dataType: String,
    @Column(name = "state")
    val state: DataSourcingState,
)
