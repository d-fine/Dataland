package org.dataland.e2etests.utils

import org.dataland.communitymanager.openApiClient.model.CompanyRightAssignmentString
import org.dataland.communitymanager.openApiClient.model.CompanyRightAssignmentString.CompanyRight
import java.util.UUID

/**
 * E2E test utility class for managing company rights via the Community Manager API.
 */
class CompanyRightsTestUtils {
    val apiAccessor = ApiAccessor()

    /**
     * Assign the specified company right to the specified company.
     * @param companyId The ID of the company to which the right will be assigned.
     * @param companyRight The company right to assign.
     * @return The CompanyRightAssignmentString DTO representing the assigned right.
     */
    fun assignCompanyRight(
        companyRight: CompanyRight,
        companyId: UUID,
    ): CompanyRightAssignmentString =
        apiAccessor.companyRightsControllerApi.postCompanyRight(
            CompanyRightAssignmentString(
                companyId = companyId.toString(),
                companyRight = companyRight,
            ),
        )

    /**
     * Remove the specified company right from the specified company.
     * @param companyId The ID of the company from which the right will be removed.
     * @param companyRight The company right to remove.
     */
    fun removeCompanyRight(
        companyRight: CompanyRight,
        companyId: UUID,
    ): Unit =
        apiAccessor.companyRightsControllerApi.deleteCompanyRight(
            CompanyRightAssignmentString(
                companyId = companyId.toString(),
                companyRight = companyRight,
            ),
        )
}
