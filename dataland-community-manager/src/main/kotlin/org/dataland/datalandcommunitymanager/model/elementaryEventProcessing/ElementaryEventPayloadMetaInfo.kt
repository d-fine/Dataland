package org.dataland.datalandcommunitymanager.model.elementaryEventProcessing

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import java.util.*

/**
 Represents the meta info in an elementary events message payload.
 */
data class ElementaryEventPayloadMetaInfo(
    val companyId: UUID,
    val framework: DataTypeEnum,
    val reportingPeriod: String,
) {
    init {
        require(reportingPeriod.isNotEmpty()) { "Reporting period cannot be empty." }
    }
}
