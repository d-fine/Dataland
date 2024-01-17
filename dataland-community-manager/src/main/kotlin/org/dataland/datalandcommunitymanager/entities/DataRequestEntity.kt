package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.*
import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredMessageRequest


/**
 * The entity storing the information considering one single data request
 */
@Entity
@Table(name = "data_requests")
data class DataRequestEntity(
    @Id
    val dataRequestId: String,

    val userId: String,

    val creationTimestamp: Long,

    val dataTypeName: String,

    @Enumerated(EnumType.STRING)
    val dataRequestCompanyIdentifierType: DataRequestCompanyIdentifierType,

    val dataRequestCompanyIdentifierValue: String,

    val lastModifiedDate: Long,

    //@OneToMany(mappedBy = "DataRequestEntity")
    //val messageHistory: List<StoredMessageRequest>? = null,

    val requestStatus: RequestStatus,
)
