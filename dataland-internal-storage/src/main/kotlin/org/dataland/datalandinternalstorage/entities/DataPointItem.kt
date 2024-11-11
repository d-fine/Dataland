package org.dataland.datalandinternalstorage.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * The database entity for storing data points
 */
@Entity
@Table(name = "data point_items")
data class DataPointItem(
    @Id
    @Column(name = "data_id")
    val id: String,
    @Column(name = "company_id")
    val companyId: UUID,
    @Column(name = "reporting_period")
    val reportingPeriod: String,
    @Column(name = "data_point_specification")
    val dataPointSpecification: String,
    @Column(name = "data", columnDefinition = "TEXT")
    val data: String,
)
