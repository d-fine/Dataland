package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.CompanyRoleAssignmentRepository
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Implements utility functions that can be used e.g., in PRE_AUTHORIZE
 * for several authentication use-cases
 */
@Suppress("TooManyFunctions")
@Service("SecurityUtilsService")
class SecurityUtilsService(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val companyRoleAssignmentRepository: CompanyRoleAssignmentRepository,
    @Autowired private val companyRolesManager: CompanyRolesManager,
    @Autowired private val dataRequestQueryManager: DataRequestQueryManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val roleModificationPermissionsMap =
        mapOf(
            CompanyRole.CompanyOwner to enumValues<CompanyRole>().toList(),
            CompanyRole.DataUploader to emptyList(),
            CompanyRole.MemberAdmin to listOf(CompanyRole.MemberAdmin, CompanyRole.Member),
            CompanyRole.Member to emptyList(),
        )

    /**
     * Returns true if and only if the currently authenticated user is asking for him/herself
     */
    fun isUserRequestingForOwnId(userIdRequester: String?): Boolean {
        val userIdAuthenticated = SecurityContextHolder.getContext().authentication.name
        return userIdAuthenticated == userIdRequester
    }

    /**
     * Returns true if and only if the currently authenticated user is asking for his/her own request
     */
    @Transactional
    fun isUserAskingForOwnRequest(requestId: UUID): Boolean {
        val userIdOfRequest = dataRequestRepository.findById(requestId.toString()).get().userId
        val userIdRequester = SecurityContextHolder.getContext().authentication.name
        return (userIdOfRequest == userIdRequester)
    }

    /**
     * Returns true if the request status is subject to change and conditions are met.
     * This is the case when the request is to be changed from answered to either closed or open
     * or if the status is changed to withdrawn.
     */
    @Transactional
    fun isRequestStatusChangeableByUser(
        requestId: UUID,
        requestStatusToPatch: RequestStatus?,
    ): Boolean {
        if (requestStatusToPatch == null) return true
        val currentRequestStatus = dataRequestRepository.findById(requestId.toString()).get().requestStatus
        val statusChangeFromAnsweredToResolved =
            currentRequestStatus == RequestStatus.Answered && requestStatusToPatch == RequestStatus.Resolved
        val statusChangeFromAnsweredToOpen =
            currentRequestStatus == RequestStatus.Answered && requestStatusToPatch == RequestStatus.Open
        val statusChangeFromAnsweredToWithdrawn =
            currentRequestStatus == RequestStatus.Answered && requestStatusToPatch == RequestStatus.Withdrawn
        val statusChangeFromOpenToWithdrawn =
            currentRequestStatus == RequestStatus.Open && requestStatusToPatch == RequestStatus.Withdrawn
        val statusChangeFromNonSourceableToOpen =
            currentRequestStatus == RequestStatus.NonSourceable && requestStatusToPatch == RequestStatus.Open
        return (
            statusChangeFromAnsweredToResolved ||
                statusChangeFromAnsweredToOpen ||
                statusChangeFromAnsweredToWithdrawn ||
                statusChangeFromOpenToWithdrawn ||
                statusChangeFromNonSourceableToOpen
        )
    }

    /**
     * Returns true if the request message history is subject to change and conditions are met.
     * This is the case when no contacts are provided or
     * the request status is open or answered and patched to open as well
     */
    @Transactional
    fun isRequestMessageHistoryChangeableByUser(
        requestId: UUID,
        requestStatusToPatch: RequestStatus?,
        contacts: Set<String>?,
        message: String?,
    ): Boolean {
        if (contacts == null) return true
        val currentRequestStatus = dataRequestRepository.findById(requestId.toString()).get().requestStatus
        return message != null &&
            (
                currentRequestStatus == RequestStatus.Open ||
                    (currentRequestStatus == RequestStatus.Answered && requestStatusToPatch == RequestStatus.Open)
            )
    }

    /**
     * Returns true if the user is member of the company
     * @param companyId dataland companyId
     */
    @Transactional
    fun isUserMemberOfTheCompany(companyId: UUID?): Boolean {
        val userId = SecurityContextHolder.getContext().authentication.name
        if (companyId == null || userId == null) return false
        return companyRoleAssignmentRepository
            .getCompanyRoleAssignmentsByProvidedParameters(
                companyId = companyId.toString(), userId = userId, companyRole = null,
            ).isNotEmpty()
    }

    /**
     * Returns true if the user has the rights to add/remove the companyRole
     * @param companyId dataland companyId
     * @param companyRoleToModify the companyRole to add/remove
     */
    @Transactional
    fun hasUserPermissionToModifyTheCompanyRole(
        companyId: UUID,
        companyRoleToModify: CompanyRole,
    ): Boolean {
        val userId = SecurityContextHolder.getContext().authentication.name ?: return false
        val userCompanyRoles =
            companyRoleAssignmentRepository.getCompanyRoleAssignmentsByProvidedParameters(
                companyId = companyId.toString(), userId = userId, companyRole = null,
            )
        return userCompanyRoles.any {
            roleModificationPermissionsMap[it.companyRole]?.contains(companyRoleToModify) == true
        }
    }

    /**
     * Returns true if the requesting user is company owner
     * @param requestId the requestId for which a company ownership check should be done
     */
    fun isUserCompanyOwnerForRequestId(requestId: String): Boolean {
        val requestEntity = dataRequestQueryManager.getDataRequestById(requestId)
        return isUserCompanyOwnerForCompanyId(requestEntity.datalandCompanyId)
    }

    /**
     * Returns true if the requesting user is company owner
     * @param companyId the company Id for which ownership should be tested
     */
    fun isUserCompanyOwnerForCompanyId(companyId: String?): Boolean =
        if (companyId.isNullOrBlank()) {
            false
        } else {
            val userId = DatalandAuthentication.fromContext().userId
            try {
                companyRolesManager.validateIfCompanyRoleForCompanyIsAssignedToUser(
                    companyRole = CompanyRole.CompanyOwner,
                    companyId = companyId, userId = userId,
                )
                true
            } catch (e: ResourceNotFoundApiException) {
                logger.error("The user is not the company owner for the specified company. Catched error: $e")
                false
            }
        }

    /**
     * Returns true if the user is not trying to patch the arguments
     * @param accessStatus the accessStatus of the patch
     * @param requestPriority the requestPriority of the patch
     * @param adminComment the adminComment of the patch
     *
     */
    fun isNotTryingToPatch(
        accessStatus: AccessStatus?,
        requestPriority: RequestPriority?,
        adminComment: String?,
    ): Boolean = accessStatus == null && requestPriority == null && adminComment == null

    /**
     * Returns true if user is not trying to patch any of the arguments
     * @param requestStatus the request status of the patch request
     * @param contacts the contacts of the patch request
     * @param message the message of the patch request
     * @param requestPriority the requestPriority of the patch
     * @param adminComment the adminComment of the patch request
     */
    fun isNotTryingToPatch(
        requestStatus: RequestStatus?,
        contacts: Set<String>?,
        message: String?,
        requestPriority: RequestPriority?,
        adminComment: String?,
    ): Boolean {
        val isNotTryingToPatchStatusContactsMessage =
            requestStatus == null && contacts.isNullOrEmpty() && message.isNullOrBlank()
        val isNotTryingToPatchPriorityAdminComment = requestPriority == null && adminComment == null
        return isNotTryingToPatchStatusContactsMessage && isNotTryingToPatchPriorityAdminComment
    }
}
