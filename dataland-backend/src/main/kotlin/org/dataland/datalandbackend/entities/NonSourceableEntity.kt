package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.dataland.datalandbackend.converter.DataTypeConverter
import org.dataland.datalandbackend.model.DataType
import java.util.UUID

/**
 * The database entity for storing the history on non-sourceable datasets
 */
@Entity
@Table(
    name = "data_source_history",
)
data class NonSourceableEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_Id", nullable = true, updatable = false)
    val eventId: UUID?,
    @JoinColumn(name = "company_id")
    var companyId: String,
    @Column(name = "data_type", nullable = false)
    @Convert(converter = DataTypeConverter::class)
    var dataType: DataType,
    @Column(name = "reporting_period", nullable = false)
    var reportingPeriod: String,
    @Column(name = "is_non_sourceable")
    var isNonSourceable: Boolean?,
    @Column(name = "reason", nullable = true)
    var reason: String,
    @Column(name = "creation_time", nullable = false)
    var creationTime: Long,
)
