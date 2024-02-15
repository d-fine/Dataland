package org.dataland.datalandcommunitymanager.entities

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject

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

    @Column(columnDefinition = "text")
    var messageHistory: String?,

    val lastModifiedDate: Long,

    @Enumerated(EnumType.STRING)
    var requestStatus: RequestStatus,
) {

    /**
     * Converts a data request entity to the respective api model
     * @return an api model object
     */
    fun toStoredDataRequest(): StoredDataRequest {
        val dataType = DataTypeEnum.entries.find { it.value == this.dataTypeName }
        val objectMapper = ObjectMapper()

        val emptyMutableListOfStoredDataRequestMessageObjectsAsString =
            objectMapper.writeValueAsString(mutableListOf<StoredDataRequestMessageObject>())

        return StoredDataRequest(
            this.dataRequestId,
            this.userId,
            this.creationTimestamp,
            dataType,
            this.reportingPeriod,
            this.dataRequestCompanyIdentifierType,
            this.dataRequestCompanyIdentifierValue,
            objectMapper.readValue(
                this.messageHistory ?: emptyMutableListOfStoredDataRequestMessageObjectsAsString,
                object : TypeReference<MutableList<StoredDataRequestMessageObject>>() {},
            ),
            this.lastModifiedDate,
            this.requestStatus,
        )
    }
}
