package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyDataOwnersEntity
import org.dataland.datalandbackend.repositories.DataOwnerRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of a (company) data ownership manager for Dataland
 * @param dataOwnerRepository  JPA for data ownership relations
 */
@Service("DataRequestManager")
class DataOwnersManager(
    @Autowired private val dataOwnerRepository: DataOwnerRepository,
    @Autowired private val companyRepository: StoredCompanyRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun isValidUUID(checkString: String): Boolean {
        val regexPattern = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        return regexPattern.matches(checkString)
    }

    private fun checkIdsAreValid(companyId: String, userId: String) {
        if (!isValidUUID(companyId)) throw IllegalArgumentException("The companyId '$companyId' is not a valid UUID.")
        if (!isValidUUID(userId)) throw IllegalArgumentException("The userId '$userId' is not a valid UUID.")
    }

    /**
     * Method to add a data owner to a given company
     * @param companyId the ID of the company to which the data owner is to be added
     * @param userId the ID of the user who is to become a data owner
     * @return an entity holding the data ownership relations for the given company
     */
    @Transactional
    fun addDataOwnerToCompany(companyId: String, userId: String): CompanyDataOwnersEntity {
        checkIdsAreValid(companyId, userId)
        if (!companyRepository.existsById(companyId)) {
            throw ResourceNotFoundApiException(
                "Company not found",
                "There is no company corresponding to the provided Id $companyId stored on Dataland.",
            )
        } else {
            if (dataOwnerRepository.existsById(companyId)) {
                val dataOwnersForCompany = dataOwnerRepository.findById(companyId).get()
                return if (dataOwnersForCompany.dataOwners.contains(userId)) {
                    logger.info(
                        "User with Id $userId is already data owner of company with Id $companyId.",
                    )
                    dataOwnersForCompany
                } else {
                    dataOwnersForCompany.dataOwners.add(userId)
                    dataOwnerRepository.save(dataOwnersForCompany)
                }
            } else {
                return dataOwnerRepository.save(
                    CompanyDataOwnersEntity(
                        companyId = companyId,
                        dataOwners = mutableListOf(userId),
                    ),
                )
            }
        }
    }

    /**
     * Method to delete a data owner from a given company
     * @param companyId the ID of the company to which the data owner is to be deleted from
     * @param userId the ID of the user who is to be removed as a data owner
     * @return an entity holding the data ownership relations for the given company
     */
    @Transactional
    fun deleteDataOwnerFromCompany(companyId: String, userId: String): CompanyDataOwnersEntity? {
        checkIdsAreValid(companyId, userId)
        if (dataOwnerRepository.existsById(companyId)) {
            val dataOwnersForCompany = dataOwnerRepository.findById(companyId).get()
            if (dataOwnersForCompany.dataOwners.contains(userId)) {
                dataOwnersForCompany.dataOwners.remove(userId)
            } else {
                throw ResourceNotFoundApiException("Company not found",
                    "User with Id $userId has not been data owner of company $companyId")
            }
            return if (dataOwnersForCompany.dataOwners.isEmpty()) {
                dataOwnerRepository.deleteById(companyId)
                null
            }
            else {
                dataOwnerRepository.save(CompanyDataOwnersEntity(companyId = companyId,
                    dataOwners = dataOwnersForCompany.dataOwners))
            }

        }
        else {
            throw ResourceNotFoundApiException("No data owners found",
                "The companyId '$companyId' does not have any data owners.")
        }
    }
}
