package org.dataland.datasourcingservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
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
         * Search for requests based on optional filters.
         * @param companyId to filter by
         * @param dataType to filter by
         * @param reportingPeriod to filter by
         * @param state to filter by
         * @return list of matching StoredRequest objects
         */
        @Transactional
        fun searchRequests(
            companyId: UUID?,
            dataType: String?,
            reportingPeriod: String?,
            state: RequestState?,
            chunkSize: Int = 100,
            chunkIndex: Int = 0,
        ): List<ExtendedStoredRequest> =
            requestRepository
                .findByListOfIdsAndFetchDataSourcingEntity(
                    requestRepository
                        .searchRequests(
                            companyId,
                            dataType,
                            reportingPeriod,
                            state,
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
                        ).content,
                ).map { it.toExtendedStoredDataRequest() }

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
