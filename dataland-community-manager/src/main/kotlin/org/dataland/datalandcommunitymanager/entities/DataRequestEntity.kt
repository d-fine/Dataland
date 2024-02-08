package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus

/**
 * The entity storing the information considering one single data request
 */
@Entity
@Table(name = "data_requests")
data class DataRequestEntity(
    @Id
    @Column(name = "data_request_id")
    val dataRequestId: String,

    val userId: String,

    val creationTimestamp: Long,

    val dataTypeName: String,

    val reportingPeriod: String,

    val datalandCompanyId: String,

    @Column(columnDefinition = "text")
    var messageHistory: String?,

    val lastModifiedDate: Long,

    @Enumerated(EnumType.STRING)
    var requestStatus: RequestStatus,
)
