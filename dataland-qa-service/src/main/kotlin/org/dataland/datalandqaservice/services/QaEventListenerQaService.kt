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
 * Implementation of a QA Service reacting on the upload_queue and forwarding message to qa_queue
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 */
@Component
class QaEventListenerQaService
    @Suppress("LongParameterList")
    constructor(
        @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
        @Autowired var objectMapper: ObjectMapper,
        @Autowired var messageUtils: MessageQueueUtils,
        @Autowired val qaReviewManager: QaReviewManager,
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
        fun addDatasetToQaReviewRepositoryWithStatusPending(
            @Payload messageAsJsonString: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            messageUtils.validateMessageType(type, MessageType.MANUAL_QA_REQUESTED)
            val message = objectMapper.readValue(messageAsJsonString, ManualQaRequestedMessage::class.java)

            val dataId = message.resourceId
            if (dataId.isEmpty()) {
                throw MessageQueueRejectException("Provided data ID is empty (correlationId: $correlationId)")
            }

            messageUtils.rejectMessageOnException {
                logger.info("Received data with DataId: $dataId on QA message queue with Correlation Id: $correlationId")
                qaReviewManager.saveQaReviewEntityAndSendQaStatusChangeMessage(
                    dataId = dataId,
                    qaStatus = QaStatus.Pending,
                    reviewerId = metaDataControllerApi.getDataMetaInfo(dataId).uploaderUserId ?: "No Uploader available",
                    comment = null,
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
            messageUtils.validateMessageType(type, MessageType.MANUAL_QA_REQUESTED)
            val message = objectMapper.readValue(messageAsJsonString, ManualQaRequestedMessage::class.java)
            val documentId = message.resourceId

            if (documentId.isEmpty()) {
                throw MessageQueueRejectException("Provided document ID is empty (correlationId: $correlationId)")
            }
            messageUtils.rejectMessageOnException {
                logger.info(
                    "Received document with Hash: $documentId on QA message queue with Correlation Id: $correlationId",
                )
                val messageToSend =
                    objectMapper.writeValueAsString(
                        QaStatusChangeMessage(
                            documentId,
                            QaStatus.Accepted,
                            documentId,
                        ),
                    )
                cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                    messageToSend, MessageType.QA_STATUS_CHANGED, correlationId, ExchangeName.DATA_QUALITY_ASSURED,
                    RoutingKeyNames.DOCUMENT,
                )
            }
        }

        /**
         * Method to retrieve bypassQA message and store the message in the review repository
         * @param messageAsJsonString the message body as json string
         * @param correlationId the correlation ID of the current user process
         * @param type the type of the message
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            "itemStoredPersistBypassQaResultQaService",
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.ITEM_STORED, declare = "false"),
                    key = [RoutingKeyNames.PERSIST_BYPASS_QA_RESULT],
                ),
            ],
        )
        @Transactional
        fun addDatasetWithBypassQaTrueToQaReviewRepository(
            @Payload messageAsJsonString: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            messageUtils.validateMessageType(type, MessageType.PERSIST_BYPASS_QA_RESULT)
            val message = objectMapper.readValue(messageAsJsonString, ManualQaRequestedMessage::class.java)
            val dataId = message.resourceId
            val bypassQa = message.bypassQa

            if (dataId.isEmpty()) {
                throw MessageQueueRejectException("Provided data ID is empty (correlationId: $correlationId)")
            }
            if (bypassQa == null || !bypassQa) {
                throw MessageQueueRejectException(
                    "'BypassQa' is not true; this message should not end up here. " +
                        "(correlationId: $correlationId)",
                )
            }

            messageUtils.rejectMessageOnException {
                logger.info("Received data with DataId: $dataId on QA message queue with Correlation Id: $correlationId")

                val reviewerId = metaDataControllerApi.getDataMetaInfo(dataId).uploaderUserId ?: "No Uploader available"

                logger.info("BypassQa: Assigning quality status ${QaStatus.Accepted} and reviewerId $reviewerId to dataset with ID $dataId")

                qaReviewManager.saveQaReviewEntityAndSendQaStatusChangeMessage(
                    dataId = dataId,
                    qaStatus = QaStatus.Accepted,
                    reviewerId = reviewerId,
                    comment = "Automatically QA approved.",
                    correlationId = correlationId,
                )
            }
        }
    }
