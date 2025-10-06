package org.dataland.datasourcingservice.services

import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service class for handling data sourcing queries.
 */
@Service("DataSourcingQueryManager")
class DataSourcingQueryManager(
    @Autowired private val dataSourcingRepository: DataSourcingRepository,
) {
    /**
     * Search data sourcings based on the provided filters and return a paginated chunk.
     * @param companyId to filter by
     * @param dataType to filter by
     * @param reportingPeriod to filter by
     * @param state to filter by
     * @param chunkSize the size of the chunk to return
     * @param chunkIndex the index of the chunk to return
     * @return list of matching StoredDataSourcing objects
     */
    @Transactional(readOnly = true)
    fun searchDataSourcings(
        companyId: UUID?,
        dataType: String?,
        reportingPeriod: String?,
        state: DataSourcingState?,
        chunkSize: Int = 100,
        chunkIndex: Int = 0,
    ): List<StoredDataSourcing> =
        dataSourcingRepository
            .searchDataSourcingEntitiesAndFetchAllStoredFields(
                companyId, dataType, reportingPeriod, state,
                PageRequest.of(chunkIndex, chunkSize),
            ).content
            .map { it.toStoredDataSourcing() }
}
