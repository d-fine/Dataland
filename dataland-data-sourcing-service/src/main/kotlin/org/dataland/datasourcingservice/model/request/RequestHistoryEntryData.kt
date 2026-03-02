package org.dataland.datasourcingservice.model.request

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datasourcingservice.model.enums.DisplayedState

/**
 * A class that has the fields needed to display a request history entry for a non admin view
 */
data class RequestHistoryEntryData(
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_LAST_MODIFIED_TIMESTAMP_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_LAST_MODIFIED_TIMESTAMP_EXAMPLE,
    )
    override val modificationDate: Long,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.DISPLAYED_STATE_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.DISPLAYED_STATE_EXAMPLE,
    )
    override val displayedState: DisplayedState,
) : RequestHistoryEntry
