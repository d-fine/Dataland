package org.dataland.datalandcommunitymanager.services.elementaryEventProcessing

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandcommunitymanager.services.NotificationService
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.json.JSONObject
import org.slf4j.Logger
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
 * Defines the processing of private framework data upload events as elementary events
 */
@Component("PrivateDataUploadProcessor")
class PrivateDataUploadProcessor(
    @Autowired notificationService: NotificationService,
    @Autowired uploadEventRepository: UploadEventRepository,
    @Autowired objectMapper: ObjectMapper,
) : BaseEventProcessor(notificationService, uploadEventRepository, objectMapper) {
    override var notificationEventType = UploadEventType.DataUploadEvent
    override var messageType = MessageType.PRIVATE_DATA_RECEIVED
    override var actionType = ActionType.STORE_PRIVATE_DATA_AND_DOCUMENTS
    override var logger: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Method that listens to private data storage requests, persists them as elementary events and asks the
     * Notification service to potentially send notifications
     * @param payload content of the private data storage message
     * @param correlationId the correlation ID of the current user process that has triggered this message
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        "privateRequestReceivedCommunityManagerNotificationService",
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
    fun processEvent(
        @Payload payload: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        validateIncomingPayloadAndReturnDataId(payload, type)

        MessageQueueUtils.rejectMessageOnException {
            super.processEvent(
                createElementaryEventBasicInfo(payload),
                correlationId,
                type,
            )
        }
    }

    /**
     * Validates the incoming Payloads and returns the dataId
     * @param payload the Payload as a string
     * @param messageType the type of the message
     * @returns the dataId of the dataset
     */
    fun validateIncomingPayloadAndReturnDataId(
        payload: String,
        messageType: String,
    ): String {
        MessageQueueUtils.validateMessageType(messageType, this.messageType)

        val payloadJsonObject = JSONObject(payload)

        val actionType = payloadJsonObject.getString("actionType")

        if (actionType != this.actionType) {
            throw MessageQueueRejectException(
                "Expected action type ${this.actionType}, but was $actionType.",
            )
        }

        return payloadJsonObject
            .getString("dataId")
            .takeIf { it.isNotEmpty() }
            ?: throw MessageQueueRejectException("The dataId in the message payload is empty.")
    }
}
