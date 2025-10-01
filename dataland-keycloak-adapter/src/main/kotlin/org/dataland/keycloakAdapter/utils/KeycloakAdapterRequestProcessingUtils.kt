package org.dataland.keycloakAdapter.utils

import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
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
     * If the userId belongs to the currently authenticated user, check the roles from the authentication context.
     * Otherwise, retrieve the roles from Keycloak using the KeycloakUserService.
     * @param userId the userId of the user in question
     * @return true if the user has the role of a premium user, false otherwise
     */
    fun userIsPremiumUser(userId: String): Boolean {
        val authenticationOfLoggedInUser = DatalandAuthentication.fromContext()
        return if (userId == authenticationOfLoggedInUser.userId) {
            authenticationOfLoggedInUser.roles.contains(
                DatalandRealmRole.ROLE_PREMIUM_USER,
            )
        } else {
            keycloakUserService.getUserRoleNames(userId).contains("ROLE_PREMIUM_USER")
        }
    }
}
