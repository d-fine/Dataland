package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRoleAssignmentExtended
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import java.util.UUID

/**
 * Service for retrieving users whose email addresses match the suffixes defined in a company's information.
 */
@Service
class EmailSuffixUserService
    @Autowired
    constructor(
        private val companyDataControllerApi: CompanyDataControllerApi,
        private val keycloakUserService: KeycloakUserService,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Returns all users whose email address ends with any of the suffixes defined in the company's emailSuffix attribute.
         * @param companyId the company to check
         * @return list of users with emails matching the suffixes
         */
        fun getUsersByCompanyEmailSuffix(companyId: UUID): List<CompanyRoleAssignmentExtended> {
            val result: List<CompanyRoleAssignmentExtended>
            val storedCompany =
                try {
                    companyDataControllerApi.getCompanyById(companyId.toString())
                } catch (ex: RestClientException) {
                    logger.warn("Company not found or error fetching company for $companyId", ex)
                    return emptyList()
                }
            val emailSuffixes: List<String> = storedCompany.companyInformation.emailSuffix?.filter { it.isNotBlank() } ?: emptyList()
            if (emailSuffixes.isEmpty()) {
                logger.info("No emailSuffix defined for company $companyId")
                result = emptyList()
            } else {
                val usersBySuffix =
                    emailSuffixes.flatMap { suffix ->
                        keycloakUserService.searchUsers(suffix)
                    }
                val uniqueUsers = usersBySuffix.distinctBy { it.userId }
                result =
                    uniqueUsers.map { user ->
                        CompanyRoleAssignmentExtended(
                            companyRole = CompanyRole.Member, // or null/default if needed
                            companyId = companyId.toString(),
                            userId = user.userId,
                            email = user.email ?: "",
                            firstName = user.firstName,
                            lastName = user.lastName,
                        )
                    }
            }
            return result
        }
    }
