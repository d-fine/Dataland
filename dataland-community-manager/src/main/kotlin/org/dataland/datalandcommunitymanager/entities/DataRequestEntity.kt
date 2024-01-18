package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.*
import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus


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

    @OneToMany
    val messageHistory: List<MessageRequestEntity>,

    val lastModifiedDate: Long,

    var requestStatus: RequestStatus,
)
