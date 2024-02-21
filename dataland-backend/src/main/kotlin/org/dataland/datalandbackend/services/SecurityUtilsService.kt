package org.dataland.datalandbackend.services

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

/**
 * Implements utility functions that can be used e.g., in PRE_AUTHORIZE
 * for several authentication use-cases
 */
@Service("SecurityUtilsService")
class SecurityUtilsService {
    /**
     * Returns true if and only if the currently authenticated user is asking for him/herself
     */
    fun amIAskingForMySelf(userIdRequester: String): Boolean {
        val userIdAuthenticated = SecurityContextHolder.getContext().authentication.name
        return userIdAuthenticated == userIdRequester
    }
}
