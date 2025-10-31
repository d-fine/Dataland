package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandcommunitymanager.api.CompanyRightsApi
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRight
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRightAssignment
import org.dataland.datalandcommunitymanager.services.CompanyRightsManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the company rights endpoints.
 */
@RestController
class CompanyRightsController(
    @Autowired private val companyRightsManager: CompanyRightsManager,
) : CompanyRightsApi {
    override fun getCompanyRights(companyId: String): ResponseEntity<List<CompanyRight>> =
        ResponseEntity.ok(
            companyRightsManager.getCompanyRights(
                ValidationUtils.convertToUUID(companyId),
            ),
        )

    override fun postCompanyRight(companyRightAssignment: CompanyRightAssignment<String>): ResponseEntity<CompanyRightAssignment<String>> =
        ResponseEntity.ok(
            companyRightsManager.assignCompanyRight(
                convertToCompanyRightAssignmentWithUUID(
                    companyRightAssignment,
                ),
            ),
        )

    private fun convertToCompanyRightAssignmentWithUUID(
        companyRightAssignmentWithString: CompanyRightAssignment<String>,
    ): CompanyRightAssignment<UUID> =
        CompanyRightAssignment<UUID>(
            companyId = ValidationUtils.convertToUUID(companyRightAssignmentWithString.companyId),
            companyRight = companyRightAssignmentWithString.companyRight,
        )

    override fun deleteCompanyRight(companyRightAssignment: CompanyRightAssignment<String>) =
        ResponseEntity.ok(
            companyRightsManager.removeCompanyRight(
                convertToCompanyRightAssignmentWithUUID(
                    companyRightAssignment,
                ),
            ),
        )
}
