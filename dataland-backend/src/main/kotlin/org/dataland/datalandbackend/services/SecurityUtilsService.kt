package org.dataland.datalandbackend.services

import okhttp3.internal.concurrent.TaskRunner.Companion.logger
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.stereotype.Service
import java.nio.file.AccessDeniedException

/**
 * Implements utility functions that can be used e.g., in PRE_AUTHORIZE
 * for several authentication use-cases
 */
@Service("SecurityUtilsService")
class SecurityUtilsService {
    /**
     * Returns true if and only if the user is authenticated
     */
    fun isUserAuthenticated(): Boolean {
        val currentAuthentication = DatalandAuthentication.fromContextOrNull() ?: return false
        logger.info("Current authentication of User $currentAuthentication")
        if (!currentAuthentication.isAuthenticated) {
            throw AccessDeniedException(
                "Access Denied " +
                    "Access to this resource has been denied. Please contact support if you need further information",

            )
        } else {
            return true
        }
    }

    /**
     * Returns true if and only if the currently authenticated user has the provided user id
     */
    fun isUserIdOfRequestingUser(userId: String): Boolean {
        val currentAuthentication = DatalandAuthentication.fromContextOrNull() ?: return false
        return currentAuthentication.userId == userId && userId.isNotBlank()
    }
}
