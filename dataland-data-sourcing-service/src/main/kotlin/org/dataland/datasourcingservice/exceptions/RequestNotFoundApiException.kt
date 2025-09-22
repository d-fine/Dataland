package org.dataland.datasourcingservice.exceptions

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import java.util.UUID

class RequestNotFoundApiException(
    val requestId: UUID,
) : ResourceNotFoundApiException(
        summary = "Request not found.",
        message = "Dataland does not know the request with ID $requestId.",
    )
