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
 * Service class for handling queries pertaining to email addresses.
 */
@Service("EmailAddressService")
class EmailAddressService
    @Autowired
    constructor(
        private val companyDataControllerApi: CompanyDataControllerApi,
        private val keycloakUserService: KeycloakUserService,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Checks if there is a registered Dataland user with the specified email address
         * and, if so, returns basic information on that user gathered from Keycloak.
         * @email the email address to validate
         */
        fun validateEmailAddress(email: String): KeycloakUserInfo {
            val keycloakUserInfo = keycloakUserService.findUserByEmail(email)
            if (keycloakUserInfo == null) {
                throw ResourceNotFoundApiException(
                    summary = "No user found with this email",
                    message = "There is no registered Dataland user with this email address.",
                )
            }
            return KeycloakUserInfo(
                userId = keycloakUserInfo.userId,
                email = keycloakUserInfo.email,
                firstName = keycloakUserInfo.firstName,
                lastName = keycloakUserInfo.lastName,
            )
        }

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
