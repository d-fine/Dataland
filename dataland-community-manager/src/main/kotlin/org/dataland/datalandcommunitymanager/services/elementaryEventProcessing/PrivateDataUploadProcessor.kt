package org.dataland.datalandcommunitymanager.services.elementaryEventProcessing

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
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
import java.util.*

/**
 * Defines the processing of private framework data upload events as elementary events
 */
@Component("PrivateDataUploadProcessor")
class PrivateDataUploadProcessor(
    @Autowired messageUtils: MessageQueueUtils,
    @Autowired notificationService: NotificationService,
    @Autowired elementaryEventRepository: ElementaryEventRepository,
    @Autowired objectMapper: ObjectMapper,
) : BaseEventProcessor(messageUtils, notificationService, elementaryEventRepository, objectMapper) {

    override var elementaryEventType = ElementaryEventType.UploadEvent
    override var messageType = MessageType.PrivateDataReceived
    override var actionType = ActionType.StorePrivateDataAndDocuments
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
                value = Queue(
                    "privateRequestReceivedCommunityManagerNotificationService",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DeadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.PrivateRequestReceived, declare = "false"),
                key = [RoutingKeyNames.metaDataPersisted],
            ),
        ],
    )
    fun processEvent(
        @Payload payload: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        validateIncomingPayloadAndReturnDataId(payload, type)

        super.processEvent(
            createElementaryEventBasicInfo(payload),
            correlationId,
            type,
        )
    }

    override fun validateIncomingPayloadAndReturnDataId(payload: String, messageType: String): String {
        messageUtils.validateMessageType(messageType, this.messageType)

        val payloadJsonObject = JSONObject(payload)

        val actionType = payloadJsonObject.getString("actionType")

        if (actionType != this.actionType) {
            throw MessageQueueRejectException(
                "Expected action type ${this.actionType}, but was $actionType.",
            )
        }

        return payloadJsonObject.getString("dataId")
            .takeIf { it.isNotEmpty() }
            ?: throw MessageQueueRejectException("The dataId in the message payload is empty.")
    }
}
