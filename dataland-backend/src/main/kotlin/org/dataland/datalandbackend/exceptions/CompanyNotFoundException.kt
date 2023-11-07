package org.dataland.datalandbackend.exceptions

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException

/**
 * A CompanyNotFoundException is a ResourceNotFoundApiException and should be thrown if a company could not be found
 * via dataland company ID
 */
class CompanyNotFoundException(companyId: String) : ResourceNotFoundApiException(
    "No company with ID: $companyId found",
    "Ther is no company with ID: $companyId on Dataland",
)
