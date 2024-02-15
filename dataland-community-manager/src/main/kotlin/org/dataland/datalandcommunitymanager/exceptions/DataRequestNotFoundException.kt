package org.dataland.datalandcommunitymanager.exceptions

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException

/**
 * A ResourceNotFoundApiException should be thrown if a dataland-internal resource could not be located
 * Both message and summary are displayed to the user with a 404 status code
 */
class DataRequestNotFoundException(
    requestId: String,
) : ResourceNotFoundApiException(
    "Data request not found",
    "Dataland does not know the Data request ID $requestId",
)
