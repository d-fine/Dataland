package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.json.JSONObject
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
import org.springframework.transaction.annotation.Transactional

/**
 * This service checks if freshly uploaded and validated data answers a data request
 */
@Service("DataRequestUpdater")
class DataRequestUploadListener(
    @Autowired private val messageUtils: MessageQueueUtils,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val dataRequestAlterationManager: DataRequestAlterationManager,
) {
    private val logger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

    /**
     * Checks if for a given dataset there are open requests with matching company identifier, reporting period
     * and data type and sets their status to answered
     * @param jsonString the message describing the result of the completed QA process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        "dataQualityAssuredCommunityManagerDataManager",
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.DATA_QUALITY_ASSURED, declare = "false"),
                key = [RoutingKeyNames.DATA],
            ),
        ],
    )
    @Transactional
    fun changeRequestStatusAfterUpload(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) id: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.QA_COMPLETED)
        val qaCompletedMessage = objectMapper.readValue(jsonString, QaCompletedMessage::class.java)
        val dataId = qaCompletedMessage.identifier
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        logger.info("Received data QA completed message for dataset with ID $dataId")
        if (qaCompletedMessage.validationResult != QaStatus.Accepted) {
            logger.info("Dataset with ID $dataId was not accepted and request matching is cancelled")
            return
        }
        messageUtils.rejectMessageOnException {
            dataRequestAlterationManager.patchRequestStatusFromOpenToAnsweredByDataId(dataId, correlationId = id)
        }
    }

    /**
     * Checks if for a given dataset there are open requests with matching company identifier, reporting period
     * and data type and sets their status to answered and handles the update of the access status
     * @param dataId the dataId of the uploaded data
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        "privateRequestReceivedCommunityManager",
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.PRIVATE_REQUEST_RECEIVED, declare = "false"),
                key = [RoutingKeyNames.META_DATA_PERSISTED],
            ),
        ],
    )
    @Transactional
    fun changeRequestStatusAfterPrivateDataUpload(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) id: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.PRIVATE_DATA_RECEIVED)
        val payloadJsonObject = JSONObject(payload)
        val dataId = payloadJsonObject.getString("dataId")
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        messageUtils.rejectMessageOnException {
            dataRequestAlterationManager.patchRequestStatusFromOpenToAnsweredByDataId(dataId, correlationId = id)
        }
    }
}
