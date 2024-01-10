package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyDataOwnersEntity
import org.dataland.datalandbackend.repositories.DataOwnerRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

/**
 * Implementation of a (company) data ownership manager for Dataland
 * @param dataOwnerRepository  JPA for data ownership relations
 */
@Service("DataOwnersManager")
class DataOwnersManager(
    @Autowired private val dataOwnerRepository: DataOwnerRepository,
    @Autowired private val companyRepository: StoredCompanyRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to add a data owner to a given company
     * @param companyId the ID of the company to which the data owner is to be added
     * @param userId the ID of the user who is to become a data owner
     * @return an entity holding the data ownership relations for the given company
     */
    @Transactional
    fun addDataOwnerToCompany(companyId: String, userId: String): CompanyDataOwnersEntity {
        if (!companyRepository.existsById(companyId)) {
            throw ResourceNotFoundApiException(
                "Company not found",
                "There is no company corresponding to the provided Id $companyId stored on Dataland.",
            )
        }
        if (dataOwnerRepository.existsById(companyId)) {
            val dataOwnersForCompany = dataOwnerRepository.findById(companyId).get()
            return if (dataOwnersForCompany.dataOwners.contains(userId)) {
                logger.info(
                    "User with Id $userId is already data owner of company with Id $companyId.",
                )
                dataOwnersForCompany
            } else {
                logger.info("New data owner with Id $userId added to company with Id $companyId.")
                dataOwnersForCompany.dataOwners.add(userId)
                dataOwnerRepository.save(dataOwnersForCompany)
            }
        } else {
            logger.info("A first data owner with Id $userId is added to company with Id $companyId.")
            return dataOwnerRepository.save(
                CompanyDataOwnersEntity(
                    companyId = companyId,
                    dataOwners = mutableListOf(userId),
                ),
            )
        }
    }

    private fun checkIfCompanyIsValid(companyId: String) {
        if (!companyRepository.existsById(companyId)) {
            throw InvalidInputApiException(
                "Company is invalid",
                "There is no company corresponding to the provided Id $companyId stored on Dataland.",
            )
        }
    }

    /**
     * Method to get a data owner from a given company
     * @param companyId the ID of the company to which the data owner is requested
     * @return the userId(s) holding the data ownership relations for the given company
     */
    @Transactional
    fun getDataOwnerFromCompany(companyId: String): CompanyDataOwnersEntity {
       val dataOwnersOfCompany = dataOwnerRepository.findById(companyId).getOrElse {
            throw ResourceNotFoundApiException(
                "No data owners found",
                "The companyId '$companyId' do not have any data owners.",
            )
        }
        return dataOwnersOfCompany
    }

    /**
     * Method to delete a data owner from a given company
     * @param companyId the ID of the company to which the data owner is to be deleted from
     * @param userId the ID of the user who is to be removed as a data owner
     * @return an entity holding the data ownership relations for the given company
     */
    @Transactional
    fun deleteDataOwnerFromCompany(companyId: String, userId: String): CompanyDataOwnersEntity {
        if (dataOwnerRepository.existsById(companyId)) {
            val dataOwnersForCompany = dataOwnerRepository.findById(companyId).get()
            if (dataOwnersForCompany.dataOwners.contains(userId)) {
                dataOwnersForCompany.dataOwners.remove(userId)
            } else {
                throw ResourceNotFoundApiException(
                    "No data owners found",
                    "User with Id $userId has not been data owner of company $companyId",
                )
            }
            return if (dataOwnersForCompany.dataOwners.isEmpty()) {
                dataOwnerRepository.deleteById(companyId)
                CompanyDataOwnersEntity(companyId, dataOwnersForCompany.dataOwners)
            } else {
                dataOwnerRepository.save(
                    CompanyDataOwnersEntity(
                        companyId = companyId,
                        dataOwners = dataOwnersForCompany.dataOwners,
                    ),
                )
            }
        } else {
            throw InvalidInputApiException(
                "Company not found",
                "The companyId '$companyId' does not exist and therefore doesn't have any data owners.",
            )
        }
    }

    /**
     * Method to check whether a specified user is a data owner of a given company, which throws an exception if not
     * @param companyId the ID of the company
     * @param userId the ID of the user
     */
    @Transactional
    fun checkUserCompanyCombinationForDataOwnership(companyId: String, userId: String) {
        checkIfCompanyIsValid(companyId)
        val failException = ResourceNotFoundApiException(
            "User is not a data owner",
            "The user with Id $userId is not a data owner of the company with Id $companyId.",
        )
        if (!dataOwnerRepository.existsById(companyId)) {
            throw failException
        } else if (!dataOwnerRepository.getReferenceById(companyId).dataOwners.contains(userId)) {
            throw failException
        }
    }

    /**
     * Method to check whether the user currently authenticated user is data owner of a specified company and therefore
     * has uploader rights for this company
     * @param companyId the ID of the company
     * @return a Boolean indicating whether the user is data owner or not
     */
    @Transactional(readOnly = true)
    fun isCurrentUserDataOwner(companyId: String): Boolean {
        val userId = DatalandAuthentication.fromContext().userId
        fun exceptionToThrow(cause: Throwable?) = InsufficientRightsApiException(
            "Neither uploader nor data owner",
            "You don't seem be a data owner of company $companyId, which would be required for uploading this data " +
                "set without general uploader rights.",
            cause,
        )
        try {
            checkUserCompanyCombinationForDataOwnership(companyId, userId)
            return true
        } catch (invalidInputApiException: InvalidInputApiException) {
            throw exceptionToThrow(invalidInputApiException)
        } catch (resourceNotFoundApiException: ResourceNotFoundApiException) {
            throw exceptionToThrow(resourceNotFoundApiException)
        }
    }
}
