package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import java.util.*

/**
 * The entity storing the information considering one data requests
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

    val datalandCompanyId: String,

    @OneToMany(mappedBy = "dataRequest")
    var messageHistory: List<MessageEntity>,

    @OneToMany(mappedBy = "dataRequest")
    var dataRequestStatusHistory: List<RequestStatusEntity>,

    var lastModifiedDate: Long,

) {
    val requestStatus: RequestStatus
        get() = (dataRequestStatusHistory.maxByOrNull { it.creationTimestamp }?.requestStatus) ?: RequestStatus.Open
    val accessStatus: AccessStatus
        get() = (dataRequestStatusHistory.maxByOrNull { it.creationTimestamp }?.accessStatus) ?: AccessStatus.Public
    constructor(
        userId: String,
        dataType: String,
        reportingPeriod: String,
        datalandCompanyId: String,
        creationTimestamp: Long,
    ) : this(
        dataRequestId = UUID.randomUUID().toString(),
        userId = userId,
        creationTimestamp = creationTimestamp,
        dataType = dataType,
        reportingPeriod = reportingPeriod,
        datalandCompanyId = datalandCompanyId,
        messageHistory = listOf(),
        dataRequestStatusHistory = listOf(),
        lastModifiedDate = creationTimestamp,
    )

    /**
     * Adds a messageEntity to the messageHistory.
     * Note, this is not automatically saved in the database, you also need to persist the messageEntity.
     */
    fun addRequestEventToMessageHistory(messageEntity: MessageEntity) {
        this.messageHistory += messageEntity
    }

    /**
     * Adds a requestStatusEntity to the dataRequestStatusHistory.
     * Note, this is not automatically saved in the database, you also need to persist the requestStatusEntity.
     */
    fun addToDataRequestStatusHistory(requestStatusEntity: RequestStatusEntity) {
        this.dataRequestStatusHistory += requestStatusEntity
    }

    /**
     * Converts this entity to a StoredDataRequest
     * @returns the StoredDataRequest
     */
    fun toStoredDataRequest() = StoredDataRequest(
        dataRequestId = dataRequestId,
        userId = userId,
        creationTimestamp = creationTimestamp,
        dataType = dataType,
        reportingPeriod = reportingPeriod,
        datalandCompanyId = datalandCompanyId,
        messageHistory = messageHistory
            .sortedBy { it.creationTimestamp }
            .map { it.toStoredDataRequestMessageObject() },
        dataRequestStatusHistory = dataRequestStatusHistory
            .sortedBy { it.creationTimestamp }
            .map { it.toStoredDataRequestStatusObject() },
        lastModifiedDate = lastModifiedDate,
        requestStatus = requestStatus,
        accessStatus = accessStatus,
    )

    /**
     * This method returns the appropriate description for a given datatype enum
     * @return datatype description
     */
    fun getDataTypeDescription(): String {
        return DataTypeEnum.entries.find { it.value == dataType }.let { readableFrameworkNameMapping[it] }
            ?: dataType
    }
}
