package org.dataland.datalandcommunitymanager.model.dataRequest

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the different statuses of a data request
 */
@Schema(
    enumAsRef = true,
)
enum class RequestStatus { Open, Answered, Resolved, Withdrawn, Closed }
//TODO here new status have to be added: Revoked, Granted, Declined, Pending
//TODO could lead to confusion when to use which status