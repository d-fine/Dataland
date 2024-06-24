package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.CompanyRoleAssignmentRepository
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Implements utility functions that can be used e.g., in PRE_AUTHORIZE
 * for several authentication use-cases
 */
@Service("SecurityUtilsService")
class SecurityUtilsService(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val companyRoleAssignmentRepository: CompanyRoleAssignmentRepository,
) {
    /**
     * Returns true if and only if the currently authenticated user is asking for him/herself
     */
    fun isUserRequestingForOwnId(userIdRequester: String): Boolean {
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
        return (
            statusChangeFromAnsweredToResolved || statusChangeFromAnsweredToOpen ||
                statusChangeFromAnsweredToWithdrawn || statusChangeFromOpenToWithdrawn
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
        return message != null && (
            currentRequestStatus == RequestStatus.Open ||
                (currentRequestStatus == RequestStatus.Answered && requestStatusToPatch == RequestStatus.Open)
            )
    }

    /**
     * Returns true if the user is member of the company
     * @param companyId dataland companyId
     */
    @Transactional
    fun isUserMemberOfTheCompany(
        companyId: UUID,
    ): Boolean {
        val userId = SecurityContextHolder.getContext().authentication.name ?: return false
        return companyRoleAssignmentRepository.findByCompanyIdAndUserId(companyId.toString(), userId).isNotEmpty()
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
            companyRoleAssignmentRepository.findByCompanyIdAndUserId(companyId.toString(), userId)
        val rolePermissionsMap = mapOf(
            CompanyRole.CompanyOwner to enumValues<CompanyRole>().toList(),
            CompanyRole.CompanyUploader to emptyList(),
            CompanyRole.ExternalCompanyUploader to emptyList(),
            CompanyRole.CompanyUserAdmin to listOf(CompanyRole.CompanyUserAdmin, CompanyRole.CompanyMember),
            CompanyRole.CompanyMember to emptyList(),
        )
        var hasUserPermission = false
        userCompanyRoles.forEach {
            if (rolePermissionsMap[it.companyRole]?.contains(companyRoleToModify) == true) hasUserPermission = true
        }
        return hasUserPermission
    }
}
