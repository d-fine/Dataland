package org.dataland.datalandcommunitymanager.services.elementaryEventProcessing

import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.dataland.datalandcommunitymanager.model.elementaryEventProcessing.ElementaryEventBasicInfo
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandcommunitymanager.services.NotificationService
import org.dataland.datalandcommunitymanager.utils.PayloadValidator.validatePayloadAndReturnElementaryEventBasicInfo
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
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
@Component
class PrivateDataUploadProcessor(
    @Autowired val messageUtils: MessageQueueUtils,
    @Autowired val notificationService: NotificationService,
    @Autowired override val elementaryEventRepository: ElementaryEventRepository,
) : BaseEventProcessor() {
    override val logger = LoggerFactory.getLogger(this.javaClass)

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
                key = [""],
            ),
        ],
    )
    override fun processEvent(
        @Payload payload: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        super.runProcessingLogicIfFeatureFlagEnabled(payload, correlationId, type)
    }

    override fun runProcessingLogic(payload: String, correlationId: String, type: String) {
        messageUtils.validateMessageType(type, MessageType.PrivateDataReceived)
        val elementaryEventBasicInfo =
            validatePayloadAndReturnElementaryEventBasicInfo(payload, ActionType.StorePrivateDataAndDocuments)

        logger.info(
            "Processing elementary event: Request for storage of private framework data. " +
                "CorrelationId: $correlationId",
        )
        val storedElementaryEvent = createAndSaveElementaryUploadEvent(elementaryEventBasicInfo)
        notificationService.notifyOfElementaryEvents(storedElementaryEvent, correlationId)
    }

    private fun createAndSaveElementaryUploadEvent(elementaryEventBasicInfo: ElementaryEventBasicInfo) =
        super.createAndSaveElementaryEvent(elementaryEventBasicInfo, ElementaryEventType.UploadEvent)
}
