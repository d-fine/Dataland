package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import java.util.UUID

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
    val datalandCompanyId: String,
    var emailOnUpdate: Boolean,
    @OneToMany(mappedBy = "dataRequest")
    var messageHistory: List<MessageEntity>,
    @OneToMany(mappedBy = "dataRequest")
    var dataRequestStatusHistory: List<RequestStatusEntity>,
    var lastModifiedDate: Long,
    var requestPriority: RequestPriority,
    var adminComment: String?,
) {
    val requestStatus: RequestStatus
        get() = (dataRequestStatusHistory.maxByOrNull { it.creationTimestamp }?.requestStatus) ?: RequestStatus.Open
    val accessStatus: AccessStatus
        get() = (dataRequestStatusHistory.maxByOrNull { it.creationTimestamp }?.accessStatus) ?: AccessStatus.Public
    val requestStatusChangeReason: String?
        get() = (dataRequestStatusHistory.maxByOrNull { it.creationTimestamp }?.requestStatusChangeReason)
    constructor(
        userId: String,
        dataType: String,
        emailOnUpdate: Boolean,
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
        emailOnUpdate = emailOnUpdate,
        messageHistory = listOf(),
        dataRequestStatusHistory = listOf(),
        lastModifiedDate = creationTimestamp,
        requestPriority =
            if (DatalandAuthentication.fromContext().roles.contains(DatalandRealmRole.ROLE_PREMIUM_USER)) {
                RequestPriority.High
            } else {
                RequestPriority.Low
            },
        adminComment = null,
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
    fun toStoredDataRequest(userEmailAddress: String? = null) =
        StoredDataRequest(
            dataRequestId = dataRequestId,
            userId = userId,
            userEmailAddress = userEmailAddress,
            creationTimestamp = creationTimestamp,
            dataType = dataType,
            reportingPeriod = reportingPeriod,
            datalandCompanyId = datalandCompanyId,
            emailOnUpdate = emailOnUpdate,
            messageHistory =
                messageHistory
                    .sortedBy { it.creationTimestamp }
                    .map { it.toStoredDataRequestMessageObject() },
            dataRequestStatusHistory =
                dataRequestStatusHistory
                    .sortedBy { it.creationTimestamp }
                    .map { it.toStoredDataRequestStatusObject() },
            lastModifiedDate = lastModifiedDate,
            requestStatus = requestStatus,
            accessStatus = accessStatus,
            requestPriority = requestPriority,
            adminComment = adminComment,
        )

    /**
     * This method returns the appropriate description for a given datatype enum
     * @return datatype description
     */
    fun getDataTypeDescription(): String =
        DataTypeEnum.entries.find { it.value == dataType }.let { readableFrameworkNameMapping[it] }
            ?: dataType
}
