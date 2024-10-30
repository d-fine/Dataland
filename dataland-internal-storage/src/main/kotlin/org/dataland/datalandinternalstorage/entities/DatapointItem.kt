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
@Table(name = "datapoint_items")
data class DatapointItem(
    @Id
    @Column(name = "data_id")
    val id: String,
    @Column(name = "company_id")
    val companyId: UUID,
    @Column(name = "reporting_period")
    val reportingPeriod: String,
    @Column(name = "datapoint_specification")
    val datapointSpecification: String,
    @Column(name = "data", columnDefinition = "TEXT")
    val data: String,
)
