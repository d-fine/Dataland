package org.dataland.datasourcingservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.ExtendedStoredRequest
import org.dataland.datasourcingservice.model.request.StoredRequest
import org.hibernate.envers.Audited
import java.util.UUID

/**
 * The database entity for storing request objects.
 */
@SuppressWarnings("LongParameterList")
@Entity
@Audited
@Table(name = "requests")
class RequestEntity(
    @Id
    @Column(name = "id")
    val id: UUID,
    @Column(name = "company_id")
    val companyId: UUID,
    @Column(name = "reporting_period")
    val reportingPeriod: String,
    @Column(name = "data_type")
    val dataType: String,
    @Column(name = "user_id")
    val userId: UUID,
    @Column(name = "creation_time_stamp")
    val creationTimestamp: Long,
    @Column(name = "member_comment", length = 1000)
    val memberComment: String? = null,
    @Column(name = "admin_comment", length = 1000)
    var adminComment: String? = null,
    @Column(name = "last_modified_date")
    var lastModifiedDate: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "request_priority")
    var requestPriority: RequestPriority,
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    var state: RequestState,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_sourcing_id")
    var dataSourcingEntity: DataSourcingEntity? = null,
) {
    /**
     * Converts this RequestEntity to a StoredDataRequest.
     */
    fun toStoredDataRequest(): StoredRequest =
        StoredRequest(
            id = id.toString(),
            companyId = companyId.toString(),
            reportingPeriod = reportingPeriod,
            dataType = dataType,
            userId = userId.toString(),
            creationTimeStamp = creationTimestamp,
            memberComment = memberComment,
            adminComment = adminComment,
            lastModifiedDate = lastModifiedDate,
            requestPriority = requestPriority,
            state = state,
            dataSourcingEntityId = dataSourcingEntity?.dataSourcingId?.toString(),
        )

    /**
     * Converts this RequestEntity to a ExtendedStoredDataRequest.
     */
    fun toExtendedStoredRequest(): ExtendedStoredRequest =
        ExtendedStoredRequest(
            id = id.toString(),
            companyId = companyId.toString(),
            reportingPeriod = reportingPeriod,
            dataType = dataType,
            userId = userId.toString(),
            creationTimeStamp = creationTimestamp,
            memberComment = memberComment,
            adminComment = adminComment,
            lastModifiedDate = lastModifiedDate,
            requestPriority = requestPriority,
            state = state,
            dataSourcingEntityId = dataSourcingEntity?.dataSourcingId?.toString(),
            companyName = String(),
            userEmailAddress = null,
        )

    constructor(
        userId: UUID,
        companyId: UUID,
        dataType: String,
        reportingPeriod: String,
        memberComment: String?,
        creationTimestamp: Long,
        requestPriority: RequestPriority,
    ) : this(
        id = UUID.randomUUID(),
        companyId = companyId,
        reportingPeriod = reportingPeriod,
        dataType = dataType,
        userId = userId,
        creationTimestamp = creationTimestamp,
        memberComment = memberComment,
        lastModifiedDate = creationTimestamp,
        requestPriority = requestPriority,
        state = RequestState.Open,
    )
}
