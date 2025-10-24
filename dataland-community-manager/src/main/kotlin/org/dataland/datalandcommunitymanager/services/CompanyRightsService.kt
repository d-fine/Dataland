package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRight
import org.dataland.datalandcommunitymanager.repositories.CompanyRightsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service class for handling company rights.
 */
@Service
class CompanyRightsService(
    @Autowired private val companyRightsRepository: CompanyRightsRepository,
) {
    /**
     * Get a list of all rights assigned to a company.
     * @param companyId The Dataland ID of the company.
     * @return The associated list of company rights.
     */
    fun getCompanyRights(companyId: UUID): List<CompanyRight> =
        companyRightsRepository.findAllByCompanyId(companyId).map { it.companyRight }
}
