package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.ReviewHistoryRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.ReviewQueueRepository
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
 * Service class for listening to the QA report deletion requests
 * @param messageUtils utils for handling of messages
 * @param reportManager service for managing QA reports
 */
@Service
class QaReportEventListenerService(
    @Autowired private val messageUtils: MessageQueueUtils,
    @Autowired private val reportManager: QaReportManager,
    @Autowired val reviewQueueRepository: ReviewQueueRepository,
    @Autowired val reviewHistoryRepository: ReviewHistoryRepository,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method that listens to the Request Received queue for potential data deletion messages
     * and deletes the corresponding QA reports accordingly
     * @param payload the content of the message
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "qaReportDeleteDataRequestReceivedQueue",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DeadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.RequestReceived, declare = "false"),
                key = [""],
            ),
        ],
    )
    @Transactional
    fun distributeIncomingRequests(
        @Payload payload: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.PublicDataReceived)
        val payloadJson = JSONObject(payload)
        val dataId = payloadJson.getString("dataId")
        val actionType = payloadJson.getString("actionType")
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty.")
        }

        logger.info("Deleting all QA Reports associated with data id $dataId. CorrelationId: $correlationId")
        messageUtils.rejectMessageOnException {
            if (actionType == ActionType.DeleteData) {
                reportManager.deleteAllQaReportsForDataId(dataId)
                reviewQueueRepository.deleteByDataId(dataId)
                reviewHistoryRepository.deleteByDataId(dataId)
            }
        }
    }
}
