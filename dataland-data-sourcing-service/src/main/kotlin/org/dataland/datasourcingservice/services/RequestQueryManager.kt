package org.dataland.datasourcingservice.services

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datasourcingservice.model.enums.RequestState
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
     * @param ownedCompanyIdsByUser the company ids for which the user is a company owner
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

        if (filter.emailAddress != null) {
            setupEmailAddressFilter(filter.emailAddress, keycloakUserService)
        }

        val companyIdsMatchingSearchString = companyIdsMatchingSearchString(filter.companySearchString)

        val extendedStoredDataRequests =
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
                    companyIds = companyIdsMatchingSearchString,
                ).map { entity ->
                    val dto = entity.toExtendedStoredDataRequest()
                    dto.copy(
                        companyName = companyDataController.getCompanyById(entity.companyId.toString()).companyInformation.companyName,
                        userEmailAddress = keycloakUserService.getUser(entity.userId.toString()).email,
                    )
                }

        return extendedStoredDataRequests
    }

    private fun companyIdsMatchingSearchString(companySearchString: String?): List<String>? =
        if (companySearchString != null) {
            companyDataController
                .getCompanies(
                    searchString = companySearchString,
                    chunkIndex = 0,
                    chunkSize = Int.MAX_VALUE,
                ).map { it.companyId }
        } else {
            null
        }

    /**
     * This function should be called when the email address filter is not empty, i.e. if shouldFilterByEmailAddress
     * is true. The keycloakUserControllerApiService is required to get the user ids for the email addresses.
     */

    private fun setupEmailAddressFilter(
        emailAddress: String,
        keycloakUserControllerApiService: KeycloakUserService
    ): List<KeycloakUserInfo> =
            emailAddress
                .let { keycloakUserControllerApiService.searchUsers(it) }

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
                val dto = entity.toExtendedStoredDataRequest()
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
        requestRepository.getNumberOfRequests(requestSearchFilter)
}
