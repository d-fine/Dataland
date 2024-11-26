package org.dataland.datalandcommunitymanager.utils
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Implementation of a request admin comment masker
 *
 * Masks the admin comment for non admins
 * */
@Component
class DataRequestMasker
    @Autowired
    constructor(
        private val keycloakUserControllerApiService: KeycloakUserService,
    ) {
        /** This method checks if a user is an admin
         * @returns True if user is an admin, else false
         */
        fun isUserAdmin(): Boolean {
            val authenticationContext = DatalandAuthentication.fromContext()
            return authenticationContext.roles.contains(DatalandRealmRole.ROLE_ADMIN)
        }

        /** This method hides the admin comment for non admins
         * @param extendedStoredDataRequests list of extendedStoredDataRequests to check
         * @returns all data requests with hidden admin comment if requester is not an admin
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

        /** This method adds the email address if the user is allowed to see it
         * @param extendedStoredDataRequests list of extendedStoredDataRequests to check
         * @param ownedCompanyIdsByUser the company ids for which the user is a company owner
         * @param filter the search filter containing relevant search parameters
         * @returns all data requests with the corresponding email address if the user is allowed to see it
         */
        fun addEmailAddressIfAllowedToSee(
            extendedStoredDataRequests: List<ExtendedStoredDataRequest>,
            ownedCompanyIdsByUser: List<String>,
            filter: DataRequestsFilter,
        ): List<ExtendedStoredDataRequest> {
            val usersMatchingEmailFilter = filter.setupEmailAddressFilter(keycloakUserControllerApiService)
            val userIdsToEmails = usersMatchingEmailFilter.associate { it.userId to it.email }.toMutableMap()

            val extendedStoredDataRequestsWithMails =
                extendedStoredDataRequests.map {
                    val allowedToSeeEmailAddress =
                        isUserAdmin() ||
                            (
                                ownedCompanyIdsByUser.contains(it.datalandCompanyId) &&
                                    it.accessStatus != AccessStatus.Public
                            )

                    it.userEmailAddress =
                        it.userId
                            .takeIf { allowedToSeeEmailAddress }
                            ?.let { userIdsToEmails.getOrPut(it) { keycloakUserControllerApiService.getUser(it).email ?: "" } }

                    it
                }
            return extendedStoredDataRequestsWithMails
        }

        /** This method hides the admin comment for non admins
         * @param storedDataRequest a single stored dataRequest
         * @returns the request with hidden admin comment if requester is not an admin
         */
        fun hideAdminCommentForNonAdmins(storedDataRequest: StoredDataRequest): StoredDataRequest =
            if (!isUserAdmin()) {
                storedDataRequest.copy(adminComment = null)
            } else {
                storedDataRequest
            }
    }
