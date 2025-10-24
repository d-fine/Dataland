package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandcommunitymanager.api.CompanyRightsApi
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRight
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRightAssignment
import org.dataland.datalandcommunitymanager.services.CompanyRightsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the company rights endpoints.
 */
@RestController
class CompanyRightsController(
    @Autowired private val companyRightsService: CompanyRightsService,
) : CompanyRightsApi {
    override fun getCompanyRights(companyId: String): ResponseEntity<List<CompanyRight>> =
        ResponseEntity.ok(
            companyRightsService.getCompanyRights(
                ValidationUtils.convertToUUID(companyId),
            ),
        )

    override fun postCompanyRight(companyRightAssignment: CompanyRightAssignment): ResponseEntity<CompanyRightAssignment> =
        ResponseEntity.ok(
            companyRightsService.assignCompanyRight(companyRightAssignment),
        )
}
