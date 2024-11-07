package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewLogEntity
import org.dataland.datalandqaservice.repositories.QaReviewRepository
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
import java.time.Instant

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
        @Autowired val datasetQaReviewRepository: QaReviewRepository,
        @Autowired val companyDataControllerApi: CompanyDataControllerApi,
        @Autowired val metaDataControllerApi: MetaDataControllerApi,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        private val reviewerIdAutomatedQaService = "automated-qa-service"

        private data class ForwardedQaMessage(
            val identifier: String,
            val comment: String,
        )

        private data class PersistAutomatedQaResultMessage(
            val identifier: String,
            val validationResult: QaStatus,
            val reviewerId: String,
            val resourceType: String,
            val message: String?,
        )

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
                            "manualQaRequestedDataQaService",
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.MANUAL_QA_REQUESTED, declare = "false"),
                    key = [RoutingKeyNames.DATA],
                ),
            ],
        )
        @Transactional
        fun addDataSetToQaReviewQueue(
            @Payload messageAsJsonString: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            messageUtils.validateMessageType(type, MessageType.MANUAL_QA_REQUESTED)
            val message = objectMapper.readValue(messageAsJsonString, ForwardedQaMessage::class.java)

            val comment = message.comment
            val dataId = message.identifier
            if (dataId.isEmpty()) {
                throw MessageQueueRejectException("Provided data ID is empty")
            }

            val dataMetaInfo = metaDataControllerApi.getDataMetaInfo(dataId)
            val companyName = companyDataControllerApi.getCompanyById(dataMetaInfo.companyId).companyInformation.companyName

            messageUtils.rejectMessageOnException {
                logger.info("Received data with DataId: $dataId on QA message queue with Correlation Id: $correlationId")
                storeDatasetWithQaStatusPending(
                    dataId = dataId,
                    companyId = dataMetaInfo.companyId,
                    companyName = companyName,
                    dataType = dataMetaInfo.dataType,
                    reportingPeriod = dataMetaInfo.reportingPeriod,
                    uploaderId = dataMetaInfo.uploaderUserId ?: "",
                    comment,
                )
            }
        }

        /**
         * Save new QaReviewLogEntity for new pending dataset in database
         */
        @Suppress("LongParameterList")
        private fun storeDatasetWithQaStatusPending(
            dataId: String,
            companyId: String,
            companyName: String,
            dataType: DataTypeEnum,
            reportingPeriod: String,
            uploaderId: String,
            comment: String,
        ) {
            datasetQaReviewRepository.save(
                QaReviewLogEntity(
                    dataId = dataId,
                    companyId = companyId,
                    companyName = companyName,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    timestamp = Instant.now().toEpochMilli(),
                    qaStatus = QaStatus.Pending,
                    reviewerId = uploaderId,
                    comment = comment,
                ),
            )
        }

        /**
         * Method to retrieve message from dataStored exchange and constructing new one for quality_Assured exchange
         * @param messageAsJsonString the message body as json string
         * @param correlationId the correlation ID of the current user process
         * @param type the type of the message
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            "manualQaRequestedDocumentQaService",
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.MANUAL_QA_REQUESTED, declare = "false"),
                    key = [RoutingKeyNames.DOCUMENT],
                ),
            ],
        )
        fun assureQualityOfDocument(
            @Payload messageAsJsonString: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            messageUtils.validateMessageType(type, MessageType.MANUAL_QA_REQUESTED)
            val forwardedQaMessage = objectMapper.readValue(messageAsJsonString, ForwardedQaMessage::class.java)
            val documentId = forwardedQaMessage.identifier
            if (documentId.isEmpty()) {
                throw MessageQueueRejectException("Provided document ID is empty")
            }
            messageUtils.rejectMessageOnException {
                logger.info(
                    "Received document with Hash: $documentId on QA message queue with Correlation Id: $correlationId",
                )
                val messageToSend =
                    objectMapper.writeValueAsString(
                        QaCompletedMessage(documentId, QaStatus.Accepted, reviewerIdAutomatedQaService, null),
                    )
                cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                    messageToSend, MessageType.QA_STATUS_CHANGED, correlationId, ExchangeName.DATA_QUALITY_ASSURED,
                    RoutingKeyNames.DOCUMENT,
                )
            }
        }

        /**
         * Method to retrieve qa completed message and store the
         * @param messageAsJsonString the message body as json string
         * @param correlationId the correlation ID of the current user process
         * @param type the type of the message
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            "manualQaRequestedPersistAutomatedQaResultQaService",
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.MANUAL_QA_REQUESTED, declare = "false"),
                    key = [RoutingKeyNames.PERSIST_AUTOMATED_QA_RESULT],
                ),
            ],
        )
        @Transactional
        fun addDataReviewFromAutomatedQaToReviewHistoryRepository(
            @Payload messageAsJsonString: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            messageUtils.validateMessageType(type, MessageType.PERSIST_AUTOMATED_QA_RESULT)
            val persistAutomatedQaResultMessage =
                objectMapper.readValue(messageAsJsonString, PersistAutomatedQaResultMessage::class.java)
            if (persistAutomatedQaResultMessage.resourceType == "data") {
                val validationResult = persistAutomatedQaResultMessage.validationResult
                val reviewerId = persistAutomatedQaResultMessage.reviewerId
                val dataId = persistAutomatedQaResultMessage.identifier
                if (dataId.isEmpty()) {
                    throw MessageQueueRejectException("Provided data ID is empty")
                }

                val dataMetaInfo = metaDataControllerApi.getDataMetaInfo(dataId)
                val companyName = companyDataControllerApi.getCompanyById(dataMetaInfo.companyId).companyInformation.companyName

                messageUtils.rejectMessageOnException {
                    logger.info(
                        "Received data with DataId: $dataId on QA message queue with Correlation Id: $correlationId",
                    )
                    logger.info(
                        "Assigning quality status $validationResult and reviewerId $reviewerId to dataset with ID $dataId",
                    )
                    datasetQaReviewRepository.save(
                        QaReviewLogEntity(
                            dataId = dataId,
                            companyId = dataMetaInfo.companyId,
                            companyName = companyName,
                            dataType = dataMetaInfo.dataType,
                            reportingPeriod = dataMetaInfo.reportingPeriod,
                            timestamp = Instant.now().toEpochMilli(),
                            qaStatus = validationResult,
                            reviewerId = reviewerId,
                            comment = persistAutomatedQaResultMessage.message,
                        ),
                    )
                }
            }
        }
    }
