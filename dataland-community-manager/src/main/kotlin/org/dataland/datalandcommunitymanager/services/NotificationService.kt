package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
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
import java.util.UUID

@Service("NotificationService")
class NotificationService
@Suppress("LongParameterList")
constructor(
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var messageUtils: MessageQueueUtils,
    @Autowired var elementaryEventRepository: ElementaryEventRepository,
    @Autowired var notificationEventRepository: NotificationEventRepository,
    @Autowired var metaDataControllerApi: MetaDataControllerApi,
    @Autowired var objectMapper: ObjectMapper,
    @Value("\${dataland.community-manager.notification-threshold-days:30}")
    val notificationThresholdDays: Int,
    @Value("\${dataland.community-manager.notification-elementaryevents-threshold:10}")
    val elementaryEventsThreshold: Int,
) {

    /* TODO Emanuel: Ich glaube wir müssen noch einen RabbitListener für die private data queue einführen. Der
    schaut dann halt auf die andere queue (für private data), ruft aber dieselben Funktionen hier auf wie in
    "processPublicDataUploadEvent"
    */

    /**
     * Method that listens to the storage_queue and stores data into the database in case there is a message on the
     * storage_queue //TODO wrong description
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
    )
    fun processDataUploadEvent( // TODO Emanuel: wird wsl zu "prcoessPublicDataUploadEvent"
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
        if (actionType !== ActionType.StorePublicData) {
            throw MessageQueueRejectException("Provided action type is unexpected.")
        }

        // TODO Der Teil ab hier muss ausglagert werden, und dann verwenden wir ihn sowohl für den Listener auf die
        // "public" queue, als auch für die "private" queue

        val companyIdOfUpload = UUID.fromString(metaDataControllerApi.getDataMetaInfo(dataId).companyId)

        // TODO Emanuel: Get only those which match the elementary event type (data upload)
        val previouslyUnsentElementaryEvents = getUnsentElementaryEventsForCompany(companyIdOfUpload)

        val elementaryEvent = createAndSaveElementaryEvent(dataId)

        // TODO Emanuel: The "decision-function" needs to be in a seperate function so I can test it better in unit test
        when {
            isLastNotificationEventForCompanyOlderThanThreshold(companyIdOfUpload) &&
                previouslyUnsentElementaryEvents.isEmpty()
            -> createAndSendSingleNotificationResponse(elementaryEvent)
            isLastNotificationEventForCompanyOlderThanThreshold(companyIdOfUpload) &&
                previouslyUnsentElementaryEvents.isNotEmpty()
            -> {
                val allUnsentElementaryEvents = previouslyUnsentElementaryEvents.toMutableList()
                allUnsentElementaryEvents.add(elementaryEvent)
                createAndSendSummaryNotificationResponse(allUnsentElementaryEvents)
            }
            !isLastNotificationEventForCompanyOlderThanThreshold(companyIdOfUpload) &&
                previouslyUnsentElementaryEvents.size + 1 >= elementaryEventsThreshold
            -> {
                val allUnsentElementaryEvents = previouslyUnsentElementaryEvents.toMutableList()
                allUnsentElementaryEvents.add(elementaryEvent)
                createAndSendSummaryNotificationResponse(allUnsentElementaryEvents)
            }
        }
    }

    fun createAndSendSingleNotificationResponse(elementaryEvent: ElementaryEventEntity) {
        val newNotificationEvent = NotificationEventEntity(
            companyId = elementaryEvent.companyId,
            elementaryEventType = ElementaryEventType.UploadEvent,
            creationTimestamp = Instant.now().toEpochMilli(),
            elementaryEvents = mutableListOf(elementaryEvent),
        )

        notificationEventRepository.saveAndFlush(newNotificationEvent)
        elementaryEvent.notificationEvent = newNotificationEvent
        elementaryEventRepository.saveAndFlush(elementaryEvent)
        sendSingleEmailMessageToQueue()
    }

    fun createAndSendSummaryNotificationResponse(elementaryEvents: List<ElementaryEventEntity>) {
        val newNotificationEvent = NotificationEventEntity(
            companyId = elementaryEvents.first().companyId,
            elementaryEventType = ElementaryEventType.UploadEvent,
            creationTimestamp = Instant.now().toEpochMilli(),
            elementaryEvents = elementaryEvents,
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
                elementaryEventType = ElementaryEventType.UploadEvent,
                companyId = UUID.fromString(dataMetaInformation.companyId),
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
    // TODO Emanuel: We should only get those which also match the expected elementary event type
    private fun getUnsentElementaryEventsForCompany(companyId: UUID): List<ElementaryEventEntity> {
        return elementaryEventRepository.findAllByCompanyIdAndNotificationEventIsNull(companyId)
    }

    /**
     * Function retrieves all elementaryEvents for a specific company which
     * have not been sent by a previous notification event
     * @param companyId
     * @return list of elementaryEvents
     */
    private fun isLastNotificationEventForCompanyOlderThanThreshold(companyId: UUID): Boolean {
        val lastNotificationEvent = notificationEventRepository.findNotificationEventByCompanyId(companyId)
            .maxByOrNull { it.creationTimestamp }
        return lastNotificationEvent == null ||
            Duration.between(Instant.ofEpochMilli(lastNotificationEvent.creationTimestamp), Instant.now())
                .toDays() > notificationThresholdDays
    }

    fun sendSingleEmailMessageToQueue() {}
    fun sendSummaryEmailMessageToQueue() {}
}
