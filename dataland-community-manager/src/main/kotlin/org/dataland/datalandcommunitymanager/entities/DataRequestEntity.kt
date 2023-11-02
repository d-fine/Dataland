package org.dataland.datalandcommunitymanager.entities

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

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

    @Enumerated(EnumType.STRING)
    val dataType: DataTypeEnum,

    @Enumerated(EnumType.STRING)
    val dataRequestCompanyIdentifierType: DataRequestCompanyIdentifierType,

    val dataRequestCompanyIdentifierValue: String,
)
