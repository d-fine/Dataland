package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.IdentifierType

/**
 * The entity storing the information considering one single data request
 */
@Entity
@Table(name = "data_requests")
data class DataRequestEntity(
    @Id
    val dataRequestId: String,

    val userId: String,

    val timestamp: Long, // TODO compare with other timestamps in other services + think about the name

    val dataType: DataTypeEnum,

    val companyIdentifierType: IdentifierType,

    val companyIdentifierValue: String, //TODO should contain list???

    val companyIdOnDataland: String?,
)
