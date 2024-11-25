package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.dataland.datalandbackend.interfaces.ApiModelConversion
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.NonSourceableData
import org.dataland.keycloakAdapter.auth.DatalandAuthentication

/**
 * The database entity for storing the history on non-sourceable datasets
 */
@Entity
@Table(
    name = "non_sourceable",
)
data class NonSourceableEntity(
    @Id
    @Column(name = "event_Id", nullable = false, updatable = false)
    val eventId: String,
    @JoinColumn(name = "company_id")
    var companyId: String,
    @Column(name = "data_type", nullable = false)
    var dataType: String,
    @Column(name = "reporting_period", nullable = false)
    var reportingPeriod: String,
    @Column(name = "non_sourceable")
    var nonSourceable: Boolean,
    @Column(name = "reason", nullable = true)
    var reason: String,
    @Column(name = "creation_time", nullable = false)
    var creationTime: Long,
) : ApiModelConversion<NonSourceableData> {
    override fun toApiModel(viewingUser: DatalandAuthentication?): NonSourceableData =
        NonSourceableData(
            eventId = eventId,
            companyId = companyId,
            dataType = DataType.valueOf(dataType),
            reportingPeriod = reportingPeriod,
            nonSourceable = nonSourceable,
            reason = reason,
            creationTime = creationTime,
        )
}
