package org.dataland.datasourcingservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datasourcingservice.model.request.ExtendedStoredRequest
import org.dataland.datasourcingservice.model.request.RequestSearchFilter
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.collections.map

/**
 * Service class for handling request queries.
 */
@Service("RequestQueryManager")
class RequestQueryManager
    @Autowired
    constructor(
        private val requestRepository: RequestRepository,
        private val companyDataController: CompanyDataControllerApi,
        private val keycloakUserService: KeycloakUserService,
    ) {
        /**
         * Method to get all data requests based on filters.
         * @param filter the search filter containing relevant search parameters
         * @param chunkIndex the index of the chunked results which should be returned
         * @param chunkSize the size of entries per chunk which should be returned
         * @return all filtered data requests
         */
        @Transactional
        fun searchRequests(
            filter: RequestSearchFilter<UUID>,
            chunkSize: Int = 100,
            chunkIndex: Int = 0,
        ): List<ExtendedStoredRequest>? {
            val extendedStoredDataRequests =
                requestRepository
                    .findByListOfIdsAndFetchDataSourcingEntity(
                        requestRepository
                            .searchRequests(
                                searchFilter = filter,
                                PageRequest.of(
                                    chunkIndex,
                                    chunkSize,
                                    Sort.by(
                                        Sort.Order.desc("creationTimestamp"),
                                        Sort.Order.asc("companyId"),
                                        Sort.Order.desc("reportingPeriod"),
                                        Sort.Order.asc("state"),
                                    ),
                                ),
                                companyIds = companyIdsMatchingSearchString(filter.companySearchString),
                                userIds = setupEmailAddressFilter(filter.emailAddress),
                            ).content,
                    ).map { entity ->
                        val dto = entity.toExtendedStoredRequest()
                        dto.copy(
                            companyName = companyDataController.getCompanyById(entity.companyId.toString()).companyInformation.companyName,
                            userEmailAddress = keycloakUserService.getUser(entity.userId.toString()).email,
                        )
                    }

            return extendedStoredDataRequests
        }

        /**
         * Get company IDs matching the company search string.
         * @param companySearchString the company search string
         * @return list of company IDs matching the search string
         */
        private fun companyIdsMatchingSearchString(companySearchString: String?): List<UUID>? =
            if (companySearchString != null) {
                companyDataController
                    .getCompanies(
                        searchString = companySearchString,
                        chunkIndex = 0,
                        chunkSize = Int.MAX_VALUE,
                    ).map { convertToUUID(it.companyId) }
            } else {
                null
            }

        /**
         * Set up email address filter by searching for user IDs matching the email address search string.
         * @param emailAddressSearchString the email address search string
         * @return list of user IDs matching the email address search string
         */
        private fun setupEmailAddressFilter(emailAddressSearchString: String?): List<UUID>? =
            emailAddressSearchString
                ?.let {
                    keycloakUserService.searchUsers(it)
                }?.map { convertToUUID(it.userId) }

        /**
         * Search for requests based on userId
         * @param userId to filter by
         * @return list of matching ExtendedStoredRequest objects
         */
        @Transactional
        fun getRequestsByUser(userId: UUID): List<ExtendedStoredRequest> =
            requestRepository
                .findByUserId(userId)
                .map { entity ->
                    val dto = entity.toExtendedStoredRequest()
                    dto.copy(
                        companyName = companyDataController.getCompanyById(entity.companyId.toString()).companyInformation.companyName,
                        userEmailAddress = keycloakUserService.getUser(entity.userId.toString()).email,
                    )
                }

        /**
         * Get requests for requesting user
         * @return list of matching ExtendedStoredRequest objects
         */
        @Transactional
        fun getRequestsForRequestingUser(): List<ExtendedStoredRequest> {
            val userId = DatalandAuthentication.fromContext().userId
            return getRequestsByUser(
                UUID.fromString(userId),
            )
        }

        /**
         * Get the number of requests that match the optional filters.
         * @param requestSearchFilter to filter by
         * @return the number of matching requests
         */
        @Transactional(readOnly = true)
        fun getNumberOfRequests(requestSearchFilter: RequestSearchFilter<UUID>): Int =
            requestRepository.getNumberOfRequests(
                requestSearchFilter,
                companyIds = companyIdsMatchingSearchString(requestSearchFilter.companySearchString),
                userIds = setupEmailAddressFilter(requestSearchFilter.emailAddress),
            )
    }
