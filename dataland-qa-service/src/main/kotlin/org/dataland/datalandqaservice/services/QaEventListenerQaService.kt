package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.ManualQaRequestedMessage
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
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
import org.springframework.transaction.annotation.Transactional

/**
 * The QA Service's MessageListener Service. Listens to messages (usually) sent be the internal storage after a dataset
 * has been stored or if a dataset has been deleted. Calls, depending on the data event (storing, deletion), the
 * corresponding methods of the QaReviewManager and QaReportManager
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 */
@Component
class QaEventListenerQaService
    @Suppress("LongParameterList")
    constructor(
        @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
        @Autowired var objectMapper: ObjectMapper,
        @Autowired val qaReviewManager: QaReviewManager,
        @Autowired val qaReportManager: QaReportManager,
        @Autowired val metaDataControllerApi: MetaDataControllerApi,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Method to retrieve message from dataStored exchange and constructing new one for qualityAssured exchange
         * @param messageAsJsonString the message body as a json string
         * @param correlationId the correlation ID of the current user process
         * @param type the type of the message
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            "itemStoredDataQaService",
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.ITEM_STORED, declare = "false"),
                    key = [RoutingKeyNames.DATA_QA],
                ),
            ],
        )
        @Transactional
        fun addDatasetToQaReviewRepository(
            @Payload messageAsJsonString: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            MessageQueueUtils.validateMessageType(type, MessageType.MANUAL_QA_REQUESTED)
            val message = MessageQueueUtils.readMessagePayload<ManualQaRequestedMessage>(messageAsJsonString, objectMapper)

            val dataId = message.resourceId
            val bypassQa: Boolean? = message.bypassQa
            if (dataId.isEmpty()) {
                throw MessageQueueRejectException("Provided data ID is empty (correlationId: $correlationId)")
            }

            MessageQueueUtils.rejectMessageOnException {
                logger.info("Received data with dataId $dataId and bypassQA $bypassQa on QA message queue (correlation Id: $correlationId)")
                val reviewerId = metaDataControllerApi.getDataMetaInfo(dataId).uploaderUserId ?: "No Uploader available"
                val qaStatus: QaStatus
                var comment: String? = null

                when (bypassQa) {
                    true -> {
                        qaStatus = QaStatus.Accepted
                        comment = "Automatically QA approved."
                    }
                    false -> qaStatus = QaStatus.Pending
                    null -> throw MessageQueueRejectException(
                        "BypassQa is not set; message should not end up here" +
                            " (correlationId: $correlationId)",
                    )
                }

                qaReviewManager.saveQaReviewEntityAndSendQaStatusChangeMessage(
                    dataId = dataId,
                    qaStatus = qaStatus,
                    reviewerId = reviewerId,
                    comment = comment,
                    correlationId = correlationId,
                )
            }
        }

        /**
         * Method to retrieve message from dataStored exchange and constructing new one for quality_Assured exchange
         * @param blobId the documentId sent as payload
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
            MessageQueueUtils.validateMessageType(type, MessageType.MANUAL_QA_REQUESTED)
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
                    messageToSend, MessageType.QA_STATUS_CHANGED, correlationId, ExchangeName.DATA_QUALITY_ASSURED,
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
                            "itemStoredDeleteQaInfoQaService",
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.ITEM_STORED, declare = "false"),
                    key = [RoutingKeyNames.DELETE_QA_INFO],
                ),
            ],
        )
        @Transactional
        fun deleteQaInformationForDeletedDataId(
            @Payload messageAsJsonString: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            MessageQueueUtils.validateMessageType(type, MessageType.MANUAL_QA_REQUESTED)
            val message = MessageQueueUtils.readMessagePayload<ManualQaRequestedMessage>(messageAsJsonString, objectMapper)

            val dataId = message.resourceId
            val bypassQa = message.bypassQa
            if (dataId.isEmpty()) {
                throw MessageQueueRejectException("Provided data ID is empty (correlationId: $correlationId)")
            }
            if (bypassQa != null) {
                throw MessageQueueRejectException("BypassQa should be set to null when deleting QA information.")
            }

            MessageQueueUtils.rejectMessageOnException {
                qaReportManager.deleteAllQaReportsForDataId(dataId, correlationId)
                qaReviewManager.deleteAllByDataId(dataId, correlationId)
            }
        }
    }
