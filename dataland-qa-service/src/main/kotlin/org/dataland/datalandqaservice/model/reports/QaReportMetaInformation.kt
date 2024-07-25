package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import java.util.*

data class QaReportMetaInformation (
    val dataId: UUID,
    val qaReportId: UUID,
    val reporterUserId: UUID,
    val uploadTime: Long,
)