package org.dataland.datalandcommunitymanager.services.eeProcessing

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandcommunitymanager.services.NotificationService
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
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
import org.springframework.stereotype.Component
import java.util.*

/**
 * Defines the processing of private framework data upload events as elementary events
 */
@Component
class PrivateDataUploadProcessor(
    @Autowired val messageUtils: MessageQueueUtils,
    @Autowired val notificationService: NotificationService,
    @Autowired val metaDataControllerApi: MetaDataControllerApi,
    @Autowired override val elementaryEventRepository: ElementaryEventRepository,
) : BaseEventProcessor() {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * Method that listens to private data storage requests, persists them as elementary events and potentially
     * creates a notification event if specific requirements are met
     * @param payload the content of the message
     * @param correlationId the correlation ID of the current user process
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
        messageUtils.validateMessageType(type, MessageType.PrivateDataReceived)

        val dataId = JSONObject(payload).getString("dataId")
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty.")
        }
        val actionType = JSONObject(payload).getString("actionType")
        if (actionType != ActionType.StorePrivateDataAndDocuments) {
            throw MessageQueueRejectException(
                "Expected action type ${ActionType.StorePrivateDataAndDocuments}, but was $actionType.",
            )
        }

        logger.info("Processing elementary event: Request for storage of private framework data.")

        val dataMetaInfo = metaDataControllerApi.getDataMetaInfo(dataId) // TODO Emanuel: problem => lets discuss
        val companyId = UUID.fromString(dataMetaInfo.companyId)

        createAndSaveElementaryUploadEvent(dataMetaInfo)

        val unprocessedElementaryEvents = getUnprocessedElementaryEventsForCompany(companyId)

        notificationService.notifyOfElementaryEvents(unprocessedElementaryEvents, correlationId)
    }

    private fun createAndSaveElementaryUploadEvent(dataMetaInfo: DataMetaInformation) =
        super.createAndSaveElementaryEvent(dataMetaInfo, ElementaryEventType.UploadEvent)

    private fun getUnprocessedElementaryEventsForCompany(companyId: UUID): List<ElementaryEventEntity> =
        super.getUnprocessedElementaryEventsForCompany(companyId, ElementaryEventType.UploadEvent)
}
