package org.dataland.datalandcommunitymanager.model.elementaryEventProcessing

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import java.util.UUID

/**
 Represents the meta info in an elementary events message payload.
 */
data class NotificationEventBasicInfo(
    val companyId: UUID,
    val framework: DataTypeEnum,
    val reportingPeriod: String,
    val userId: UUID? = null,
    val isProcessed: Boolean,
)
