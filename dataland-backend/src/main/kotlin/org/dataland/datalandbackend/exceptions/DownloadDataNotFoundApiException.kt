package org.dataland.datalandbackend.exceptions

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException

const val JOB_NOT_FOUND_SUMMARY = "No corresponding job found for associated user."

/**
 * A DownloadDataNotFoundApiException should be thrown if the data to be downloaded does not exist.
 */
class DownloadDataNotFoundApiException(
    summary: String = "No data found for download",
    message: String = "The data you requested for download does not exist on Dataland.",
) : ResourceNotFoundApiException(summary, message, null)
