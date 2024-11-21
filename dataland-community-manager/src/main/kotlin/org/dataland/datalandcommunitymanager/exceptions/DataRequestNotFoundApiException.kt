package org.dataland.datalandcommunitymanager.exceptions

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException

/**
 * A DataRequestNotFoundApiException should be thrown if a data request could not be found
 */
class DataRequestNotFoundApiException(
    requestId: String,
) : ResourceNotFoundApiException(
        "Data request not found",
        "Dataland does not know the Data request ID $requestId",
    )
