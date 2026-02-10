package org.dataland.datasourcingservice.model.request

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.MixedState
import org.dataland.datasourcingservice.model.enums.RequestState

/**
 * A class that holds the combined states of a request and its associated data sourcing entry, used for the "mixedState"
 */
data class CombinedStateHistoryEntry(
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_LAST_MODIFIED_TIMESTAMP_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_LAST_MODIFIED_TIMESTAMP_EXAMPLE,
    )
    val lastModifiedDate: Long,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_STATE_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_STATE_EXAMPLE,
    )
    val requestState: RequestState,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_STATE_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_STATE_EXAMPLE,
    )
    val dataSourcingState: DataSourcingState,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.MIXED_STATE_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.MIXED_STATE_EXAMPLE,
    )
    val mixedState: MixedState,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.ADMIN_COMMENT_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.ADMIN_COMMENT_EXAMPLE,
        nullable = true,
    )
    val adminComment: String? = null,
)
