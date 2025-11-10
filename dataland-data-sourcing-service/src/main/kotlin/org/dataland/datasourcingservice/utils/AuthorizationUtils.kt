package org.dataland.datasourcingservice.utils

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

/**
 * Utility service class for complex authorization checks for incoming HTTP requests.
 */
@Component("AuthorizationUtils")
class AuthorizationUtils
    @Autowired
    constructor(
        private val dataSourcingRepository: DataSourcingRepository,
        private val companyRolesControllerApi: CompanyRolesControllerApi,
    ) {
        /**
         * Checks whether the specified user has a CompanyRole in the specified company.
         * @param companyId of the company in question
         * @return true if the user has a CompanyRole in the company, false otherwise
         */
        fun doesUserBelongToCompany(companyId: String): Boolean {
            val userId = DatalandAuthentication.fromContextOrNull()?.userId
            if (userId == null) return false

            return companyRolesControllerApi
                .getExtendedCompanyRoleAssignments(
                    userId = ValidationUtils.convertToUUID(userId),
                    companyId = ValidationUtils.convertToUUID(companyId),
                ).isNotEmpty()
        }

        /**
         * Checks whether the specified user belongs to the document collector company
         * associated with the specified data sourcing.
         * @param dataSourcingId of the data sourcing in question
         * @return true if the user belongs to the document collector company, false otherwise
         */
        fun doesUserBelongToDocumentCollector(dataSourcingId: String): Boolean {
            val dataSourcing = dataSourcingRepository.findByIdOrNull(ValidationUtils.convertToUUID(dataSourcingId))

            val documentCollectorId = dataSourcing?.documentCollector

            if (documentCollectorId == null) return false

            return doesUserBelongToCompany(documentCollectorId.toString())
        }
    }
