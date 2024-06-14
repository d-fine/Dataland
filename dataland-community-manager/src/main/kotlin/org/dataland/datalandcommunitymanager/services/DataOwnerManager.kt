package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.services.messaging.DataOwnershipEmailMessageSender
import org.dataland.datalandbackend.services.messaging.DataOwnershipSuccessfullyEmailMessageSender
import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.entities.CompanyDataOwnersEntity
import org.dataland.datalandcommunitymanager.repositories.DataOwnerRepository
import org.dataland.datalandcommunitymanager.utils.CompanyIdValidator
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

/**
 * Service that handles all operations associated with data-ownership
 *
 */
@Service("DataOwnersManager")
class DataOwnerManager(
    @Autowired private val companyApi: CompanyDataControllerApi,
    @Autowired private val companyIdValidator: CompanyIdValidator,
    @Autowired private val dataOwnerRepository: DataOwnerRepository,
    @Autowired private val dataOwnershipEmailMessageSender: DataOwnershipEmailMessageSender,
    @Autowired private val dataOwnershipSuccessfullyEmailMessageSender: DataOwnershipSuccessfullyEmailMessageSender,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to add a data owner to a given company
     * @param companyId the ID of the company to which the data owner is to be added
     * @param userId the ID of the user who is to become a data owner
     * @return an entity holding the data ownership relations for the given company
     */
    @Transactional
    fun addDataOwnerToCompany(
        companyId: String,
        userId: String,
        companyName: String,
    ): CompanyDataOwnersEntity {
        val correlationId = UUID.randomUUID().toString()
        companyIdValidator.checkIfCompanyIdIsValid(companyId)
        return if (dataOwnerRepository.existsById(companyId)) {
            val dataOwnersForCompany = dataOwnerRepository.findById(companyId).get()
            if (dataOwnersForCompany.dataOwners.contains(userId)) {
                logger.info("User with Id $userId is already data owner of company with Id $companyId.")
                dataOwnersForCompany
            } else {
                dataOwnershipSuccessfullyEmailMessageSender.sendDataOwnershipAcceptanceExternalEmailMessage(
                    userId, companyId, companyName, correlationId,
                )
                logger.info("New data owner with Id $userId added to company with Id $companyId.")
                dataOwnersForCompany.dataOwners.add(userId)
                dataOwnerRepository.save(dataOwnersForCompany)
            }
        } else {
            dataOwnershipSuccessfullyEmailMessageSender.sendDataOwnershipAcceptanceExternalEmailMessage(
                userId, companyId, companyName, correlationId,
            )
            logger.info("A first data owner with Id $userId is added to company with Id $companyId.")
            dataOwnerRepository.save(
                CompanyDataOwnersEntity(companyId = companyId, dataOwners = mutableListOf(userId)),
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
        companyIdValidator.checkIfCompanyIdIsValid(companyId)
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
        companyIdValidator.checkIfCompanyIdIsValid(companyId)
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
    fun checkUserCompanyCombinationForDataOwnership(
        companyId: String,
        userId: String,
    ) {
        companyIdValidator.checkIfCompanyIdIsValid(companyId)
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
     * A method to verify if a company has data owners
     * @param companyId the ID of the company for which the information should be retrieved
     */
    @Transactional(readOnly = true)
    fun checkCompanyForDataOwnership(companyId: String) {
        companyIdValidator.checkIfCompanyIdIsValid(companyId)
        val dataOwnersEntity = getDataOwnerFromCompany(companyId)
        if (dataOwnersEntity.dataOwners.isEmpty()) {
            throw ResourceNotFoundApiException(
                "Company has no data owner.",
                "Company with $companyId has no data owner(s).",
            )
        }
    }

    /**
     * Method to send a data ownership request if an ownership does not already exist
     * @param companyId the ID of the company for which data ownership is being requested
     * @param userAuthentication the DatalandAuthentication of the user who should become a data owner
     */
    @Transactional(readOnly = true)
    fun sendDataOwnershipRequestIfNecessary(
        companyId: String,
        userAuthentication: DatalandAuthentication,
        comment: String?,
        correlationId: String,
    ) {
        assertAuthenticationViaJwtToken(userAuthentication)
        companyIdValidator.checkIfCompanyIdIsValid(companyId)
        if (
            dataOwnerRepository.findById(companyId).getOrNull()?.dataOwners?.contains(userAuthentication.userId) == true
        ) {
            throw InvalidInputApiException(
                "User is already a data owner for company.",
                "User with id: ${userAuthentication.userId} is already a data owner of company with id: $companyId.",
            )
        }
        dataOwnershipEmailMessageSender.sendDataOwnershipInternalEmailMessage(
            userAuthentication = userAuthentication as DatalandJwtAuthentication,
            datalandCompanyId = companyId,
            companyName = companyApi.getCompanyById(companyId).companyInformation.companyName,
            comment = comment,
            correlationId = correlationId,
        )
    }

    private fun assertAuthenticationViaJwtToken(userAuthentication: DatalandAuthentication) {
        if (userAuthentication !is DatalandJwtAuthentication) {
            throw AuthenticationMethodNotSupportedException()
        }
    }
}
