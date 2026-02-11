package org.dataland.datasourcingservice.model.request

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.DisplayedState
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.utils.getDisplayedState

/**
 * A class that holds the combined states of a request and its associated data sourcing entry, used for the "mixedState"
 */
data class ExtendedRequestHistoryEntryData(
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_LAST_MODIFIED_TIMESTAMP_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_LAST_MODIFIED_TIMESTAMP_EXAMPLE,
    )
    override val modificationDate: Long,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_STATE_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.REQUEST_STATE_EXAMPLE,
    )
    override val requestState: RequestState,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_STATE_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.DATA_SOURCING_STATE_EXAMPLE,
    )
    override val dataSourcingState: DataSourcingState? = null,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.MIXED_STATE_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.MIXED_STATE_EXAMPLE,
    )
    override val displayedState: DisplayedState,
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.ADMIN_COMMENT_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.ADMIN_COMMENT_EXAMPLE,
        nullable = true,
    )
    override val adminComment: String? = null,
) : ExtendedRequestHistoryEntry {
    constructor(requestEntity: RequestEntity, dataSourcingState: DataSourcingState?) : this(
        modificationDate = requestEntity.lastModifiedDate,
        requestState = requestEntity.state,
        dataSourcingState = dataSourcingState,
        displayedState = getDisplayedState(requestEntity.state, dataSourcingState),
        adminComment = requestEntity.adminComment,
    )

    constructor(dataSourcingObject: DataSourcingWithoutReferences, requestState: RequestState, comment: String?) : this(
        modificationDate = dataSourcingObject.lastModifiedDate,
        requestState = requestState,
        dataSourcingState = dataSourcingObject.state,
        displayedState = getDisplayedState(requestState, dataSourcingObject.state),
        adminComment = comment,
    )
}
