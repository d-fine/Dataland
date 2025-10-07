package org.dataland.keycloakAdapter.utils

import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Utility service class for processing requests in services using Keycloak for authentication.
 */
@Service("KeycloakAdapterRequestProcessingUtils")
class KeycloakAdapterRequestProcessingUtils(
    @Autowired private val keycloakUserService: KeycloakUserService,
) {
    /**
     * We want to avoid users trying to create a new request from using other authentication methods
     * than jwt-authentication, such as api-key-authentication.
     */
    fun throwExceptionIfNotJwtAuth() {
        if (DatalandAuthentication.fromContext() !is DatalandJwtAuthentication) {
            throw AuthenticationMethodNotSupportedException()
        }
    }

    /**
     * Check if the user with the given userId has the role of a premium user.
     * Retrieve the roles from Keycloak using the KeycloakUserService.
     * @param userId the userId of the user in question
     * @return true if the user has the role of a premium user, false otherwise
     */
    fun userIsPremiumUser(userId: String): Boolean = keycloakUserService.getUserRoleNames(userId).contains("ROLE_PREMIUM_USER")
}
