package org.dataland.datalandcommunitymanager.utils
import org.dataland.datalandcommunitymanager.model.dataRequest.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.springframework.stereotype.Service

/**
 * Implementation of a request admin comment masker
 *
 * Masks the admin comment for non admins
 * */
@Service
class DataRequestMasker {
    /** This method checks if a user is an admin
     * @returns True if user is an admin, else otherwise
     */
    fun isUserAdmin(): Boolean {
        val authenticationContext = DatalandAuthentication.fromContext()
        return authenticationContext.roles.contains(DatalandRealmRole.ROLE_ADMIN)
    }

    /** This method hides the admin comment for non admins.
     * @param extendedStoredDataRequests list of extendedStoredDataRequests to check
     * @returns all data requests with modified admin comment
     */
    fun hideAdminCommentForNonAdmins(extendedStoredDataRequests: List<ExtendedStoredDataRequest>): List<ExtendedStoredDataRequest> {
        val modifiedExtendedStoredDataRequests =
            extendedStoredDataRequests.map { request ->
                if (isUserAdmin()) {
                    request
                } else {
                    request.copy(adminComment = null)
                }
            }
        return modifiedExtendedStoredDataRequests
    }

    /** This method hides the admin comment for non admins.
     * @param storedDataRequest a single stored dataRequest
     * @returns the modified storedDataRequest
     */
    fun hideAdminCommentForNonAdmins(storedDataRequest: StoredDataRequest): StoredDataRequest =
        if (!isUserAdmin()) {
            storedDataRequest.copy(adminComment = null)
        } else {
            storedDataRequest
        }
}
