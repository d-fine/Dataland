package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
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

    @Enumerated(EnumType.STRING)
    val dataRequestCompanyIdentifierType: DataRequestCompanyIdentifierType,

    val dataRequestCompanyIdentifierValue: String,

    @OneToMany(mappedBy = "dataRequestId")
    val messageHistory: MutableList<MessageRequestEntity>,

    val lastModifiedDate: Long,

    var requestStatus: RequestStatus,
)
