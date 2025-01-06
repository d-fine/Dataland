package org.dataland.datalandqaservice.utils

import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock.withAuthenticationMock

/**
 * Utility functions to be used in the tests.
 */
object UtilityFunctions {
    /**
     * Run the given block authenticated as a mock reviewer user.
     */
    inline fun withReviewerAuthentication(
        userId: String = "some-reviewer",
        block: () -> Unit,
    ) {
        withAuthenticationMock(
            userId,
            userId,
            setOf(DatalandRealmRole.ROLE_REVIEWER, DatalandRealmRole.ROLE_USER),
        ) {
            block()
        }
    }
}
