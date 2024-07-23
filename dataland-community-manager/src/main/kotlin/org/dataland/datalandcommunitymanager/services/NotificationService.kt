package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
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
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.json.JSONObject
import org.slf4j.LoggerFactory
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
    fun processPrivateDataUploadEvent(
        @Payload payload: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.PrivateDataReceived)

        val dataId = JSONObject(payload).getString("dataId")
        validateDataId(dataId)

        val actionType = JSONObject(payload).getString("actionType")
        validateActionType(ActionType.StorePrivateDataAndDocuments, actionType)

        logger.info("Processing elementary event: Request for storage of private framework data.")

        createNotificationEventForDataUploadsIfRequriementsMet(dataId, correlationId)
    }

    /**
     * Method that listens to public data storage requests and, creates and persists new elementaryEvents,
     * and creates and persists a new single or summary notification event if specific trigger requirements are met
     * @param payload the content of the message
     * @param correlationId the correlation ID of the current user process
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
    fun processPublicDataUploadEvent(
        @Payload payload: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.PublicDataReceived)
        val dataId = JSONObject(payload).getString("dataId")
        validateDataId(dataId)

        val actionType = JSONObject(payload).getString("actionType")
        validateActionType(ActionType.StorePublicData, actionType)

        logger.info("Processing elementary event: Request for storage of public framework data.")

        createNotificationEventForDataUploadsIfRequriementsMet(dataId, correlationId)
    }

    fun createNotificationEventForDataUploadsIfRequriementsMet(dataIdOfUpload: String, correlationId: String) {
        val dataMetaInfo = metaDataControllerApi.getDataMetaInfo(dataIdOfUpload) // TODO Emanuel: problem => lets discuss
        val companyId = UUID.fromString(dataMetaInfo.companyId)

        val newlyCreatedElementaryEvent = createAndSaveElementaryEvent(dataMetaInfo, ElementaryEventType.UploadEvent)
        val previouslyUnprocessedElementaryEvents =
            getUnprocessedElementaryEventsForCompany(companyId, ElementaryEventType.UploadEvent)

        val notificationEmailScope =
            determineNotificationEmailScope(companyId, previouslyUnprocessedElementaryEvents)

        if (notificationEmailScope != null) {
            createNotificationEventAndSendEmailMessageToQueue(
                notificationEmailScope,
                newlyCreatedElementaryEvent,
                previouslyUnprocessedElementaryEvents,
                correlationId,
            )
        }
    }

    enum class NotificationEmailScope { Single, Summary } // TODO maybe we should put it in separate file

    fun determineNotificationEmailScope(
        companyIdOfDataUpload: UUID,
        previouslyUnsentElementaryEvents: List<ElementaryEventEntity>,
    ): NotificationEmailScope? {
        val isLastNotificationEventOlderThanThreshold =
            isLastNotificationEventOlderThanThreshold(companyIdOfDataUpload, ElementaryEventType.UploadEvent)

        return when {
            isLastNotificationEventOlderThanThreshold && previouslyUnsentElementaryEvents.isEmpty() ->
                NotificationEmailScope.Single

            isLastNotificationEventOlderThanThreshold ||
                previouslyUnsentElementaryEvents.size + 1 >= elementaryEventsThreshold ->
                NotificationEmailScope.Summary

            else -> null
        }
    }

    private fun createNotificationEventAndSendEmailMessageToQueue(
        scope: NotificationEmailScope,
        elementaryEvent: ElementaryEventEntity,
        previouslyUnprocessedElementaryEvents: List<ElementaryEventEntity>,
        correlationId: String,
    ) {
        val allUnprocessedElementaryEvents = previouslyUnprocessedElementaryEvents + elementaryEvent
        createNotificationEvent(allUnprocessedElementaryEvents)

        when (scope) {
            NotificationEmailScope.Single -> {
                sendSingleEmailMessageToQueue(elementaryEvent, correlationId)
            }
            NotificationEmailScope.Summary -> {
                sendSummaryEmailMessageToQueue(allUnprocessedElementaryEvents, correlationId)
            }
        }
    }

    /**
     * Creates and persists a new elementaryEvent for dataUpload and returns this element
     */
    private fun createAndSaveElementaryEvent(dataMetaInfo: DataMetaInformation, elementaryEventType: ElementaryEventType): ElementaryEventEntity {
        return elementaryEventRepository.saveAndFlush( // TODO simple "save" might be sufficient => research
            ElementaryEventEntity(
                elementaryEventType = elementaryEventType,
                companyId = UUID.fromString(dataMetaInfo.companyId),
                framework = dataMetaInfo.dataType,
                reportingPeriod = dataMetaInfo.reportingPeriod,
                creationTimestamp = Instant.now().toEpochMilli(),
                notificationEvent = null,
            ),
        )
    }

    /**
     * Creates and persists a new notification event.
     */
    fun createNotificationEvent(elementaryEvents: List<ElementaryEventEntity>) {
        val newNotificationEvent = NotificationEventEntity(
            companyId = elementaryEvents.first().companyId,
            elementaryEventType = elementaryEvents.first().elementaryEventType,
            creationTimestamp = Instant.now().toEpochMilli(),
            elementaryEvents = elementaryEvents,
        )
        notificationEventRepository.saveAndFlush(newNotificationEvent)
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
        val firstElementaryEvent = elementaryEvents.first()
        val properties = mapOf(
            "companyName" to companyInfo.companyName,
            "companyId" to firstElementaryEvent.companyId.toString(),
            "frameworks" to createFrameworkAndYearStringFromElementaryEvents(elementaryEvents),
            "baseUrl" to proxyPrimaryUrl,
            "numberOfDays" to getTimePassedSinceLastNotificationEvent(firstElementaryEvent.companyId, firstElementaryEvent.elementaryEventType).toString(),
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
     * @param companyId TODO
     * @return list of elementaryEvents
     */
    // TODO Emanuel: We should only get those which also match the expected elementary event type
    private fun getUnprocessedElementaryEventsForCompany(companyId: UUID, elementaryEventType: ElementaryEventType): List<ElementaryEventEntity> {
        return elementaryEventRepository.findAllByCompanyIdAndElementaryEventTypeAndNotificationEventIsNull(
            companyId,
            elementaryEventType,
        )
    }

    /**
     * Gets last notification event for a specific company
     * @param companyId TODO
     * @param elementaryEventType TODO
     * @return last notificationEvent (null if no previous notification event for this company exists)
     */
    private fun getLastNotificationEventOrNull(
        companyId: UUID,
        elementaryEventType: ElementaryEventType,
    ): NotificationEventEntity? {
        return notificationEventRepository.findNotificationEventByCompanyIdAndElementaryEventType(companyId, elementaryEventType)
            .maxByOrNull { it.creationTimestamp }
    }

    /**
     * Gets time passed in days since last notification event for a specific company
     * @param companyId TODO
     * @return time passed in days as Int
     */
    private fun getTimePassedSinceLastNotificationEvent(companyId: UUID, elementaryEventType: ElementaryEventType): Long {
        val lastNotificationEvent = getLastNotificationEventOrNull(companyId, elementaryEventType)
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
    private fun isLastNotificationEventOlderThanThreshold(
        companyId: UUID,
        elementaryEventType: ElementaryEventType,
    ): Boolean {
        val lastNotificationEvent = getLastNotificationEventOrNull(companyId, elementaryEventType)
        return lastNotificationEvent == null ||
            Duration.between(Instant.ofEpochMilli(lastNotificationEvent.creationTimestamp), Instant.now())
                .toDays() > notificationThresholdDays
    }

    private fun createFrameworkAndYearStringFromElementaryEvents(elementaryEvents: List<ElementaryEventEntity>): String {
        val frameworkAndYears = elementaryEvents.groupBy(
            keySelector = { it.framework },
            valueTransform = { it.reportingPeriod },
        ).mapValues { (_, years) -> years.sorted() }
        return frameworkAndYears.entries.joinToString(", ") {
                (framework, years) ->
            "$framework: ${years.joinToString(" ") }"
        }
    }

    private fun validateActionType(expectedActionType: String, actualActionType: String) {
        if (actualActionType != expectedActionType) {
            throw MessageQueueRejectException("Expected action type $expectedActionType, but was $actualActionType.")
        }
    }

    private fun validateDataId(dataId: String) {
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty.")
        }
    }
}
