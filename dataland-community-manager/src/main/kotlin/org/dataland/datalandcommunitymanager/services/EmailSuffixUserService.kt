package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
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
        fun getUsersByCompanyEmailSuffix(companyId: UUID): List<KeycloakUserInfo> {
            val result: List<KeycloakUserInfo>
            val storedCompany =
                try {
                    companyDataControllerApi.getCompanyById(companyId.toString())
                } catch (ex: ResourceNotFoundApiException) {
                    logger.warn("Company not found or error fetching company for $companyId", ex)
                    return emptyList()
                }
            val emailSuffixes: List<String> = storedCompany.companyInformation.emailSuffixes?.filter { it.isNotBlank() } ?: emptyList()
            if (emailSuffixes.isEmpty()) {
                logger.info("No email suffix defined for company $companyId")
                result = emptyList()
            } else {
                val usersBySuffix =
                    emailSuffixes.flatMap { suffix ->
                        keycloakUserService.searchUsersByEmailSuffix(suffix)
                    }
                result = usersBySuffix.distinctBy { it.userId }
            }
            return result
        }
    }
