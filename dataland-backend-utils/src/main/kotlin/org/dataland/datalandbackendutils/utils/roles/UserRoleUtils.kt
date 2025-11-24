package org.dataland.datalandbackendutils.utils.roles

import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole

/**
 * Checks whether the current user has the admin role in the Dataland security realm.
 *
 * @return true if the user has the ROLE_ADMIN role, false otherwise or if the user context is null.
 */
fun isUserAdmin(): Boolean = DatalandAuthentication.fromContextOrNull()?.roles?.contains(DatalandRealmRole.ROLE_ADMIN) ?: false
