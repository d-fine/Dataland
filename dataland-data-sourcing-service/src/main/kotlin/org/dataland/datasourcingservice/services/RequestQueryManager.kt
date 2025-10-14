package org.dataland.datasourcingservice.services

import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.StoredRequest
import org.dataland.datasourcingservice.repositories.RequestRepository
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
class RequestQueryManager(
    @Autowired private val requestRepository: RequestRepository,
) {
    /**
     * Search for requests based on optional filters.
     * @param companyId to filter by
     * @param dataType to filter by
     * @param reportingPeriod to filter by
     * @param state to filter by
     * @return list of matching StoredRequest objects
     */
    @Transactional(readOnly = true)
    fun searchRequests(
        companyId: UUID?,
        dataType: String?,
        reportingPeriod: String?,
        state: RequestState?,
        chunkSize: Int = 100,
        chunkIndex: Int = 0,
    ): List<StoredRequest> =
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
            ).map { it.toStoredDataRequest() }

    /**
     * Get the number of requests that match the optional filters.
     * @param companyId to filter by
     * @param dataType to filter by
     * @param reportingPeriod to filter by
     * @param state to filter by
     * @return the number of matching requests
     */
    @Transactional(readOnly = true)
    fun getNumberOfRequests(
        companyId: UUID?,
        dataType: String?,
        reportingPeriod: String?,
        state: RequestState?,
    ): Int = requestRepository.getNumberOfRequests(companyId, dataType, reportingPeriod, state)
}
