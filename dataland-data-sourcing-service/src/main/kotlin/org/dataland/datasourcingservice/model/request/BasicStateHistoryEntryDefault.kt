package org.dataland.datasourcingservice.model.request

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datasourcingservice.model.enums.DisplayedState

/**
 * A class that holds the combined states of a request and its associated data sourcing entry, used for the "mixedState"
 */
data class BasicStateHistoryEntryDefault(
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_LAST_MODIFIED_TIMESTAMP_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_LAST_MODIFIED_TIMESTAMP_EXAMPLE,
    )
    override val modificationDate: Long,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.MIXED_STATE_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.MIXED_STATE_EXAMPLE,
    )
    override val displayedState: DisplayedState,
) : BasicStateHistoryEntry
