package org.dataland.datasourcingservice.model.request

import java.util.UUID

/**
 * A preprocessed request that is validated and
 * contains all necessary information to process a data request.
 */
data class PreprocessedRequest(
    val companyId: UUID,
    val userId: UUID,
    val dataType: String,
    val correlationId: String,
)
