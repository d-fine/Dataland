package org.dataland.datalandcommunitymanager.services.elementaryEventProcessing

import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandcommunitymanager.services.NotificationService
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
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
* Defines the processing of public framework data upload events as elementary events
*/
@Component
class PublicDataUploadProcessor(
    @Autowired messageUtils: MessageQueueUtils,
    @Autowired notificationService: NotificationService,
    @Autowired elementaryEventRepository: ElementaryEventRepository,
) : BaseEventProcessor(messageUtils, notificationService, elementaryEventRepository) {

    override var elementaryEventType = ElementaryEventType.UploadEvent
    override var messageType = MessageType.PublicDataReceived
    override var actionType = ActionType.StorePublicData
    override var logger: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Method that listens to public data storage requests, persists them as elementary events and asks the
     * Notification service to potentially send notifications
     * @param payload content of the public data storage message
     * @param correlationId the correlation ID of the current user process that has triggered this message
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "requestReceivedCommunityManagerNotificationService",
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
    override fun processEvent(
        @Payload payload: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        super.processEvent(payload, correlationId, type)
    }
}
