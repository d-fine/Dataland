package org.dataland.keycloakAdapter.utils

import org.dataland.datalandbackendutils.exceptions.AuthenticationMethodNotSupportedException
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

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
