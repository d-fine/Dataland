package org.dataland.keycloakAdapter

/**
 * A collection of all the Dataland Roles in the
 * Datalandsecurity realm. Can be used in conjunction with the
 * Authentication.hasRole extension function to check if a user has a certain role
 */
enum class DatalandRealmRoles {
    ROLE_USER, ROLE_UPLOADER, ROLE_ADMIN
}
