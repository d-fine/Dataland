package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyDataOwnersEntity
import org.dataland.datalandbackend.repositories.DataOwnerRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandemail.email.EmailSender
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

/**
 * Implementation of a (company) data ownership manager for Dataland
 * @param dataOwnerRepository  JPA for data ownership relations
 */
@Service("DataOwnersManager")
class DataOwnersManager(
    @Autowired private val dataOwnerRepository: DataOwnerRepository,
    @Autowired private val companyRepository: StoredCompanyRepository,
    @Autowired private val emailSender: EmailSender,
    @Autowired private val dataOwnershipRequestEmailBuilder: DataOwnershipRequestEmailBuilder,
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
        checkIfCompanyIsValid(companyId)
        return if (dataOwnerRepository.existsById(companyId)) {
            val dataOwnersForCompany = dataOwnerRepository.findById(companyId).get()
            if (dataOwnersForCompany.dataOwners.contains(userId)) {
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
            dataOwnerRepository.save(
                CompanyDataOwnersEntity(
                    companyId = companyId,
                    dataOwners = mutableListOf(userId),
                ),
            )
        }
    }

    private fun checkIfCompanyIsValid(companyId: String) {
        if (!companyRepository.existsById(companyId)) {
            throw ResourceNotFoundApiException(
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
            CompanyDataOwnersEntity(companyId, mutableListOf())
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
        checkIfCompanyIsValid(companyId)
        if (dataOwnerRepository.existsById(companyId)) {
            val dataOwnersForCompany = dataOwnerRepository.findById(companyId).get()
            if (dataOwnersForCompany.dataOwners.contains(userId)) {
                dataOwnersForCompany.dataOwners.remove(userId)
            } else {
                throw ResourceNotFoundApiException(
                    "Data owner not found",
                    "User with Id $userId has not been data owner of company $companyId",
                )
            }
            return if (dataOwnersForCompany.dataOwners.isEmpty()) {
                dataOwnerRepository.deleteById(companyId)
                CompanyDataOwnersEntity(companyId = companyId, dataOwners = dataOwnersForCompany.dataOwners)
            } else {
                dataOwnerRepository.save(
                    CompanyDataOwnersEntity(
                        companyId = companyId,
                        dataOwners = dataOwnersForCompany.dataOwners,
                    ),
                )
            }
        } else {
            throw ResourceNotFoundApiException(
                "No data owners found",
                "The companyId '$companyId' does not have any data owners.",
            )
        }
    }

    /**
     * Method to check whether a specified user is a data owner of a given company, which throws an exception if not
     * @param companyId the ID of the company
     * @param userId the ID of the user
     */
    @Transactional(readOnly = true)
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
     * Method to check whether the currently authenticated user is data owner of a specified company and therefore
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

    /**
     * Method to send an data ownership request if an ownership does not already exist
     * @param companyId the ID of the company for which data ownership is being requested
     * @param userAuthentication the DatalandAuthentication of the user who should become a data owner
     */
    @Transactional(readOnly = true)
    fun sendDataOwnershipRequestIfNecessary(
        companyId: String,
        userAuthentication: DatalandAuthentication,
        comment: String?,
    ) {
        assertAuthenticationViaJwtToken(userAuthentication)
        val companyName = companyRepository.findById(companyId).getOrElse {
            throw ResourceNotFoundApiException(
                "Company is invalid",
                "There is no company corresponding to the provided Id $companyId stored on Dataland.",
            )
        }.companyName
        if (
            dataOwnerRepository.findById(companyId).getOrNull()?.dataOwners?.contains(userAuthentication.userId) == true
        ) {
            throw InvalidInputApiException(
                "User is already a data owner for company.",
                "User with id: ${userAuthentication.userId} is already a data owner of company with id: $companyId.",
            )
        }
        emailSender.sendEmail(
            dataOwnershipRequestEmailBuilder.buildDataOwnershipRequest(
                companyId,
                companyName,
                userAuthentication,
                comment,
            ),
        )
    }

    private fun assertAuthenticationViaJwtToken(userAuthentication: DatalandAuthentication) {
        if (userAuthentication !is DatalandJwtAuthentication) {
            throw AuthenticationMethodNotSupportedException()
        }
    }

    /**
     * Method to determine if the owner doing the request is data owner of the specified company
     */
    fun isUserDataOwner(userIdRequester: String): Boolean {
        val userIdAuthenticated = SecurityContextHolder.getContext().authentication.name
        return userIdAuthenticated == userIdRequester
        //TODO test if this sonar fix works
    }
}
