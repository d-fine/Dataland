package org.dataland.datalandbackend.exceptions

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException

class CompanyNotFoundException(companyId: String) : ResourceNotFoundApiException(
    "No company with ID: $companyId found",
    "Ther is no company with ID: $companyId on Dataland",
) {
}