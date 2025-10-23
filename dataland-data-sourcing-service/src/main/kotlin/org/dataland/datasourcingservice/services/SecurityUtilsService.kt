package org.dataland.datasourcingservice.services

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

/**
 * Provides utility functions to be used in PreAuthorize blocks.
 */
@Service("SecurityUtilsService")
class SecurityUtilsService
    @Autowired
    constructor(
        private val requestRepository: RequestRepository,
        private val companyRolesManager: CompanyRolesManager,
    ) {
        /**
         * Returns true if and only if the currently authenticated user is asking for his/her own request
         */
        @Transactional(readOnly = true)
        fun isUserAskingForOwnRequest(requestId: String): Boolean {
            val userIdOfRequest = requestRepository.findById(UUID.fromString(requestId)).getOrNull()?.userId ?: return false
            val userIdRequester = UUID.fromString(SecurityContextHolder.getContext().authentication.name)
            return userIdOfRequest == userIdRequester
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

    }
