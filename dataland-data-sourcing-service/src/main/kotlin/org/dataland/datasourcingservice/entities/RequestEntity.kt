package org.dataland.datasourcingservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import java.util.Date
import java.util.UUID

/**
 * The database entity for storing request objects.
 */
@SuppressWarnings("LongParameterList")
@Entity
@Table(name = "requests")
class RequestEntity(
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID,
    @Column(name = "company_id")
    val companyId: UUID,
    val reportingPeriod: String,
    @Column(name = "data_type")
    val dataType: String,
    @Column(name = "user_id")
    val userId: UUID,
    @Column(name = "creation_time_stamp")
    val creationTimeStamp: Date,
    @Column(name = "billable")
    val billable: Boolean = true,
    @Column(name = "member_comment", length = 1000)
    val memberComment: String? = null,
    @Column(name = "admin_comment", length = 1000)
    val adminComment: String? = null,
    @Column(name = "last_modified_date")
    val lastModifiedDate: Date,
    @Column(name = "request_prioriry")
    val requestPriority: RequestPriority,
    @Column(name = "state")
    val state: RequestState,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_sourcing_id")
    val dataSourcingEntity: DataSourcingEntity? = null,
)
