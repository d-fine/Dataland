package org.dataland.datasourcingservice.services

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
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
        private val companyRolesControllerApi: CompanyRolesControllerApi,
        private val dataSourcingManager: DataSourcingManager,
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
         * Checks whether the logged-in user has a CompanyRole in the specified company.
         * @param companyId of the company in question
         * @return true if the user has a CompanyRole in the company, false otherwise
         */
        fun doesUserBelongToCompany(companyId: String): Boolean {
            val userId = DatalandAuthentication.fromContextOrNull()?.userId ?: return false

            return companyRolesControllerApi
                .getExtendedCompanyRoleAssignments(
                    userId = ValidationUtils.convertToUUID(userId),
                    companyId = ValidationUtils.convertToUUID(companyId),
                ).isNotEmpty()
        }

        /**
         * Checks whether the logged-in user belongs to the document collector company
         * associated with the specified data sourcing.
         * @param dataSourcingId of the data sourcing in question
         * @return true if the user belongs to the document collector company, false otherwise
         */
        fun doesUserBelongToDocumentCollector(dataSourcingId: String): Boolean {
            val dataSourcing = dataSourcingManager.getStoredDataSourcing(ValidationUtils.convertToUUID(dataSourcingId))

            return doesUserBelongTo(dataSourcing) { it.documentCollector }
        }

        /**
         * Generic helper function to check whether the logged-in user belongs to a company
         * associated with the specified data sourcing.
         * @param dataSourcing in question
         * @param companySelector function to select the company ID from the data sourcing
         * @return true if the user belongs to the selected company, false otherwise
         */
        private fun doesUserBelongTo(
            dataSourcing: StoredDataSourcing,
            companySelector: (StoredDataSourcing) -> String?,
        ): Boolean =
            dataSourcing.let { ds ->
                companySelector(ds)?.let { companyId -> doesUserBelongToCompany(companyId) } ?: false
            }

        /**
         * Checks whether the logged-in user can patch the state of the specified data sourcing
         * to the specified state.
         * @param dataSourcingId of the data sourcing in question
         * @param state to which the data sourcing state should be patched
         * @return true if the user can patch the state, false otherwise
         */
        fun canUserPatchState(
            dataSourcingId: String,
            state: DataSourcingState,
        ): Boolean {
            val dataSourcing = dataSourcingManager.getStoredDataSourcing(ValidationUtils.convertToUUID(dataSourcingId))
            val result =
                when (state) {
                    DataSourcingState.DocumentSourcingDone -> doesUserBelongTo(dataSourcing) { it.documentCollector }
                    DataSourcingState.NonSourceable ->
                        doesUserBelongTo(dataSourcing) { it.documentCollector } ||
                            doesUserBelongTo(dataSourcing) { it.dataExtractor }
                    else -> false
                }
            return result
        }
    }
