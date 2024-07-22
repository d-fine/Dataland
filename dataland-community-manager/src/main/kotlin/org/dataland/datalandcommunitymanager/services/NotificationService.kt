package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.dataland.datalandcommunitymanager.model.FrameworkAndYear
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
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
import java.util.*

@Service("NotificationService")
class NotificationService
@Suppress("LongParameterList")
constructor(
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var messageUtils: MessageQueueUtils,
    @Autowired var elementaryEventRepository: ElementaryEventRepository,
    @Autowired var notificationEventRepository: NotificationEventRepository,
    @Autowired var metaDataControllerApi: MetaDataControllerApi,
    @Autowired var companyDataControllerApi: CompanyDataControllerApi,
    @Autowired var objectMapper: ObjectMapper,
    @Value("\${dataland.community-manager.notification-threshold-days:30}")
    private val notificationThresholdDays: Int,
    @Value("\${dataland.community-manager.notification-elementaryevents-threshold:10}")
    private val elementaryEventsThreshold: Int,
    @Value("\${dataland.community-manager.proxy-primary-url:local-dev.dataland.com}")
    private val proxyPrimaryUrl: String,
) {

    /* TODO Emanuel: Ich glaube wir müssen noch einen RabbitListener für die private data queue einführen. Der
    schaut dann halt auf die andere queue (für private data), ruft aber dieselben Funktionen hier auf wie in
    "processPublicDataUploadEvent"
    */

    /**
     * Method that listens to the storage_queue and, creates and persists new elementaryEvents, and creates and persists
     * a new single or summary notification event if necessary i.e. specific trigger requirements are met
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
        val companyMetadata = metaDataControllerApi.getDataMetaInfo(dataId)
        val companyIdOfUpload = UUID.fromString(companyMetadata.companyId)

        // TODO Emanuel: Get only those which match the elementary event type (data upload)
        val previouslyUnsentElementaryEvents = getUnsentElementaryEventsForCompany(companyIdOfUpload)

        val elementaryEvent = createAndSaveElementaryEvent(companyMetadata)

        // TODO Emanuel: The "decision-function" needs to be in a separate function so I can test it better in unit test
        when {
            isLastNotificationEventForCompanyOlderThanThreshold(companyIdOfUpload) &&
                previouslyUnsentElementaryEvents.isEmpty()
            -> {
                createAndSendSingleNotificationResponse(elementaryEvent)
                sendSingleEmailMessageToQueue(elementaryEvent, correlationId)
            }

            isLastNotificationEventForCompanyOlderThanThreshold(companyIdOfUpload) &&
                previouslyUnsentElementaryEvents.isNotEmpty()
            -> {
                val allUnsentElementaryEvents = previouslyUnsentElementaryEvents.toMutableList()
                allUnsentElementaryEvents.add(elementaryEvent)
                createAndSendSummaryNotificationResponse(allUnsentElementaryEvents)
                sendSummaryEmailMessageToQueue(allUnsentElementaryEvents, correlationId)
            }
            !isLastNotificationEventForCompanyOlderThanThreshold(companyIdOfUpload) &&
                previouslyUnsentElementaryEvents.size + 1 >= elementaryEventsThreshold
            -> {
                val allUnsentElementaryEvents = previouslyUnsentElementaryEvents.toMutableList()
                allUnsentElementaryEvents.add(elementaryEvent)
                createAndSendSummaryNotificationResponse(allUnsentElementaryEvents)
                sendSummaryEmailMessageToQueue(allUnsentElementaryEvents, correlationId)
            }
        }
    }

    /**
     * Creates and persists a new elementaryEvent for dataUpload and returns this element
     */
    private fun createAndSaveElementaryEvent(companyMetadata: DataMetaInformation): ElementaryEventEntity {
        return elementaryEventRepository.saveAndFlush(
            ElementaryEventEntity(
                elementaryEventType = ElementaryEventType.UploadEvent,
                companyId = UUID.fromString(companyMetadata.companyId),
                framework = companyMetadata.dataType,
                reportingPeriod = companyMetadata.reportingPeriod,
                creationTimestamp = Instant.now().toEpochMilli(),
                notificationEvent = null,
            ),
        )
    }

    /**
     * Creates and persists a new notification event and sends internal single mail message to queue.
     * The elementaryEvent to be sent as part of this notification is updated accordingly.
     */
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
    }

    /**
     * Creates and persists a new notification event and sends internal summary mail message to queue.
     * The elementaryEvents to be sent as part of this notification are updated accordingly.
     */
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
    }

    /**
     * Sends singleNotification Template Email Message to Queue
     */
    fun sendSingleEmailMessageToQueue(
        elementaryEvent: ElementaryEventEntity,
        correlationId: String,
    ) {
        val companyInfo = companyDataControllerApi.getCompanyInfo(elementaryEvent.companyId.toString())
        val properties = mapOf(
            "companyName" to companyInfo.companyName,
            "companyId" to elementaryEvent.companyId.toString(),
            "framework" to elementaryEvent.framework.toString(),
            "year" to elementaryEvent.reportingPeriod,
            "baseUrl" to proxyPrimaryUrl,
        )

        companyInfo.companyContactDetails?.forEach {
                contactAddress ->
            val message = TemplateEmailMessage(
                emailTemplateType = TemplateEmailMessage.Type.SingleNotification,
                receiver = TemplateEmailMessage.EmailAddressEmailRecipient(contactAddress),
                properties = properties,
            )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                objectMapper.writeValueAsString(message),
                MessageType.SendTemplateEmail,
                correlationId,
                ExchangeName.SendEmail,
                RoutingKeyNames.templateEmail,
            )
        }
    }

    fun sendSummaryEmailMessageToQueue(
        elementaryEvents: List<ElementaryEventEntity>,
        correlationId: String,
    ) {
        val companyInfo = companyDataControllerApi.getCompanyInfo(elementaryEvents.first().companyId.toString())
        val properties = mapOf(
            "companyName" to companyInfo.companyName,
            "companyId" to elementaryEvents.first().companyId.toString(),
            // "frameworks" to getListOfFrameWorksAndYearsFromElementaryEvents(elementaryEvents),
            "baseUrl" to proxyPrimaryUrl,
            "numberOfDays" to getTimePassedSinceLastNotificationEvent(elementaryEvents.first().companyId).toString(),
        )

        companyInfo.companyContactDetails?.forEach {
                contactAddress ->
            val message = TemplateEmailMessage(
                emailTemplateType = TemplateEmailMessage.Type.SingleNotification,
                receiver = TemplateEmailMessage.EmailAddressEmailRecipient(contactAddress),
                properties = properties,
            )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                objectMapper.writeValueAsString(message),
                MessageType.SendTemplateEmail,
                correlationId,
                ExchangeName.SendEmail,
                RoutingKeyNames.templateEmail,
            )
        }
    }

    /**
     * Retrieves all elementaryEvents for a specific company which have not been sent by a previous notification event
     * @param companyId
     * @return list of elementaryEvents
     */
    // TODO Emanuel: We should only get those which also match the expected elementary event type
    private fun getUnsentElementaryEventsForCompany(companyId: UUID): List<ElementaryEventEntity> {
        return elementaryEventRepository.findAllByCompanyIdAndNotificationEventIsNull(companyId)
    }

    /**
     * Gets last notification event for a specific company
     * @param companyId
     * @return last notificationEvent (null if no previous notification event for this company exists)
     */
    private fun getLastNotificationEventOrNull(companyId: UUID): NotificationEventEntity? {
        return notificationEventRepository.findNotificationEventByCompanyId(companyId)
            .maxByOrNull { it.creationTimestamp }
    }

    /**
     * Gets time passed in days since last notification event for a specific company
     * @param companyId
     * @return time passed in days as Int
     */
    private fun getTimePassedSinceLastNotificationEvent(companyId: UUID): Long {
        val lastNotificationEvent = getLastNotificationEventOrNull(companyId)
        return if (lastNotificationEvent == null) {
            elementaryEventsThreshold.toLong()
        } else {
            Duration.between(Instant.ofEpochMilli(lastNotificationEvent.creationTimestamp), Instant.now()).toDays()
        }
    }

    /**
     * Checks if last notification event for company is older than threshold in days
     * @param companyId
     * @return if last notification event for company is older than threshold in days
     */
    private fun isLastNotificationEventForCompanyOlderThanThreshold(companyId: UUID): Boolean {
        val lastNotificationEvent = getLastNotificationEventOrNull(companyId)
        return lastNotificationEvent == null ||
            Duration.between(Instant.ofEpochMilli(lastNotificationEvent.creationTimestamp), Instant.now())
                .toDays() > notificationThresholdDays
    }

    private fun getListOfFrameWorksAndYearsFromElementaryEvents(elementaryEvents: List<ElementaryEventEntity>): List<FrameworkAndYear> {
        val frameworkAndYears = mutableListOf<FrameworkAndYear>()
        for (elementaryEvent in elementaryEvents) {
            frameworkAndYears.add(FrameworkAndYear(elementaryEvent.framework, elementaryEvent.reportingPeriod))
        }
        return frameworkAndYears
    }
}
