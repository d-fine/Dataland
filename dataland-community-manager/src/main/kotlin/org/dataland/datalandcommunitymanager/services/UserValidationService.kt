package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.model.BasicUserInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service class for validating user-related information.
 */
@Service("UserValidationService")
class UserValidationService(
    @Autowired private val keycloakUserService: KeycloakUserService,
) {
    /**
     * Checks if there is a registered Dataland user with the specified email address
     * and, if so, returns basic information on that user gathered from Keycloak.
     * @email the email address to validate
     */
    fun validateEmailAddress(email: String): BasicUserInformation {
        val keycloakUserInfo = keycloakUserService.findUserByEmail(email)
        if (keycloakUserInfo == null) {
            throw ResourceNotFoundApiException(
                summary = "No user found with this email",
                message = "There is no registered Dataland user with this email address.",
            )
        }
        return BasicUserInformation(
            keycloakUserInfo.userId,
            keycloakUserInfo.email,
            keycloakUserInfo.firstName,
            keycloakUserInfo.lastName,
        )
    }
}
