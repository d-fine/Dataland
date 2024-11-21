package org.dataland.datalandcommunitymanager.model.dataRequest

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the different statuses of a data request
 */
@Schema(
    enumAsRef = true,
)
enum class RequestStatus { Open, Answered, Resolved, Withdrawn, Closed, NonSourcable }
