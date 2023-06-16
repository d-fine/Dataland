package org.dataland.datalandbackend.utils

import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole

/**
 * A user can bypass QA if
 * (a) the user has the uploader and QA role
 * (b) the user is an admin
 * This function checks these conditions
 */
fun canUserBypassQa(viewingUser: DatalandAuthentication?): Boolean {
    return viewingUser?.roles?.contains(DatalandRealmRole.ROLE_UPLOADER) ?: false &&
        viewingUser?.roles?.contains(DatalandRealmRole.ROLE_REVIEWER) ?: false ||
        viewingUser?.roles?.contains(DatalandRealmRole.ROLE_ADMIN) ?: false
}
