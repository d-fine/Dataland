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
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.utils.getDataTypeEnumForFrameworkName

/**
 * The entity storing the information considering one data request
 */
@Entity
@Table(name = "data_requests")
data class DataRequestEntity(
    @Id
    @Column(name = "data_request_id")
    val dataRequestId: String,

    val userId: String,

    val creationTimestamp: Long,

    val dataType: String,

    val reportingPeriod: String,

    @Enumerated(EnumType.STRING)
    val dataRequestCompanyIdentifierType: DataRequestCompanyIdentifierType,

    val dataRequestCompanyIdentifierValue: String,

    @OneToMany(mappedBy = "dataRequest")
    var messageHistory: MutableList<MessageEntity>,

    val lastModifiedDate: Long,

    @Enumerated(EnumType.STRING)
    var requestStatus: RequestStatus,
) {
    fun associateMessages(messageHistory: MutableList<StoredDataRequestMessageObject>) {
        this.messageHistory = messageHistory.mapIndexed { index, it ->
            MessageEntity(it, index, this)
        }.toMutableList()
    }

    fun toStoredDataRequest() = StoredDataRequest(
        dataRequestId = dataRequestId,
        userId = userId,
        creationTimestamp = creationTimestamp,
        dataType = getDataTypeEnumForFrameworkName(dataType)!!,
        reportingPeriod = reportingPeriod,
        dataRequestCompanyIdentifierType = dataRequestCompanyIdentifierType,
        dataRequestCompanyIdentifierValue = dataRequestCompanyIdentifierValue,
        messageHistory = messageHistory
            .sortedBy { it.ordinal }
            .map { it.toStoredDataRequestMessageObject() }
            .toMutableList(),
        lastModifiedDate = lastModifiedDate,
        requestStatus = requestStatus,
    )
}
