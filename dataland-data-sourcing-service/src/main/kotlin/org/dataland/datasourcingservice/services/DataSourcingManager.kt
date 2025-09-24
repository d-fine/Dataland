package org.dataland.datasourcingservice.services

import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.exceptions.DataSourcingNotFoundApiException
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingPatch
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

/**
 * Service class that manages all operations related to data sourcing entities.
 */
@Service("DataSourcingManager")
class DataSourcingManager
    @Autowired
    constructor(
        private val dataSourcingRepository: DataSourcingRepository,
    ) {
        /**
         * Return the unique StoredDataSourcing object for the given dataSourcingEntityId.
         * @param dataSourcingEntityId the ID of the data sourcing entity to retrieve
         * @return the associated StoredDataSourcing object
         */
        @Transactional(readOnly = true)
        fun getStoredDataSourcing(dataSourcingEntityId: UUID): StoredDataSourcing {
            val entityWithoutFetchedRequests =
                dataSourcingRepository.findById(dataSourcingEntityId).getOrNull() ?: throw
                    DataSourcingNotFoundApiException(
                        dataSourcingEntityId,
                    )
            return dataSourcingRepository.fetchAssociatedRequests(entityWithoutFetchedRequests).toStoredDataSourcing()
        }

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
        ): StoredDataSourcing {
            val entityWithoutFetchedRequests =
                dataSourcingRepository
                    .findByCompanyIdAndDataTypeAndReportingPeriod(companyId, dataType, reportingPeriod)
                    ?: throw DataSourcingNotFoundApiException(companyId, reportingPeriod, dataType)
            return entityWithoutFetchedRequests.toStoredDataSourcing()
        }

        private fun getDataSourcingEntityById(dataSourcingEntityId: UUID): DataSourcingEntity {
            val entityWithoutFetchedRequests =
                dataSourcingRepository.findById(dataSourcingEntityId).getOrNull() ?: throw DataSourcingNotFoundApiException(
                    dataSourcingEntityId,
                )
            return dataSourcingRepository.fetchAssociatedRequests(entityWithoutFetchedRequests)
        }

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
         * @param dataSourcingEntityId the id of the data sourcing entity to patch
         * @param dataSourcingPatch the patch object containing the new values
         * @return the StoredDataSourcing object corresponding to the patched entity
         */
        @Transactional
        fun patchDataSourcingEntityById(
            dataSourcingEntityId: UUID,
            dataSourcingPatch: DataSourcingPatch,
        ): StoredDataSourcing {
            val dataSourcingEntity = getDataSourcingEntityById(dataSourcingEntityId)
            return handlePatchOfDataSourcingEntity(dataSourcingEntity, dataSourcingPatch)
        }

        /**
         * Patches the data sourcing entity according to the given patch object.
         * @param dataSourcingEntity the data sourcing entity to patch
         * @param dataSourcingPatch the patch object containing the new values
         * @return the StoredDataSourcing object corresponding to the patched entity
         */
        private fun handlePatchOfDataSourcingEntity(
            dataSourcingEntity: DataSourcingEntity,
            dataSourcingPatch: DataSourcingPatch,
        ): StoredDataSourcing {
            performStatePatch(dataSourcingEntity, dataSourcingPatch.state)
            updateIfNotNull(dataSourcingPatch.documentIds) { dataSourcingEntity.documentIds = it }
            updateIfNotNull(dataSourcingPatch.expectedPublicationDatesOfDocuments) {
                dataSourcingEntity.expectedPublicationDatesDocuments = it
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

        private fun performStatePatch(
            dataSourcingEntity: DataSourcingEntity,
            state: DataSourcingState?,
        ) {
            if (state == null) return
            dataSourcingEntity.state = state
            if (state in setOf(DataSourcingState.Answered, DataSourcingState.NonSourceable)) {
                dataSourcingRepository.fetchAssociatedRequests(dataSourcingEntity).associatedRequests.forEach {
                    it.state = RequestState.Processed
                }
            }
        }

        /**
         * Patches the state of the data sourcing entity according to the given state.
         * @param dataSourcingEntityId the id of the data sourcing entity to patch
         * @param state the state to patch to
         * @return the StoredDataSourcing object corresponding to the patched entity
         */
        @Transactional
        fun patchDataSourcingState(
            dataSourcingEntityId: UUID,
            state: DataSourcingState,
        ): StoredDataSourcing {
            val dataSourcingEntity =
                getDataSourcingEntityById(dataSourcingEntityId)
            return handlePatchOfDataSourcingEntity(dataSourcingEntity, DataSourcingPatch(state = state))
        }

        /**
         * Patches the documents of the data sourcing entity according to the given documents.
         * @param dataSourcingEntityId the id of the data sourcing entity to patch
         * @param documentIds to patch to
         * @param appendDocuments flag to decide if documents should overwrite current documents or merely append
         * @return the StoredDataSourcing object corresponding to the patched entity
         */
        @Transactional
        fun patchDataSourcingDocument(
            dataSourcingEntityId: UUID,
            documentIds: Set<String>,
            appendDocuments: Boolean,
        ): StoredDataSourcing {
            val dataSourcingEntity =
                getDataSourcingEntityById(dataSourcingEntityId)
            val newDocumentsIds = if (!appendDocuments) documentIds else dataSourcingEntity.documentIds + documentIds
            return handlePatchOfDataSourcingEntity(dataSourcingEntity, DataSourcingPatch(documentIds = newDocumentsIds))
        }

        /**
         * Patches the document sourcing attempt date of the data sourcing entity according to the given date.
         * @param dataSourcingEntityId the id of the data sourcing entity to patch
         * @param date the date to patch to
         * @return the StoredDataSourcing object corresponding to the patched entity
         */
        @Transactional
        fun patchDateDocumentSourcingAttempt(
            dataSourcingEntityId: UUID,
            date: LocalDate,
        ): StoredDataSourcing {
            val dataSourcingEntity =
                getDataSourcingEntityById(dataSourcingEntityId)
            return handlePatchOfDataSourcingEntity(dataSourcingEntity, DataSourcingPatch(dateDocumentSourcingAttempt = date))
        }
    }
