package org.dataland.datalandcommunitymanager.services.elementaryEventProcessing

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.model.elementaryEventProcessing.ElementaryEventBasicInfo
import org.dataland.datalandcommunitymanager.repositories.UploadEventRepository
import org.dataland.datalandcommunitymanager.services.NotificationService
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
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
 * Defines the processing of public framework data upload events as elementary events
 */
@Component
class PublicDataUploadProcessor(
    @Autowired notificationService: NotificationService,
    @Autowired uploadEventRepository: UploadEventRepository,
    @Autowired objectMapper: ObjectMapper,
    @Autowired val metaDataControllerApi: MetaDataControllerApi,
) : BaseEventProcessor(notificationService, uploadEventRepository, objectMapper) {
    override val elementaryEventType = NotificationEventType.UploadEvent
    override val messageType = MessageType.QA_STATUS_UPDATED
    override val actionType = null
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
                value =
                    Queue(
                        "dataQualityAssuredCommunityManagerNotificationService",
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS, declare = "false"),
                key = [RoutingKeyNames.DATA],
            ),
        ],
    )
    fun processEvent(
        @Payload payload: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        MessageQueueUtils.validateMessageType(messageType, this.messageType)
        val qaCompletedMessage = MessageQueueUtils.readMessagePayload<QaStatusChangeMessage>(payload, objectMapper)
        if (qaCompletedMessage.updatedQaStatus != QaStatus.Accepted) {
            return
        }

        MessageQueueUtils.rejectMessageOnException {
            super.processEvent(
                createElementaryEventBasicInfo(
                    objectMapper.writeValueAsString(metaDataControllerApi.getDataMetaInfo(qaCompletedMessage.dataId)),
                ),
                correlationId,
                type,
            )
        }
    }

    override fun createElementaryEventBasicInfo(jsonString: String): ElementaryEventBasicInfo {
        val metaDataJsonObject = JSONObject(jsonString)
        val frameworkValue = metaDataJsonObject.getString("dataType")
        metaDataJsonObject.remove("dataType")
        metaDataJsonObject.put("framework", frameworkValue)

        return super.createElementaryEventBasicInfo(metaDataJsonObject.toString())
    }
}
