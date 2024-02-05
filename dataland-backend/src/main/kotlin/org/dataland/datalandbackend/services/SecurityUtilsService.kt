package org.dataland.datalandbackend.services

import okhttp3.internal.concurrent.TaskRunner.Companion.logger
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.stereotype.Service

/**
 * Implements utility functions that can be used e.g., in PRE_AUTHORIZE
 * for several authentication use-cases
 */
@Service("SecurityUtilsService")
class SecurityUtilsService {
    /**
     * Returns true if and only if the user is authenticated
     */
    fun isUserAuthenticated(userId: String): Boolean {
        val currentAuthentication = DatalandAuthentication.fromContextOrNull() ?: return false
        logger.info("currentAuthentication $currentAuthentication")
        return currentAuthentication.isAuthenticated
    }
    /**
     * Returns true if and only if the currently authenticated user has the provided user id
     */
    fun isUserIdOfRequestingUser(userId: String): Boolean {
        val currentAuthentication = DatalandAuthentication.fromContextOrNull() ?: return false
        return currentAuthentication.userId == userId && userId.isNotBlank()
    }
}
