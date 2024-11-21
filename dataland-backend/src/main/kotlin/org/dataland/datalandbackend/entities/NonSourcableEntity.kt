package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.dataland.datalandbackend.interfaces.ApiModelConversion
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.NonSourcableData
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import java.util.UUID

/**
 * The database entity for storing the history on non-sourcable datasets
 */
@Entity
@Table(
    name = "non_sourcable",
)
data class NonSourcableEntity(
    @Id
    @Column(name = "event_Id", nullable = false, updatable = false)
    val eventId: UUID = UUID.randomUUID(),
    @JoinColumn(name = "company_id")
    var company: StoredCompanyEntity,
    @Column(name = "data_type", nullable = false)
    var dataType: String,
    @Column(name = "reporting_period", nullable = false)
    var reportingPeriod: String,
    @Column(name = "non_sourcable", nullable = true)
    var nonSourcable: Boolean,
    @Column(name = "reason", nullable = true)
    var reason: String,
    @Column(name = "creation_time", nullable = false)
    var creationTime: Long,
) : ApiModelConversion<NonSourcableData> {
    override fun toApiModel(viewingUser: DatalandAuthentication?): NonSourcableData =
        NonSourcableData(
            eventId = eventId,
            companyId = company.companyId,
            dataType = DataType.valueOf(dataType),
            reportingPeriod = reportingPeriod,
            nonSourcable = nonSourcable,
            reason = reason,
            creationTime = creationTime,
        )
}
