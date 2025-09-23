package org.dataland.datasourcingservice.model.request

import java.util.UUID

data class PreprocessedRequest(
    val companyId: UUID,
    val userId: UUID,
    val dataType: String,
    val notifyMeImmediately: Boolean,
    val correlationId: String,
)
