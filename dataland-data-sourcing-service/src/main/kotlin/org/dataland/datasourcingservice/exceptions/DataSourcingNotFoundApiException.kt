package org.dataland.datasourcingservice.exceptions

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import java.util.UUID

/**
 * Exception indicating that a data sourcing object could not be found.
 */
class DataSourcingNotFoundApiException : ResourceNotFoundApiException {
    constructor(dataSourcingId: UUID) : super(
        summary = "Data sourcing not found.",
        message = "Dataland does not know the data sourcing with ID $dataSourcingId.",
    )

    constructor(
        companyId: String,
        reportingPeriod: String,
        dataType: String,
    ) : super(
        summary = "Data sourcing not found.",
        message =
            "Dataland does not know a data sourcing associated with company ID " +
                "$companyId, reporting period $reportingPeriod and data type $dataType.",
    )
}
