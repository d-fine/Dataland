package org.dataland.datasourcingservice.services

import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.StoredRequest
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.springframework.beans.factory.annotation.Autowired
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
    @Transactional
    fun searchRequests(
        companyId: String?,
        dataType: String?,
        reportingPeriod: String?,
        state: RequestState?,
    ): List<StoredRequest> =
        requestRepository
            .searchRequests(
                companyId?.let { UUID.fromString(it) },
                dataType,
                reportingPeriod,
                state,
            ).map { it.toStoredDataRequest() }
}
