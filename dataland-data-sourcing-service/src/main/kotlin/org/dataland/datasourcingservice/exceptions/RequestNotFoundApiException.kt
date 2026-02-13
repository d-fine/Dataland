package org.dataland.datasourcingservice.exceptions

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import java.util.UUID

/**
 * Exception thrown when a request with the given ID is not found.
 */
class RequestNotFoundApiException(
    requestId: UUID,
) : ResourceNotFoundApiException(
        summary = "Request not found.",
        message = "Dataland does not know the request with ID $requestId.",
    )
