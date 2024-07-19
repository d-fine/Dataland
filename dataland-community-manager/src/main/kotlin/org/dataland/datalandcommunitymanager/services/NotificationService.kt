package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.EventType
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.json.JSONObject
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service("NotificationService")
class NotificationService
@Suppress("LongParameterList")
constructor(
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var elementaryEventRepository: ElementaryEventRepository,
    @Autowired var messageUtils: MessageQueueUtils,
    @Autowired var metaDataControllerApi: MetaDataControllerApi,
    @Autowired var notificationEventRepository: NotificationEventRepository,
    @Autowired var objectMapper: ObjectMapper,
    @Value("\${dataland.community-manager.notification-threshold-days:30}")
    val notificationThresholdDays: Int,
    @Value("\${dataland.community-manager.notification-elementaryevents-threshold:10}")
    val elementaryEventsThreshold: Int,
) {
    /**
     * Method that listens to the storage_queue and stores data into the database in case there is a message on the
     * storage_queue
     * @param payload the content of the message
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "requestReceivedInternalStorageDatabaseDataStore",
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
    ) //TODO: Online SingleDataRequests?
    fun processDataUploadEvent(
        @Payload payload: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.PublicDataReceived)
        val dataId = JSONObject(payload).getString("dataId")
        val actionType = JSONObject(payload).getString("actionType")
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty.")
        }

        //TODO: StorePrivateData reachable?
        if (actionType.isEmpty() or
            (actionType !== ActionType.StorePublicData && actionType !== ActionType.StorePrivateDataAndDocuments)
        ) {
            return
        }

        val companyIdOfUpload = metaDataControllerApi.getDataMetaInfo(dataId).companyId

        val previouslyUnsentElementaryEvents = getUnsentElementaryEventsForCompany(companyIdOfUpload)

        val elementaryEvent = createAndSaveElementaryEvent(dataId)

        when {
            isLastNotificationEventForCompanyOlderThanThreshold(companyIdOfUpload) &&
                previouslyUnsentElementaryEvents.isEmpty()
            -> singleNotificationResponse(elementaryEvent)
            isLastNotificationEventForCompanyOlderThanThreshold(companyIdOfUpload) &&
                previouslyUnsentElementaryEvents.isNotEmpty()
            -> {
                val allUnsentElementaryEvents = previouslyUnsentElementaryEvents.toMutableList()
                allUnsentElementaryEvents.add(elementaryEvent)
                summaryNotificationResponse(allUnsentElementaryEvents)
            }
            !isLastNotificationEventForCompanyOlderThanThreshold(companyIdOfUpload) &&
                previouslyUnsentElementaryEvents.size + 1 >= elementaryEventsThreshold
            -> {
                val allUnsentElementaryEvents = previouslyUnsentElementaryEvents.toMutableList()
                allUnsentElementaryEvents.add(elementaryEvent)
                summaryNotificationResponse(allUnsentElementaryEvents)
            }
        }
    }

    fun singleNotificationResponse(elementaryEvent: ElementaryEventEntity) {
        val newNotificationEvent = NotificationEventEntity(
            elementaryEvents = mutableListOf(elementaryEvent),
            companyId = elementaryEvent.companyId,
            creationTimestamp = Instant.now().toEpochMilli(),
        )

        notificationEventRepository.saveAndFlush(newNotificationEvent)
        elementaryEvent.notificationEvent = newNotificationEvent
        elementaryEventRepository.saveAndFlush(elementaryEvent)
        sendSingleEmailMessageToQueue()
    }

    fun summaryNotificationResponse(elementaryEvents: List<ElementaryEventEntity>) {
        val newNotificationEvent = NotificationEventEntity(
            elementaryEvents = elementaryEvents,
            companyId = elementaryEvents.first().companyId,
            creationTimestamp = Instant.now().toEpochMilli(),
        )

        notificationEventRepository.saveAndFlush(newNotificationEvent)
        elementaryEvents.forEach { it.notificationEvent = newNotificationEvent }
        elementaryEventRepository.saveAllAndFlush(elementaryEvents)
        sendSummaryEmailMessageToQueue()
    }

    /**
     * Function saves elementaryEvent for dataUpload and returns saved element
     */
    private fun createAndSaveElementaryEvent(dataId: String): ElementaryEventEntity {
        val dataMetaInformation = metaDataControllerApi.getDataMetaInfo(dataId)

        return elementaryEventRepository.saveAndFlush(
            ElementaryEventEntity(
                eventType = EventType.UploadEvent,
                companyId = dataMetaInformation.companyId,
                framework = dataMetaInformation.dataType,
                reportingPeriod = dataMetaInformation.reportingPeriod,
                creationTimestamp = Instant.now().toEpochMilli(),
                notificationEvent = null,
            ),
        )
    }

    /**
     * Function retrieves all elementaryEvents for a specific company which
     * have not been sent by a previous notification event
     * @param companyId
     * @return list of elementaryEvents
     */
    private fun getUnsentElementaryEventsForCompany(companyId: String): List<ElementaryEventEntity> {
        return elementaryEventRepository.findAllByCompanyIdAndNotificationEventIsNull(companyId)
    }

    /**
     * Function retrieves all elementaryEvents for a specific company which
     * have not been sent by a previous notification event
     * @param companyId
     * @return list of elementaryEvents
     */
    private fun isLastNotificationEventForCompanyOlderThanThreshold(companyId: String): Boolean {
        val lastNotificationEvent = notificationEventRepository.findNotificationEventByCompanyId(companyId)
            .maxByOrNull { it.creationTimestamp }
        return lastNotificationEvent == null || Duration.between(Instant.ofEpochMilli(lastNotificationEvent.creationTimestamp), Instant.now())
            .toDays() > notificationThresholdDays
    }

    fun sendSingleEmailMessageToQueue() {}
    fun sendSummaryEmailMessageToQueue() {}
}
