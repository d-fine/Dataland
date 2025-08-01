package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.entities.CompanyRoleAssignmentEntity
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignment
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignmentExtended
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignmentId
import org.dataland.datalandcommunitymanager.repositories.CompanyRoleAssignmentRepository
import org.dataland.datalandcommunitymanager.services.messaging.CompanyOwnershipAcceptedEmailMessageBuilder
import org.dataland.datalandcommunitymanager.services.messaging.CompanyOwnershipRequestedEmailMessageBuilder
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service that handles all operations associated with company roles
 */
@Service("CompanyRolesManager")
class CompanyRolesManager(
    @Autowired private val companyInfoService: CompanyInfoService,
    @Autowired private val companyRoleAssignmentRepository: CompanyRoleAssignmentRepository,
    @Autowired private val companyOwnershipRequestedEmailMessageBuilder: CompanyOwnershipRequestedEmailMessageBuilder,
    @Autowired private val companyOwnershipAcceptedEmailMessageBuilder: CompanyOwnershipAcceptedEmailMessageBuilder,
    @Autowired private val keycloakUserService: KeycloakUserService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    val exceptionSummaryTextWhenRoleNotAssigned = "Company role is not assigned to user"

    /**
     * Assigns a company role for the specified company to the user.
     * @param companyRole that shall be assigned
     * @param companyId of the company for which the role is being assigned
     * @param userId of the user to whom a company role shall be assigned
     * @returns an entity that summarizes all current holders of the company role for the company
     */
    @Transactional
    fun assignCompanyRoleForCompanyToUser(
        companyRole: CompanyRole,
        companyId: String,
        userId: String,
    ): CompanyRoleAssignmentEntity {
        if (!keycloakUserService.isKeycloakUserId(userId)) {
            throw ResourceNotFoundApiException(
                summary = "Unknown user ID",
                message = "The user ID $userId does not exist on Keycloak.",
            )
        }
        val companyName = companyInfoService.getValidCompanyName(companyId)
        val correlationId = UUID.randomUUID().toString()

        val companyRoleAssignmentEntityOptional =
            companyRoleAssignmentRepository.findById(
                CompanyRoleAssignmentId(companyRole = companyRole, companyId = companyId, userId = userId),
            )
        return if (companyRoleAssignmentEntityOptional.isPresent) {
            logCompanyRoleAlreadyAssigned(companyRole, companyId, userId)
            companyRoleAssignmentEntityOptional.get()
        } else {
            deleteAllCompanyRoleAssignmentsForCompanyAndUser(companyId = companyId, userId = userId)
            val savedEntity =
                saveCompanyRoleAssignment(companyRole = companyRole, companyId = companyId, userId = userId)
            logCompanyRoleHasBeenAssigned(companyRole, companyId, userId)
            if (companyRole == CompanyRole.CompanyOwner) {
                companyOwnershipAcceptedEmailMessageBuilder.buildCompanyOwnershipAcceptanceExternalEmailAndSendCEMessage(
                    userId, companyId, companyName, correlationId,
                )
            }
            savedEntity
        }
    }

    /**
     * Converts the given CompanyRoleAssignment objects to CompanyRoleAssignmentExtended objects by
     * looking up the relevant info (email, first name, last name) via Keycloak.
     */
    fun convertToExtendedCompanyRoleAssignments(companyRoleAssignments: List<CompanyRoleAssignment>): List<CompanyRoleAssignmentExtended> {
        val extendedCompanyRoleAssignments = mutableListOf<CompanyRoleAssignmentExtended>()
        companyRoleAssignments.forEach {
            val keycloakUserInfo = keycloakUserService.getUser(it.userId)

            extendedCompanyRoleAssignments.add(
                CompanyRoleAssignmentExtended(
                    companyRole = it.companyRole,
                    companyId = it.companyId,
                    userId = it.userId,
                    email = keycloakUserInfo.email ?: "",
                    firstName = keycloakUserInfo.firstName,
                    lastName = keycloakUserInfo.lastName,
                ),
            )
        }
        return extendedCompanyRoleAssignments
    }

    private fun saveCompanyRoleAssignment(
        companyRole: CompanyRole,
        companyId: String,
        userId: String,
    ): CompanyRoleAssignmentEntity =
        companyRoleAssignmentRepository.save(
            CompanyRoleAssignmentEntity(companyRole = companyRole, companyId = companyId, userId = userId),
        )

    private fun deleteAllCompanyRoleAssignmentsForCompanyAndUser(
        companyId: String,
        userId: String,
    ) {
        companyRoleAssignmentRepository.deleteAllRolesByCompanyIdAndUserId(companyId, userId)
    }

    /**
     * Logs that a user is already assigned to the specified company role for the company.
     * @param companyRole that is already assigned
     * @param companyId of the company for which the role is already assigned
     * @param userId that is already assigned the company role to
     */
    private fun logCompanyRoleAlreadyAssigned(
        companyRole: CompanyRole,
        companyId: String,
        userId: String,
    ) {
        logger.info(
            "User with Id $userId already has the company role $companyRole for the company with " +
                "the company Id $companyId",
        )
    }

    /**
     * Logs that a user has been assigned to the specified company role for the company.
     * @param companyRole that has been assigned
     * @param companyId of the company for which the role has been assigned
     * @param userId that has been assigned the company role to
     */
    private fun logCompanyRoleHasBeenAssigned(
        companyRole: CompanyRole,
        companyId: String,
        userId: String,
    ) {
        logger.info(
            "User with Id $userId has received the company role $companyRole for the company with " +
                "the company Id $companyId",
        )
    }

    /**
     * Returns a list of company role assignment entities for the specified companyRole, companyId and userId
     * @param companyRole to filter for
     * @param companyId to filter for
     * @param userId to filter for
     * @returns the company role assignment entities
     */
    @Transactional
    fun getCompanyRoleAssignmentsByParameters(
        companyRole: CompanyRole?,
        companyId: String?,
        userId: String?,
    ): List<CompanyRoleAssignmentEntity> {
        if (companyId != null) {
            companyInfoService.assertCompanyIdIsValid(companyId)
        }
        return companyRoleAssignmentRepository.getCompanyRoleAssignmentsByProvidedParameters(
            companyId = companyId, userId = userId, companyRole = companyRole,
        )
    }

    private fun throwExceptionDueToRoleNotAssignedToUser(
        companyRole: CompanyRole,
        companyId: String,
        userId: String,
    ): Unit =
        throw ResourceNotFoundApiException(
            exceptionSummaryTextWhenRoleNotAssigned,
            "The role $companyRole for company $companyId is not assigned to user $userId",
        )

    /**
     * Removes a company role assignment
     * @param companyRole of the assignment that shall be removed
     * @param companyId of the company for which the assignment is valid
     * @param userId of the user whose company role assignment shall be removed
     */
    @Transactional
    fun removeCompanyRoleForCompanyFromUser(
        companyRole: CompanyRole,
        companyId: String,
        userId: String,
    ) {
        companyInfoService.assertCompanyIdIsValid(companyId)
        val id = CompanyRoleAssignmentId(companyRole = companyRole, companyId = companyId, userId = userId)
        val companyRoleAssignmentEntityOptional = companyRoleAssignmentRepository.findById(id)
        if (companyRoleAssignmentEntityOptional.isPresent) {
            companyRoleAssignmentRepository.deleteById(id)
        } else {
            throwExceptionDueToRoleNotAssignedToUser(companyRole, companyId, userId)
        }
    }

    /**
     * Checks whether a specified user has the defined company role for a given company. If not, an exception is thrown.
     * @param companyRole to check for
     * @param companyId of the company for which the user might have the company role
     * @param userId of the user that might be assigned the company role to
     */
    @Transactional(readOnly = true)
    fun validateIfCompanyRoleForCompanyIsAssignedToUser(
        companyRole: CompanyRole,
        companyId: String,
        userId: String,
    ) {
        companyInfoService.assertCompanyIdIsValid(companyId)
        val id = CompanyRoleAssignmentId(companyRole = companyRole, companyId = companyId, userId = userId)
        if (!companyRoleAssignmentRepository.existsById(id)) {
            throwExceptionDueToRoleNotAssignedToUser(companyRole, companyId, userId)
        }
    }

    /**
     * Verifies if the specified company as at least one company owner. If not, it throws an exception.
     * @param companyId of the company to check for
     */
    @Transactional(readOnly = true)
    fun validateIfCompanyHasAtLeastOneCompanyOwner(companyId: String) {
        companyInfoService.assertCompanyIdIsValid(companyId)
        val companyRoleAssignments = getCompanyRoleAssignmentsByParameters(CompanyRole.CompanyOwner, companyId, null)
        if (companyRoleAssignments.isEmpty()) {
            throw ResourceNotFoundApiException(
                "Company has no company owner.",
                "Company with $companyId has no company owner(s).",
            )
        }
    }

    /**
     * Verifies if the user with the specified userId has the role CompanyOwner or MemberAdmin for
     * at least one company on Dataland.
     */
    @Transactional(readOnly = true)
    fun currentUserIsOwnerOrAdminOfAtLeastOneCompany(): Boolean {
        val userId = DatalandAuthentication.fromContextOrNull()?.userId
        if (userId == null) return false
        return companyRoleAssignmentRepository
            .getCompanyRoleAssignmentsByProvidedParameters(
                companyId = null, userId = userId, companyRole = null,
            ).any {
                it.companyRole in listOf(CompanyRole.CompanyOwner, CompanyRole.MemberAdmin)
            }
    }

    /**
     * Sends a company ownership request if company owner role is not assigned to user yet
     * @param companyId of the company for which company ownership is being requested
     * @param userAuthentication of the user who wants to become company owner
     * @param comment is an optional text that can be submitted to give more info about the request
     * @param correlationId to make this operation traceable
     */
    @Transactional(readOnly = true)
    fun triggerCompanyOwnershipRequest(
        companyId: String,
        userAuthentication: DatalandAuthentication,
        comment: String?,
        correlationId: String,
    ) {
        val datalandJwtAuthentication = assertAuthenticationViaJwtToken(userAuthentication)
        val companyName = companyInfoService.getValidCompanyName(companyId)
        val userId = datalandJwtAuthentication.userId
        try {
            validateIfCompanyRoleForCompanyIsAssignedToUser(CompanyRole.CompanyOwner, companyId, userId)
        } catch (e: ResourceNotFoundApiException) {
            if (e.summary == exceptionSummaryTextWhenRoleNotAssigned) {
                companyOwnershipRequestedEmailMessageBuilder.buildCompanyOwnershipInternalEmailMessageAndSendCEMessage(
                    userAuthentication = datalandJwtAuthentication,
                    datalandCompanyId = companyId,
                    companyName = companyName,
                    comment = comment,
                    correlationId = correlationId,
                )
                return
            } else {
                throw e
            }
        }
        throw InvalidInputApiException(
            "User is already a company owner for company.",
            "User with id: $userId is already a company owner of company with id: $companyId",
        )
    }

    private fun assertAuthenticationViaJwtToken(userAuthentication: DatalandAuthentication): DatalandJwtAuthentication {
        if (userAuthentication !is DatalandJwtAuthentication) {
            throw AuthenticationMethodNotSupportedException()
        } else {
            return userAuthentication
        }
    }
}
