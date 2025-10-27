package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRight
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRightAssignment
import org.dataland.datalandcommunitymanager.repositories.CompanyRightsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service class for handling company rights.
 */
@Service
class CompanyRightsService(
    @Autowired private val companyRightsRepository: CompanyRightsRepository,
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
) {
    /**
     * Get a list of all rights assigned to a company.
     * @param companyId The Dataland ID of the company.
     * @return The associated list of company rights.
     */
    @Transactional(readOnly = true)
    fun getCompanyRights(companyId: UUID): List<CompanyRight> =
        companyRightsRepository.findAllByCompanyId(companyId).map { it.companyRight }

    /**
     * Assign the specified company right to the specified company.
     * Does not create a new database entry if the assignment already exists.
     * @param companyRightAssignment The company right assignment to create.
     * @return The associated company right assignment in the database.
     */
    @Transactional
    fun assignCompanyRight(companyRightAssignment: CompanyRightAssignment<UUID>): CompanyRightAssignment<String> {
        try {
            companyDataControllerApi.isCompanyIdValid(companyRightAssignment.companyId.toString())
        } catch (_: ClientException) {
            throw ResourceNotFoundApiException(
                summary = "Company not found.",
                message = "Dataland does not know the company ID ${companyRightAssignment.companyId}.",
            )
        }
        companyRightsRepository.findByIdOrNull(companyRightAssignment.toCompanyRightId()) ?: companyRightsRepository.save(
            companyRightAssignment.toCompanyRightEntity(),
        )

        return CompanyRightAssignment<String>(
            companyId = companyRightAssignment.companyId.toString(),
            companyRight = companyRightAssignment.companyRight,
        )
    }

    /**
     * Delete the specified company right assignment.
     * @param companyRightAssignment The company right assignment to delete.
     * @throws ResourceNotFoundApiException if the specified company right assignment does not exist.
     */
    @Transactional
    fun removeCompanyRight(companyRightAssignment: CompanyRightAssignment<UUID>) {
        val companyRightEntity =
            companyRightsRepository.findByIdOrNull(
                companyRightAssignment.toCompanyRightId(),
            ) ?: throw ResourceNotFoundApiException(
                summary = "Company right assignment not found.",
                message = "The specified company right assignment does not exist.",
            )
        companyRightsRepository.delete(companyRightEntity)
    }
}
