package org.dataland.datasourcingservice.services

import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.exceptions.DataSourcingNotFoundApiException
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingPatch
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service class that manages all operations related to data sourcing entities.
 */
@Service("DataSourcingManager")
class DataSourcingManager(
    @Autowired private val dataSourcingRepository: DataSourcingRepository,
) {
    /**
     * Returns the unique StoredDataSourcing object for the given company ID, reporting period and
     * data type. Throws a DataSourcingNotFoundApiException if no such object exists.
     * @param companyId of the stored data sourcing to retrieve
     * @param reportingPeriod of the stored data sourcing to retrieve
     * @param dataType of the stored data sourcing to retrieve
     * @return the associated StoredDataSourcing object
     */
    @Transactional(readOnly = true)
    fun getStoredDataSourcing(
        companyId: UUID,
        reportingPeriod: String,
        dataType: String,
    ): StoredDataSourcing =
        getDataSourcingEntityByDataDimension(companyId, reportingPeriod, dataType)?.toStoredDataSourcing()
            ?: throw DataSourcingNotFoundApiException(companyId, reportingPeriod, dataType)

    private fun getDataSourcingEntityByDataDimension(
        companyId: UUID,
        reportingPeriod: String,
        dataType: String,
    ): DataSourcingEntity? = dataSourcingRepository.findByCompanyIdAndDataTypeAndReportingPeriod(companyId, dataType, reportingPeriod)

    private fun getDataSourcingEntityById(dataSourcingEntityID: UUID): DataSourcingEntity? =
        dataSourcingRepository.findById(dataSourcingEntityID).orElse(null)

    /**
     * Calls the specified setter function on the specified new value as long as the new value is not null.
     * @param newValue the new value to set, or null if no update should be performed
     * @param setter the setter function to call if the new value is not null
     */
    fun <T> updateIfNotNull(
        newValue: T?,
        setter: (T) -> Unit,
    ) {
        newValue?.let { setter(it) }
    }

    /**
     * Patches the data sourcing entity with the given ID according to the given patch object.
     * Throws a DataSourcingNotFoundApiException if no such data sourcing entity exists.
     * @param dataSourcingEntityId the ID of the data sourcing entity to patch
     * @param dataSourcingPatch the patch object containing the new values
     * @return the StoredDataSourcing object corresponding to the patched entity
     */
    @Transactional
    fun patchDataSourcingEntity(
        dataSourcingEntityId: UUID,
        dataSourcingPatch: DataSourcingPatch,
    ): StoredDataSourcing {
        val dataSourcingEntity =
            getDataSourcingEntityById(dataSourcingEntityId) ?: throw DataSourcingNotFoundApiException(
                dataSourcingEntityId,
            )

        updateIfNotNull(dataSourcingPatch.state) { dataSourcingEntity.state = it }
        updateIfNotNull(dataSourcingPatch.documentIds) { dataSourcingEntity.documentIds = it }
        updateIfNotNull(dataSourcingPatch.expectedPublicationDatesOfDocuments) {
            dataSourcingEntity.expectedPublicationDatesOfDocuments = it
        }
        updateIfNotNull(dataSourcingPatch.dateDocumentSourcingAttempt) {
            dataSourcingEntity.dateDocumentSourcingAttempt = it
        }
        updateIfNotNull(dataSourcingPatch.documentCollector) { dataSourcingEntity.documentCollector = it }
        updateIfNotNull(dataSourcingPatch.dataExtractor) { dataSourcingEntity.dataExtractor = it }
        updateIfNotNull(dataSourcingPatch.adminComment) { dataSourcingEntity.adminComment = it }
        updateIfNotNull(dataSourcingPatch.associatedRequests) { associatedRequest ->
            dataSourcingEntity.associatedRequests =
                associatedRequest
                    .map {
                        it.toRequestEntity().copy(dataSourcingEntity = dataSourcingEntity)
                    }.toMutableSet()
        }

        return dataSourcingEntity.toStoredDataSourcing()
    }
}
