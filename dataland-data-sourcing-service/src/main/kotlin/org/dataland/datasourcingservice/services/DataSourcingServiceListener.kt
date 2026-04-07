package org.dataland.datasourcingservice.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.NonSourceabilityAutoAcceptedEventPayload
import org.dataland.datalandmessagequeueutils.messages.NonSourceabilityCreatedEventPayload
import org.dataland.datalandmessagequeueutils.messages.QaNonSourceabilityAcceptedEventPayload
import org.dataland.datalandmessagequeueutils.messages.QaNonSourceabilityRejectedEventPayload
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingPatch
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Listener class for all RabbitMQ messages that are relevant to the data sourcing service.
 */
@Service("DataSourcingServiceListener")
class DataSourcingServiceListener
    @Autowired
    constructor(
        private val metaDataControllerApi: MetaDataControllerApi,
        private val dataSourcingManager: DataSourcingManager,
        private val dataSourcingQueryManager: DataSourcingQueryManager,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)
        private val nonSourceabilityToDataSourcingId = ConcurrentHashMap<UUID, UUID>()

        private fun getDataSourcingForDimensions(
            companyId: UUID,
            dataType: String,
            reportingPeriod: String,
        ) = dataSourcingQueryManager
            .searchDataSourcings(
                companyId = companyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                state = null,
                chunkSize = 1,
                chunkIndex = 0,
            ).firstOrNull()

        /**
         * Listener method that processes messages from the QA service indicating a QA status change of a dataset.
         * @param payload the message payload as a string
         * @param type the message type from the header
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            QueueNames.DATA_SOURCING_SERVICE_DATASET_QA_STATUS_UPDATE,
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS, declare = "false"),
                    key = [RoutingKeyNames.DATA],
                ),
            ],
        )
        fun processHandleDatasetUploadEvent(
            @Payload payload: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            MessageQueueUtils.validateMessageType(type, MessageType.QA_STATUS_UPDATED)
            val dataUploadedPayload = MessageQueueUtils.readMessagePayload<QaStatusChangeMessage>(payload)
            val dataId = dataUploadedPayload.dataId
            val updatedQaStatus = dataUploadedPayload.updatedQaStatus
            MessageQueueUtils.rejectMessageOnException {
                val dataMetaInformation = metaDataControllerApi.getDataMetaInfo(dataId)
                val companyId = UUID.fromString(dataMetaInformation.companyId)
                val reportingPeriod = dataMetaInformation.reportingPeriod
                val dataType = dataMetaInformation.dataType

                val storedDataSourcing =
                    dataSourcingQueryManager
                        .searchDataSourcings(
                            companyId = companyId,
                            dataType = dataType.toString(),
                            reportingPeriod = reportingPeriod,
                            state = null,
                            chunkSize = 1,
                            chunkIndex = 0,
                        ).firstOrNull()

                if (storedDataSourcing == null) {
                    logger.info(
                        "Received QA status update message for dataset with ID $dataId. However, no data sourcing " +
                            "object exists for the associated company ID $companyId, reporting period $reportingPeriod and " +
                            "data type $dataType.",
                    )
                    return@rejectMessageOnException
                }

                when (updatedQaStatus) {
                    QaStatus.Accepted -> {
                        dataSourcingManager.patchDataSourcingEntityById(
                            UUID.fromString(storedDataSourcing.dataSourcingId),
                            DataSourcingPatch(state = DataSourcingState.Done),
                        )
                    }

                    QaStatus.Pending -> {
                        if (storedDataSourcing.state != DataSourcingState.Done) {
                            dataSourcingManager.patchDataSourcingEntityById(
                                UUID.fromString(storedDataSourcing.dataSourcingId),
                                DataSourcingPatch(state = DataSourcingState.DataVerification),
                            )
                        }
                    }

                    else -> Unit
                }
            }
        }

        /**
         * Listener method that processes backend non-sourceability lifecycle events and updates data-sourcing states.
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            QueueNames.NON_SOURCEABILITY_CREATED_QUEUE,
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.BACKEND_DATA_NONSOURCEABLE, declare = "false"),
                    key = [RoutingKeyNames.NON_SOURCEABILITY_CREATED, RoutingKeyNames.NON_SOURCEABILITY_AUTO_ACCEPTED],
                ),
            ],
        )
        fun processBackendNonSourceabilityEvents(
            @Payload payload: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            MessageQueueUtils.rejectMessageOnException {
                when (type) {
                    MessageType.NON_SOURCEABILITY_CREATED -> {
                        val eventPayload = MessageQueueUtils.readMessagePayload<NonSourceabilityCreatedEventPayload>(payload)
                        handleNonSourceabilityCreated(eventPayload, correlationId)
                    }

                    MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED -> {
                        val eventPayload = MessageQueueUtils.readMessagePayload<NonSourceabilityAutoAcceptedEventPayload>(payload)
                        handleNonSourceabilityAutoAccepted(eventPayload, correlationId)
                    }

                    else -> {
                        throw MessageQueueRejectException("Unsupported non-sourceability message type $type")
                    }
                }
            }
        }

        private fun handleNonSourceabilityCreated(
            eventPayload: NonSourceabilityCreatedEventPayload,
            correlationId: String,
        ) {
            val storedDataSourcing =
                getDataSourcingForDimensions(
                    companyId = eventPayload.companyId,
                    dataType = eventPayload.dataType,
                    reportingPeriod = eventPayload.reportingPeriod,
                )
                    ?: throw MessageQueueRejectException(
                        "No matching data sourcing found for nonSourceabilityId " +
                            "${eventPayload.nonSourceabilityId} (correlationId: $correlationId)",
                    )

            dataSourcingManager.patchDataSourcingEntityById(
                UUID.fromString(storedDataSourcing.dataSourcingId),
                DataSourcingPatch(state = DataSourcingState.NonSourceableVerification),
            )
            nonSourceabilityToDataSourcingId[eventPayload.nonSourceabilityId] =
                UUID.fromString(storedDataSourcing.dataSourcingId)

            logger.info(
                "Applied ${MessageType.NON_SOURCEABILITY_CREATED} for nonSourceabilityId " +
                    "${eventPayload.nonSourceabilityId}, transitioned dataSourcingId " +
                    "${storedDataSourcing.dataSourcingId} to ${DataSourcingState.NonSourceableVerification} " +
                    "(correlationId: $correlationId)",
            )
        }

        private fun handleNonSourceabilityAutoAccepted(
            eventPayload: NonSourceabilityAutoAcceptedEventPayload,
            correlationId: String,
        ) {
            val storedDataSourcing =
                getDataSourcingForDimensions(
                    companyId = eventPayload.companyId,
                    dataType = eventPayload.dataType,
                    reportingPeriod = eventPayload.reportingPeriod,
                )
                    ?: throw MessageQueueRejectException(
                        "No matching data sourcing found for nonSourceabilityId " +
                            "${eventPayload.nonSourceabilityId} (correlationId: $correlationId)",
                    )

            dataSourcingManager.patchDataSourcingEntityById(
                UUID.fromString(storedDataSourcing.dataSourcingId),
                DataSourcingPatch(state = DataSourcingState.NonSourceable),
            )
            nonSourceabilityToDataSourcingId[eventPayload.nonSourceabilityId] =
                UUID.fromString(storedDataSourcing.dataSourcingId)

            logger.info(
                "Applied ${MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED} for nonSourceabilityId " +
                    "${eventPayload.nonSourceabilityId}, transitioned dataSourcingId " +
                    "${storedDataSourcing.dataSourcingId} to ${DataSourcingState.NonSourceable} " +
                    "(correlationId: $correlationId)",
            )
        }

        /**
         * Listener method that processes QA decision events for non-sourceability claims.
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            QueueNames.QA_DECISION_QUEUE,
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS, declare = "false"),
                    key = [RoutingKeyNames.QA_DECISION_ACCEPTED, RoutingKeyNames.QA_DECISION_REJECTED],
                ),
            ],
        )
        fun processQaNonSourceabilityDecisionEvents(
            @Payload payload: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            MessageQueueUtils.rejectMessageOnException {
                when (type) {
                    MessageType.QA_NON_SOURCEABILITY_ACCEPTED -> {
                        val eventPayload = MessageQueueUtils.readMessagePayload<QaNonSourceabilityAcceptedEventPayload>(payload)
                        handleQaNonSourceabilityAccepted(eventPayload, correlationId)
                    }

                    MessageType.QA_NON_SOURCEABILITY_REJECTED -> {
                        val eventPayload = MessageQueueUtils.readMessagePayload<QaNonSourceabilityRejectedEventPayload>(payload)
                        handleQaNonSourceabilityRejected(eventPayload, correlationId)
                    }

                    else -> {
                        throw MessageQueueRejectException("Unsupported QA non-sourceability decision message type $type")
                    }
                }
            }
        }

        private fun handleQaNonSourceabilityAccepted(
            eventPayload: QaNonSourceabilityAcceptedEventPayload,
            correlationId: String,
        ) {
            val dataSourcingId =
                nonSourceabilityToDataSourcingId[eventPayload.nonSourceabilityId]
                    ?: throw MessageQueueRejectException(
                        "No matching data sourcing found for nonSourceabilityId " +
                            "${eventPayload.nonSourceabilityId} (correlationId: $correlationId)",
                    )

            dataSourcingManager.patchDataSourcingEntityById(
                dataSourcingId,
                DataSourcingPatch(state = DataSourcingState.NonSourceable),
            )

            logger.info(
                "Applied ${MessageType.QA_NON_SOURCEABILITY_ACCEPTED} for nonSourceabilityId " +
                    "${eventPayload.nonSourceabilityId}, transitioned dataSourcingId " +
                    "$dataSourcingId to ${DataSourcingState.NonSourceable} (correlationId: $correlationId)",
            )
        }

        private fun handleQaNonSourceabilityRejected(
            eventPayload: QaNonSourceabilityRejectedEventPayload,
            correlationId: String,
        ) {
            val dataSourcingId =
                nonSourceabilityToDataSourcingId[eventPayload.nonSourceabilityId]
                    ?: throw MessageQueueRejectException(
                        "No matching data sourcing found for nonSourceabilityId " +
                            "${eventPayload.nonSourceabilityId} (correlationId: $correlationId)",
                    )

            logger.info(
                "Applied ${MessageType.QA_NON_SOURCEABILITY_REJECTED} for nonSourceabilityId " +
                    "${eventPayload.nonSourceabilityId}, kept dataSourcingId $dataSourcingId in " +
                    "${DataSourcingState.NonSourceableVerification} (correlationId: $correlationId)",
            )
        }
    }
