package org.dataland.datalandcommunitymanager.entities

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.IdentifierType

/**
 * The entity storing the information considering one single data request
 */
// @Entity   TODO
// @Table(name = "review_information")       TODO
data class DataRequestEntity(
    // @Id     TODO
    val dataRequestId: String,

    val userId: String,

    val timestamp: Long, // TODO compare with other timestamps in other services + think about the name

    val dataType: DataTypeEnum,

    val companyIdentifierType: IdentifierType,

    val companyIdentifierValue: String,

    val companyIdOnDataland: String?,
)
