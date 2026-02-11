package org.dataland.datasourcingservice.model.request

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences
import org.dataland.datasourcingservice.model.enums.DisplayedState
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.utils.getDisplayedState

/**
 * A class that holds the combined states of a request and its associated data sourcing entry, used for the "mixedState"
 */
data class RequestHistoryEntryData(
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
) : RequestHistoryEntry {
    constructor(requestEntity: RequestEntity) : this(
        modificationDate = requestEntity.lastModifiedDate,
        displayedState = getDisplayedState(requestEntity.state, null),
    )

    constructor(dataSourcingObject: DataSourcingWithoutReferences, lastDisplayedState: DisplayedState) : this(
        modificationDate = dataSourcingObject.lastModifiedDate,
        displayedState =
            if (lastDisplayedState == DisplayedState.Withdrawn) {
                DisplayedState.Withdrawn
            } else {
                getDisplayedState(RequestState.Processing, dataSourcingObject.state)
            },
    )

    constructor(dataSourcingObject: DataSourcingWithoutReferences, requestEntity: RequestEntity) : this(
        modificationDate = dataSourcingObject.lastModifiedDate,
        displayedState = getDisplayedState(requestEntity.state, dataSourcingObject.state),
    )
}
