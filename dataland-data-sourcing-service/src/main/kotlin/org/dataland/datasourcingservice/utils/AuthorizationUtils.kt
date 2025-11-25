package org.dataland.datasourcingservice.utils

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Utility service class for complex authorization checks for incoming HTTP requests.
 */
@Component("AuthorizationUtils")
class AuthorizationUtils
    @Autowired
    constructor(
        private val companyRolesControllerApi: CompanyRolesControllerApi,
        private val dataSourcingManager: DataSourcingManager,
    ) {
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
