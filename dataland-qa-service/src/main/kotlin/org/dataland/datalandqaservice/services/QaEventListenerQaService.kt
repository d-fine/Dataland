package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.ManualQaRequestedMessage
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.messages.data.DataIdPayload
import org.dataland.datalandmessagequeueutils.messages.data.DataPointUploadedPayload
import org.dataland.datalandmessagequeueutils.messages.data.DataUploadedPayload
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.AssembledDataMigrationManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReviewManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

/**
 * The QA Service's MessageListener Service. Listens to messages (usually) sent be the internal storage after a dataset
 * has been stored or if a dataset has been deleted. Calls, depending on the data event (storing, deletion), the
 * corresponding methods of the QaReviewManager and QaReportManager
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 */
@Component
class QaEventListenerQaService
    @Suppress("LongParameterList")
    @Autowired
    constructor(
        private val cloudEventMessageHandler: CloudEventMessageHandler,
        private val objectMapper: ObjectMapper,
        private val qaReviewManager: QaReviewManager,
        private val dataPointQaReviewManager: DataPointQaReviewManager,
        private val qaReportManager: QaReportManager,
        private val metaDataControllerApi: MetaDataControllerApi,
        private val dataPointControllerApi: DataPointControllerApi,
        private val assembledDataMigrationManager: AssembledDataMigrationManager,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Method to retrieve message from dataStored exchange and constructing new one for qualityAssured exchange
         * @param payload the message body as a json string
         * @param correlationId the correlation ID of the current user process
         * @param type the type of the message
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            QueueNames.QA_SERVICE_DATASET_QA,
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.BACKEND_DATASET_EVENTS, declare = "false"),
                    key = [RoutingKeyNames.DATASET_UPLOAD, RoutingKeyNames.DATASET_QA],
                ),
            ],
        )
        fun addDatasetToQaReviewRepository(
            @Payload payload: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            MessageQueueUtils.validateMessageType(type, MessageType.PUBLIC_DATA_RECEIVED)

            MessageQueueUtils.rejectMessageOnException {
                val dataUploadedPayload = MessageQueueUtils.readMessagePayload<DataUploadedPayload>(payload, objectMapper)
                val dataId = dataUploadedPayload.dataId
                MessageQueueUtils.validateDataId(dataId)
                val bypassQa: Boolean = dataUploadedPayload.bypassQa

                logger.info("Received data with dataId $dataId and bypassQA $bypassQa on QA message queue (correlation Id: $correlationId)")

                val triggeringUserId = requireNotNull(metaDataControllerApi.getDataMetaInfo(dataId).uploaderUserId)

                val (qaStatus, comment) =
                    when (bypassQa) {
                        true -> Pair(QaStatus.Accepted, "Automatically QA approved.")
                        false -> Pair(QaStatus.Pending, null)
                    }

                val qaReviewEntity =
                    qaReviewManager.saveQaReviewEntity(
                        dataId = dataId,
                        qaStatus = qaStatus,
                        triggeringUserId = triggeringUserId,
                        comment = comment,
                        correlationId = correlationId,
                    )

                qaReviewManager.sendQaStatusUpdateMessage(
                    qaReviewEntity = qaReviewEntity, correlationId = correlationId,
                )
            }
        }

        /**
         * Method to retrieve message from dataStored exchange and constructing new one for quality_Assured exchange
         * @param messageAsJsonString the content of the message
         * @param correlationId the correlation ID of the current user process
         * @param type the type of the message
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            "itemStoredDocumentQaService",
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.ITEM_STORED, declare = "false"),
                    key = [RoutingKeyNames.DOCUMENT_QA],
                ),
            ],
        )
        fun assureQualityOfDocument(
            @Payload messageAsJsonString: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            MessageQueueUtils.validateMessageType(type, MessageType.QA_REQUESTED)
            val message = MessageQueueUtils.readMessagePayload<ManualQaRequestedMessage>(messageAsJsonString, objectMapper)
            val documentId = message.resourceId

            if (documentId.isEmpty()) {
                throw MessageQueueRejectException("Provided document ID is empty (correlationId: $correlationId)")
            }
            MessageQueueUtils.rejectMessageOnException {
                logger.info(
                    "Received document with Hash: $documentId on QA message queue with Correlation Id: $correlationId",
                )
                val messageToSend =
                    objectMapper.writeValueAsString(
                        QaStatusChangeMessage(
                            documentId,
                            QaStatus.Accepted,
                            null,
                        ),
                    )
                cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                    messageToSend, MessageType.QA_STATUS_UPDATED, correlationId, ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS,
                    RoutingKeyNames.DOCUMENT,
                )
            }
        }

        /**
         * Method that listens to the ItemStored Exchange for potential data deletion messages and deletes the corresponding
         * QA reports accordingly
         * @param payload the content of the message
         * @param correlationId the correlation ID of the current user process
         * @param type the type of the message
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            QueueNames.QA_SERVICE_DATASET_QA_DELETION,
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.BACKEND_DATASET_EVENTS, declare = "false"),
                    key = [RoutingKeyNames.DATASET_DELETION],
                ),
            ],
        )
        fun deleteQaInformationForDeletedDataId(
            @Payload payload: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            MessageQueueUtils.validateMessageType(type, MessageType.DELETE_DATA)
            MessageQueueUtils.rejectMessageOnException {
                val dataId = MessageQueueUtils.readMessagePayload<DataUploadedPayload>(payload, objectMapper).dataId
                MessageQueueUtils.validateDataId(dataId)
                qaReportManager.deleteAllQaReportsForDataId(dataId, correlationId)
                qaReviewManager.deleteAllByDataId(dataId, correlationId)
            }
        }

        /**
         * Method that listens to the BACKEND_DATASET_EVENTS Exchange for migration messages
         * and carries the migration to the QA reports
         * @param payload the content of the message
         * @param correlationId the correlation ID of the current user process
         * @param type the type of the message
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            QueueNames.QA_SERVICE_DATASET_MIGRATION,
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.BACKEND_DATASET_EVENTS, declare = "false"),
                    key = [RoutingKeyNames.DATASET_STORED_TO_ASSEMBLED_MIGRATION],
                ),
            ],
        )
        fun migrateStoredDatasetToAssembledDataset(
            @Payload payload: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            MessageQueueUtils.validateMessageType(type, MessageType.DATA_MIGRATED)
            MessageQueueUtils.rejectMessageOnException {
                val dataId = MessageQueueUtils.readMessagePayload<DataIdPayload>(payload, objectMapper).dataId
                assembledDataMigrationManager.migrateStoredDatasetToAssembledDataset(dataId, correlationId)
            }
        }

        /**
         * Method to retrieve message from dataStored exchange and constructing new one for qualityAssured exchange
         * @param payload the message body as a json string
         * @param correlationId the correlation ID of the current user process
         * @param type the type of the message
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            QueueNames.QA_SERVICE_DATA_POINT_QA,
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.BACKEND_DATA_POINT_EVENTS, declare = "false"),
                    key = [RoutingKeyNames.DATA_POINT_UPLOAD],
                ),
            ],
        )
        fun addReviewEntityForUploadedDataPoint(
            @Payload payload: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            MessageQueueUtils.validateMessageType(type, MessageType.PUBLIC_DATA_RECEIVED)

            MessageQueueUtils.rejectMessageOnException {
                val dataUploadedPayload = MessageQueueUtils.readMessagePayload<DataPointUploadedPayload>(payload, objectMapper)
                MessageQueueUtils.validateDataId(dataUploadedPayload.dataPointId)
                logger.info(
                    "Received QA required for datapoint dataId ${dataUploadedPayload.dataPointId} with " +
                        "initial QA status ${dataUploadedPayload.initialQaStatus} and message " +
                        "${dataUploadedPayload.initialQaComment} (correlation Id: $correlationId)",
                )

                saveQaReviewEntityFromMessage(dataUploadedPayload, correlationId)
            }
        }

        /**
         * Method to save a DataPointQaReviewEntity from a QaPayload
         * @param dataUploadedPayload the payload containing the dataId and bypassQa
         * @param correlationId the correlation ID of the current user process
         * @return the saved DataPointQaReviewEntity
         */
        fun saveQaReviewEntityFromMessage(
            dataUploadedPayload: DataPointUploadedPayload,
            correlationId: String,
        ): DataPointQaReviewEntity {
            val dataPointId = dataUploadedPayload.dataPointId
            val triggeringUserId = requireNotNull(dataPointControllerApi.getDataPointMetaInfo(dataPointId).uploaderUserId)

            val qaStatus = QaStatus.valueOf(dataUploadedPayload.initialQaStatus)

            return dataPointQaReviewManager.reviewDataPoint(
                dataPointId = dataPointId,
                qaStatus = qaStatus,
                triggeringUserId = triggeringUserId,
                comment = dataUploadedPayload.initialQaComment,
                correlationId = correlationId,
            )
        }
    }
