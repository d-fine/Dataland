package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole

/**
 * A class for carrying out user authentication in different contexts.
 */
class UserAuthenticationTool {
    /**
     * Checks whether a user attempting to impersonate another user has the
     * right to do so. Currently, only admins have this right.
     *
     * @param userId If null, no impersonation attempt is made. Otherwise,
     * the current user attempts to impersonate the user with this user id.
     */
    fun checkAuthenticationForUserImpersonationAttempt(userId: String?) {
        if (
            userId != null &&
            !DatalandAuthentication.fromContext().roles.contains(
                DatalandRealmRole.ROLE_ADMIN,
            )
        ) {
            throw InsufficientRightsApiException(
                summary = "Insufficient rights for posting this request.",
                message = "Only admins can post requests in the name of other users.",
            )
        }
    }
}
