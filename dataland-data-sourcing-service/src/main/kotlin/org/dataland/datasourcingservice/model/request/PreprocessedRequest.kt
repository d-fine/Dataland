package org.dataland.datasourcingservice.model.request

import java.util.UUID

data class PreprocessedRequest(
    val companyId: String,
    val userId: UUID,
    val dataType: String,
    val notifyMeImmediately: Boolean,
    val correlationId: String,
)
