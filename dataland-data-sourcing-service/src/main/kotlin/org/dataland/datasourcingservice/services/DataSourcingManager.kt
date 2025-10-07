package org.dataland.datasourcingservice.services

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.exceptions.DataSourcingNotFoundApiException
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingPatch
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences
import org.dataland.datasourcingservice.model.datasourcing.ReducedDataSourcing
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.datasourcingservice.utils.DataSourcingUtils.updateIfNotNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

/**
 * Service class that manages all operations related to data sourcing entities.
 */
@Service("DataSourcingManager")
class DataSourcingManager
    @Autowired
    constructor(
        private val dataSourcingRepository: DataSourcingRepository,
        private val dataRevisionRepository: DataRevisionRepository,
        private val dataSourcingValidator: DataSourcingValidator,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        private fun getFullyFetchedDataSourcingEntityById(dataSourcingEntityId: UUID): DataSourcingEntity =
            dataSourcingRepository.findByIdAndFetchAllStoredFields(dataSourcingEntityId)
                ?: throw DataSourcingNotFoundApiException(dataSourcingEntityId)

        /**
         * Return the unique StoredDataSourcing object for the given dataSourcingEntityId.
         * @param dataSourcingEntityId the ID of the data sourcing entity to retrieve
         * @return the associated StoredDataSourcing object
         */
        @Transactional(readOnly = true)
        fun getStoredDataSourcing(dataSourcingEntityId: UUID): StoredDataSourcing =
            getFullyFetchedDataSourcingEntityById(dataSourcingEntityId)
                .toStoredDataSourcing()
                .also { logger.info("Get data sourcing entity with id: $dataSourcingEntityId") }

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
            val dataSourcingEntity = getFullyFetchedDataSourcingEntityById(dataSourcingEntityId)
            logger.info("Patch data sourcing entity with id: $dataSourcingEntityId.")
            return handlePatchOfDataSourcingEntity(dataSourcingEntity, dataSourcingPatch).toStoredDataSourcing()
        }

        /**
         * Patches the data sourcing entity according to the given patch object. To prevent
         * runtime errors, all lazily fetched fields in the entity should already be fetched
         * at the beginning.
         * @param fullyFetchedDataSourcingEntity the data sourcing entity to patch
         * @param dataSourcingPatch the patch object containing the new values
         * @return the StoredDataSourcing object corresponding to the patched entity
         */
        private fun handlePatchOfDataSourcingEntity(
            fullyFetchedDataSourcingEntity: DataSourcingEntity,
            dataSourcingPatch: DataSourcingPatch,
        ): DataSourcingEntity {
            performStatePatch(fullyFetchedDataSourcingEntity, dataSourcingPatch.state)
            updateIfNotNull(dataSourcingPatch.documentIds) { fullyFetchedDataSourcingEntity.documentIds = it }
            updateIfNotNull(dataSourcingPatch.expectedPublicationDatesOfDocuments) {
                fullyFetchedDataSourcingEntity.expectedPublicationDatesOfDocuments = it
            }
            updateIfNotNull(dataSourcingPatch.dateOfNextDocumentSourcingAttempt) {
                fullyFetchedDataSourcingEntity.dateOfNextDocumentSourcingAttempt = it
            }
            updateIfNotNull(dataSourcingPatch.documentCollector) {
                fullyFetchedDataSourcingEntity.documentCollector = it
            }
            updateIfNotNull(dataSourcingPatch.dataExtractor) { fullyFetchedDataSourcingEntity.dataExtractor = it }
            updateIfNotNull(dataSourcingPatch.adminComment) { fullyFetchedDataSourcingEntity.adminComment = it }
            updateIfNotNull(dataSourcingPatch.associatedRequests) { associatedRequest ->
                fullyFetchedDataSourcingEntity.associatedRequests =
                    associatedRequest
                        .map { request ->
                            val requestEntity = request.toRequestEntity()
                            requestEntity.dataSourcingEntity = fullyFetchedDataSourcingEntity
                            requestEntity
                        }.toMutableSet()
            }

            return dataSourcingRepository.save(fullyFetchedDataSourcingEntity)
        }

        /**
         * Performs the state patch on the given data sourcing entity, of which the associated Requests
         * field must already have been fetched.
         */
        private fun performStatePatch(
            dataSourcingEntityWithFetchedRequests: DataSourcingEntity,
            state: DataSourcingState?,
        ) {
            if (state == null) return
            dataSourcingEntityWithFetchedRequests.state = state
            if (state in setOf(DataSourcingState.Done, DataSourcingState.NonSourceable)) {
                dataSourcingEntityWithFetchedRequests.associatedRequests.forEach {
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
        ): ReducedDataSourcing {
            val dataSourcingEntity = getFullyFetchedDataSourcingEntityById(dataSourcingEntityId)
            logger
                .info(
                    "Patch state of data sourcing entity with id: $dataSourcingEntityId " +
                        "from state ${dataSourcingEntity.state} to state $state.",
                )
            return handlePatchOfDataSourcingEntity(
                dataSourcingEntity,
                DataSourcingPatch(state = state),
            ).toReducedDataSourcing()
        }

        /**
         * Patches the documents of the data sourcing entity according to the given documents.
         * @param dataSourcingEntityId the id of the data sourcing entity to patch
         * @param documentIds the document IDs to patch to the data sourcing entity
         * @param appendDocuments flag to decide if the provided list of documentIds should overwrite
         * the current list of documentIds or if the documentIds should be appended to the existing list
         * @return the StoredDataSourcing object corresponding to the patched entity
         */
        @Transactional
        fun patchDataSourcingDocument(
            dataSourcingEntityId: UUID,
            documentIds: Set<String>,
            appendDocuments: Boolean,
        ): ReducedDataSourcing {
            documentIds.forEach {
                dataSourcingValidator.validateDocumentId(it)
            }
            val dataSourcingEntity = getFullyFetchedDataSourcingEntityById(dataSourcingEntityId)
            val newDocumentsIds = if (!appendDocuments) documentIds else dataSourcingEntity.documentIds + documentIds
            logger.info(
                "Patch documents with ids $documentIds of data sourcing entity with id: $dataSourcingEntityId with " +
                    "appendDocuments = $appendDocuments.",
            )
            return handlePatchOfDataSourcingEntity(
                dataSourcingEntity,
                DataSourcingPatch(documentIds = newDocumentsIds),
            ).toReducedDataSourcing()
        }

        /**
         * Patches the document sourcing attempt date of the data sourcing entity according to the given date.
         * @param dataSourcingEntityId the id of the data sourcing entity to patch
         * @param date the date to patch to
         * @return the StoredDataSourcing object corresponding to the patched entity
         */
        @Transactional
        fun patchDateOfNextDocumentSourcingAttempt(
            dataSourcingEntityId: UUID,
            date: LocalDate,
        ): ReducedDataSourcing {
            val dataSourcingEntity = getFullyFetchedDataSourcingEntityById(dataSourcingEntityId)
            logger.info(
                "Patch dateOfNextDocumentSourcingAttempt of data sourcing entity with id: $dataSourcingEntityId with" +
                    " dateOfNextDocumentSourcingAttempt: $date.",
            )
            return handlePatchOfDataSourcingEntity(
                dataSourcingEntity,
                DataSourcingPatch(dateOfNextDocumentSourcingAttempt = date),
            ).toReducedDataSourcing()
        }

        /**
         * Resets an existing DataSourcingEntity to the Initialized state or creates a new one if none exists.
         *
         * Associates the given RequestEntity with the DataSourcingEntity and stores it in the database. This will also
         * cascade the save operation to the associated RequestEntity automatically.
         *
         * @param requestEntity the RequestEntity to associate with the DataSourcingEntity
         * @return the reset or newly created DataSourcingEntity
         */
        fun resetOrCreateDataSourcingObjectAndAddRequest(requestEntity: RequestEntity): DataSourcingEntity {
            val dataSourcingEntity =
                dataSourcingRepository.findByDataDimensionAndFetchAllStoredFields(
                    requestEntity.companyId,
                    requestEntity.dataType,
                    requestEntity.reportingPeriod,
                ) ?: DataSourcingEntity(
                    companyId = requestEntity.companyId,
                    reportingPeriod = requestEntity.reportingPeriod,
                    dataType = requestEntity.dataType,
                )
            logger.info(
                "Add request with id ${requestEntity.id} to data sourcing entity with id ${dataSourcingEntity.dataSourcingId}.",
            )
            dataSourcingEntity.state = DataSourcingState.Initialized
            dataSourcingEntity.addAssociatedRequest(requestEntity)
            return dataSourcingRepository.save(dataSourcingEntity)
        }

        /**
         * Patches the document collector and/or data extractor of the data sourcing entity with the given ID.
         * Throws a DataSourcingNotFoundApiException if no such data sourcing entity exists.
         * @param dataSourcingEntityId the id of the data sourcing entity to patch
         * @param documentCollector the ID of the new document collector, or null if no update should be performed
         * @param dataExtractor the ID of the new data extractor, or null if no update should be performed
         * @param adminComment an optional admin comment to add to the data sourcing entity
         * @return the StoredDataSourcing object corresponding to the patched entity
         */
        @Transactional
        fun patchProviderAndAdminComment(
            dataSourcingEntityId: UUID,
            documentCollector: String?,
            dataExtractor: String?,
            adminComment: String?,
        ): StoredDataSourcing {
            val dataSourcingEntity = getFullyFetchedDataSourcingEntityById(dataSourcingEntityId)
            logger.info(
                "Patch documentCollector: $documentCollector, data extractor: $dataExtractor " +
                    "and admin comment: $adminComment to data sourcing entity with id ${dataSourcingEntity.dataSourcingId}.",
            )
            return handlePatchOfDataSourcingEntity(
                dataSourcingEntity,
                DataSourcingPatch(
                    documentCollector = documentCollector?.let { UUID.fromString(it) },
                    dataExtractor = dataExtractor?.let { UUID.fromString(it) },
                    adminComment = adminComment,
                ),
            ).toStoredDataSourcing()
        }

        /**
         * Retrieves all StoredDataSourcing objects associated with the given company ID, either as a document
         * collector or data extractor.
         * @param companyId The UUID of the company whose data sourcing objects are to be retrieved.
         * @return A list of StoredDataSourcing objects associated with the specified company ID, or null if none exist.
         */
        @Transactional(readOnly = true)
        fun getStoredDataSourcingForCompanyId(companyId: UUID): List<ReducedDataSourcing> {
            logger.info(
                "Find all assigned data sourcing objects for " +
                    "company with id: $companyId.",
            )
            val dataSourcingEntities =
                dataSourcingRepository
                    .findAllByDocumentCollectorAndFetchNonRequestFields(companyId)
                    .plus(dataSourcingRepository.findAllByDataExtractor(companyId))
            return dataSourcingEntities.map { entity -> entity.toReducedDataSourcing() }
        }

        /**
         * Retrieves the history of revisions for a specific data sourcing object identified by its ID.
         * The returned DTOs do not include lazily fetched references to avoid runtime errors.
         * @param id The UUID string of the data sourcing object whose history is to be retrieved.
         * @return A list of StoredDataSourcing objects representing the revision history.
         * @throws InvalidInputApiException If the provided ID is not a valid UUID format.
         */
        @Transactional(readOnly = true)
        fun retrieveDataSourcingHistory(id: String): List<DataSourcingWithoutReferences> {
            val uuid =
                try {
                    UUID.fromString(id)
                } catch (_: IllegalArgumentException) {
                    throw InvalidInputApiException(
                        "Invalid UUID format for id: $id",
                        message = "Invalid UUID format for id: $id, please provide a valid UUID string.",
                    )
                }
            logger.info("Retrieve data sourcing history for data sourcing entity with id: $id.")
            return dataRevisionRepository
                .listDataSourcingRevisionsById(uuid)
                .map { it.toDataSourcingWithoutReferences() }
                .ifEmpty {
                    throw DataSourcingNotFoundApiException(uuid)
                }
        }
    }
