package org.dataland.datasourcingservice.services

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.SourceabilityMessage
import org.dataland.datasourcingservice.entities.DataSourcingEntity
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
import org.dataland.datasourcingservice.utils.isUserAdmin
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID
import java.util.UUID.randomUUID

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
        private val existingRequestsManager: ExistingRequestsManager,
        private val cloudEventMessageHandler: CloudEventMessageHandler,
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
                .toStoredDataSourcing(isUserAdmin())
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
            val correlationId = randomUUID().toString()
            val dataSourcingEntity = getFullyFetchedDataSourcingEntityById(dataSourcingEntityId)
            logger.info("Patch data sourcing entity with id: $dataSourcingEntityId. CorrelationId: $correlationId")
            return handlePatchOfDataSourcingEntity(dataSourcingEntity, dataSourcingPatch, correlationId).toStoredDataSourcing()
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
            correlationId: String,
        ): DataSourcingEntity {
            performStatePatch(fullyFetchedDataSourcingEntity, dataSourcingPatch.state, correlationId)
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
         * field must already have been fetched. If state is changed to NonSourceable also send Message to RabbitMQ.
         */
        private fun performStatePatch(
            dataSourcingEntityWithFetchedRequests: DataSourcingEntity,
            state: DataSourcingState?,
            correlationId: String,
        ) {
            if (state == null) return

            if (state in setOf(DataSourcingState.Done, DataSourcingState.NonSourceable)) {
                dataSourcingEntityWithFetchedRequests.associatedRequests
                    .filter { it.state != RequestState.Withdrawn }
                    .forEach {
                        existingRequestsManager.patchRequestState(
                            it.id,
                            RequestState.Processed,
                            null,
                        )
                    }
            }
            if (state == DataSourcingState.NonSourceable &&
                dataSourcingEntityWithFetchedRequests.state != DataSourcingState.NonSourceable
            ) {
                sendNonSourceableMessage(dataSourcingEntityWithFetchedRequests, correlationId)
            }
            dataSourcingEntityWithFetchedRequests.state = state
        }

        /**
         * Builds, logs and sends non-sourceability message to RabbitMQ
         */
        private fun sendNonSourceableMessage(
            dataSourcingEntityWithFetchedRequests: DataSourcingEntity,
            correlationId: String,
        ) {
            val messageBody =
                SourceabilityMessage(
                    BasicDataDimensions(
                        dataSourcingEntityWithFetchedRequests.companyId.toString(),
                        dataSourcingEntityWithFetchedRequests.dataType,
                        dataSourcingEntityWithFetchedRequests.reportingPeriod,
                    ),
                    true,
                    "",
                )
            logger.info(
                "Sending non-sourceable message to message queue for data sourcing entity with id: " +
                    "${dataSourcingEntityWithFetchedRequests.dataSourcingId}. CorrelationId: $correlationId.",
            )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                JsonUtils.defaultObjectMapper.writeValueAsString(messageBody),
                MessageType.DATASOURCING_NONSOURCEABLE,
                correlationId,
                ExchangeName.DATASOURCING_DATA_NONSOURCEABLE,
                RoutingKeyNames.DATASOURCING_NONSOURCEABLE,
            )
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
            val correlationId = randomUUID().toString()
            logger
                .info(
                    "Patch state of data sourcing entity with id: $dataSourcingEntityId " +
                        "from state ${dataSourcingEntity.state} to state $state. CorrelationId: $correlationId.",
                )
            return handlePatchOfDataSourcingEntity(
                dataSourcingEntity,
                DataSourcingPatch(state = state),
                correlationId,
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
            val correlationId = randomUUID().toString()
            documentIds.forEach {
                dataSourcingValidator.validateDocumentId(it)
            }
            val dataSourcingEntity = getFullyFetchedDataSourcingEntityById(dataSourcingEntityId)
            val newDocumentsIds = if (!appendDocuments) documentIds else dataSourcingEntity.documentIds + documentIds
            logger.info(
                "Patch documents with ids $documentIds of data sourcing entity with id: $dataSourcingEntityId with " +
                    "appendDocuments = $appendDocuments. CorrelationId: $correlationId.",
            )
            return handlePatchOfDataSourcingEntity(
                dataSourcingEntity,
                DataSourcingPatch(documentIds = newDocumentsIds),
                correlationId,
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
            val correlationId = randomUUID().toString()
            val dataSourcingEntity = getFullyFetchedDataSourcingEntityById(dataSourcingEntityId)
            logger.info(
                "Patch dateOfNextDocumentSourcingAttempt of data sourcing entity with id: $dataSourcingEntityId with" +
                    " dateOfNextDocumentSourcingAttempt: $date. CorrelationId: $correlationId.",
            )
            return handlePatchOfDataSourcingEntity(
                dataSourcingEntity,
                DataSourcingPatch(dateOfNextDocumentSourcingAttempt = date),
                correlationId,
            ).toReducedDataSourcing()
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
            documentCollector: UUID?,
            dataExtractor: UUID?,
            adminComment: String?,
        ): StoredDataSourcing {
            val correlationId = randomUUID().toString()
            val dataSourcingEntity = getFullyFetchedDataSourcingEntityById(dataSourcingEntityId)
            logger.info(
                "Patch documentCollector: $documentCollector, data extractor: $dataExtractor " +
                    "and admin comment: $adminComment to data sourcing entity with id ${dataSourcingEntity.dataSourcingId}." +
                    "CorrelationId: $correlationId.",
            )
            return handlePatchOfDataSourcingEntity(
                dataSourcingEntity,
                DataSourcingPatch(
                    documentCollector = documentCollector,
                    dataExtractor = dataExtractor,
                    adminComment = adminComment,
                ),
                correlationId,
            ).toStoredDataSourcing(isUserAdmin())
        }

        /**
         * Retrieves all StoredDataSourcing objects associated with the given company ID, either as a document
         * collector or data extractor.
         * @param companyId The UUID of the company whose data sourcing objects are to be retrieved.
         * @return A list of StoredDataSourcing objects associated with the specified company ID, or null if none exist.
         */
        @Transactional(readOnly = true)
        fun getStoredDataSourcingForCompanyId(companyId: UUID): List<StoredDataSourcing> {
            logger.info(
                "Find all assigned data sourcing objects for " +
                    "company with id: $companyId.",
            )
            val isUserAdmin = isUserAdmin()
            val dataSourcingEntities =
                dataSourcingRepository
                    .findAllByDocumentCollectorAndFetchNonRequestFields(companyId)
                    .plus(dataSourcingRepository.findAllByDataExtractor(companyId))
            return dataSourcingEntities.map { entity -> entity.toStoredDataSourcing(isUserAdmin) }
        }

        /**
         * Retrieves the history of revisions for a specific data sourcing object identified by its ID.
         * The returned DTOs do not include lazily fetched references to avoid runtime errors.
         * @param id The UUID string of the data sourcing object whose history is to be retrieved.
         * @param stateChangesOnly If true, only returns revisions where the state changed.
         * @return A list of StoredDataSourcing objects representing the revision history.
         * @throws InvalidInputApiException If the provided ID is not a valid UUID format.
         */
        @Transactional(readOnly = true)
        fun retrieveDataSourcingHistory(
            id: UUID,
            stateChangesOnly: Boolean = false,
        ): List<DataSourcingWithoutReferences> {
            logger.info("Retrieve data sourcing history for data sourcing entity with id: $id.")
            val revisions = dataRevisionRepository.listDataSourcingRevisionsById(id)
            if (revisions.isEmpty()) {
                throw DataSourcingNotFoundApiException(id)
            }
            val filteredRevisions = if (stateChangesOnly) keepOnlyStateChanges(revisions) else revisions
            return filteredRevisions
                .map { (entity, timestamp) -> entity.toDataSourcingWithoutReferences(isUserAdmin(), timestamp) }
        }

        private fun keepOnlyStateChanges(revisions: List<Pair<DataSourcingEntity, Long>>): List<Pair<DataSourcingEntity, Long>> {
            if (revisions.isEmpty()) return emptyList()
            return buildList {
                add(revisions.first())
                revisions.zipWithNext { prev, current ->
                    if (current.first.state != prev.first.state) add(current)
                }
            }
        }
    }
