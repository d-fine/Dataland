package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.MultipleDatasetsUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.SingleDatasetUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Service that handles creation of notification events and sending notifications to interested parties
 * in case of elementary events
 */
@Service("NotificationService")
class NotificationService
    @Suppress("LongParameterList")
    @Autowired
    constructor(
        val cloudEventMessageHandler: CloudEventMessageHandler,
        val notificationEventRepository: NotificationEventRepository,
        val elementaryEventRepository: ElementaryEventRepository,
        val companyDataControllerApi: CompanyDataControllerApi,
        val companyRolesManager: CompanyRolesManager,
        val objectMapper: ObjectMapper,
        @Value("\${dataland.community-manager.notification-threshold-days:30}")
        private val notificationThresholdDays: Int,
        @Value("\${dataland.community-manager.notification-elementaryevents-threshold:10}")
        private val elementaryEventsThreshold: Int,
    ) {
        private val logger = LoggerFactory.getLogger(this.javaClass)

        /**
         * Enum that contains all possible types of notification emails that might be triggered.
         */
        enum class NotificationEmailType { Single, Summary }

        /**
         * Checks if notification event shall be created or not.
         * If yes, it creates it and sends a message to the queue to trigger notification emails.
         */
        @Transactional
        fun notifyOfElementaryEvents(
            latestElementaryEvent: ElementaryEventEntity,
            correlationId: String,
        ) {
            val companyId = latestElementaryEvent.companyId
            val unprocessedElementaryEvents =
                elementaryEventRepository.findAllByCompanyIdAndElementaryEventTypeAndNotificationEventIsNull(
                    companyId,
                    latestElementaryEvent.elementaryEventType,
                )
            val companyInfo = companyDataControllerApi.getCompanyInfo(companyId.toString())
            val emailReceivers = companyInfo.companyContactDetails
            determineNotificationEmailType(latestElementaryEvent, unprocessedElementaryEvents)
                ?.let { notificationEmailType ->
                    logger.info(
                        "Requirements for notification event are met. " +
                            "Creating notification event and sending notification emails. CorrelationId: $correlationId",
                    )

                    createNotificationEventAndReferenceIt(latestElementaryEvent, unprocessedElementaryEvents)

                    if (!hasCompanyOwner(companyId) && !emailReceivers.isNullOrEmpty()) {
                        val typedEmailData =
                            buildEmailData(
                                companyInfo.companyName,
                                notificationEmailType,
                                latestElementaryEvent,
                                unprocessedElementaryEvents,
                                )
                        sendEmailMessagesToQueue(typedEmailData, emailReceivers, correlationId)
                        // TODO fix this later
                        //NotificationServiceUtils.sendInternalMessageToQueue(
                        //    objectMapper, cloudEventMessageHandler, emailReceivers,
                        //    notificationEmailType, emailProperties, correlationId,
                        //)
                    }
                }
        }

        /**
         * Checks if the requirements for creating a notification event are met.
         * If yes, it returns the type of notification mail that shall be sent.
         * Else it simply returns null.
         */
        fun determineNotificationEmailType(
            latestElementaryEvent: ElementaryEventEntity,
            unprocessedElementaryEvents: List<ElementaryEventEntity>,
        ): NotificationEmailType? {
            val isLastNotificationEventOlderThanThreshold =
                isLastNotificationEventOlderThanThreshold(
                    latestElementaryEvent.companyId,
                    latestElementaryEvent.elementaryEventType,
                )

            return when {
                isLastNotificationEventOlderThanThreshold && unprocessedElementaryEvents.size == 1 ->
                    NotificationEmailType.Single
                isLastNotificationEventOlderThanThreshold ||
                    unprocessedElementaryEvents.size >= elementaryEventsThreshold ->
                    NotificationEmailType.Summary
                else -> null
            }
        }

        /**
         * Creates and persists a new notification event and also puts the reference to this newly created notification
         * event into the associated elementary events.
         */
        fun createNotificationEventAndReferenceIt(
            latestElementaryEvent: ElementaryEventEntity,
            unprocessedElementaryEvents: List<ElementaryEventEntity>,
        ) {
            val notificationEvent =
                NotificationEventEntity(
                    companyId = latestElementaryEvent.companyId,
                    elementaryEventType = latestElementaryEvent.elementaryEventType,
                    creationTimestamp = Instant.now().toEpochMilli(),
                )
            val savedNotificationEvent = notificationEventRepository.saveAndFlush(notificationEvent)
            unprocessedElementaryEvents.forEach {
                it.notificationEvent = savedNotificationEvent
                elementaryEventRepository.saveAndFlush(it)
            }
        }

        /**
         * TODO
         */
        fun buildEmailData(
                companyName: String,
                notificationEmailType: NotificationEmailType,
                latestElementaryEvent: ElementaryEventEntity,
                unprocessedElementaryEvents: List<ElementaryEventEntity>,
            ) : TypedEmailData =
                when (notificationEmailType) {
                    NotificationEmailType.Single ->
                        SingleDatasetUploadedEngagement(
                            companyName = companyName,
                            companyId = latestElementaryEvent.companyId.toString(),
                            dataType = readableFrameworkNameMapping[latestElementaryEvent.framework] ?: "",
                            reportingPeriod = latestElementaryEvent.reportingPeriod
                        )
                    NotificationEmailType.Summary -> {
                        val frameworkData = unprocessedElementaryEvents
                            .groupBy { it.framework }
                            .map { (framework, events) ->
                                MultipleDatasetsUploadedEngagement.FrameworkData(
                                    readableFrameworkNameMapping[framework] ?: "", events.map { it.reportingPeriod }
                                )
                            }

                        MultipleDatasetsUploadedEngagement(
                            companyName = companyName,
                            companyId = latestElementaryEvent.companyId.toString(),
                            frameworkData = frameworkData,
                            numberOfDays = getDaysPassedSinceLastNotificationEvent(
                                latestElementaryEvent.companyId, latestElementaryEvent.elementaryEventType,
                            )
                        )
                    }
                }

        /**
         * Sends messages to queue in order to make the email service send mails to all receivers.
         */
        fun sendEmailMessagesToQueue(
            typedEmailData: TypedEmailData,
            emailReceivers: List<String>,
            correlationId: String,
        ) {
            emailReceivers.forEach { emailAddress ->
                val message = EmailMessage(typedEmailData, listOf(EmailRecipient.EmailAddress(emailAddress)), emptyList(), emptyList())
                cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                    objectMapper.writeValueAsString(message),
                    MessageType.SEND_EMAIL,
                    correlationId,
                    ExchangeName.SEND_EMAIL,
                    RoutingKeyNames.EMAIL,
                )
            }
        }

        /**
         * Gets last notification event for a specific company and elementary event type
         * @param companyId for which a notification event might have happened
         * @param elementaryEventType of the elementary events for which the notification event was created
         * @return last notificationEvent (null if no previous notification event for this company exists)
         */
        fun getLastNotificationEventOrNull(
            companyId: UUID,
            elementaryEventType: ElementaryEventType,
        ): NotificationEventEntity? =
            notificationEventRepository
                .findNotificationEventByCompanyIdAndElementaryEventType(
                    companyId,
                    elementaryEventType,
                ).maxByOrNull { it.creationTimestamp }

        /**
         * Gets days passed since last notification event for a specific company. If there was no last notification
         * event, it returns "null".
         * @param companyId for which a notification event might have happened
         * @param elementaryEventType of the elementary events for which the notification event was created
         * @return time passed in days, or null if there is no last notification event
         */
        fun getDaysPassedSinceLastNotificationEvent(
            companyId: UUID,
            elementaryEventType: ElementaryEventType,
        ): Long? =
            getLastNotificationEventOrNull(companyId, elementaryEventType)?.let { lastNotificationEvent ->
                Duration.between(Instant.ofEpochMilli(lastNotificationEvent.creationTimestamp), Instant.now()).toDays()
            }

        /**
         * Checks if last notification event for company is older than threshold in days
         * @param companyId
         * @return if last notification event for company is older than threshold in days
         */
        fun isLastNotificationEventOlderThanThreshold(
            companyId: UUID,
            elementaryEventType: ElementaryEventType,
        ): Boolean {
            val lastNotificationEvent = getLastNotificationEventOrNull(companyId, elementaryEventType)
            return lastNotificationEvent == null ||
                Duration
                    .between(Instant.ofEpochMilli(lastNotificationEvent.creationTimestamp), Instant.now())
                    .toDays() > notificationThresholdDays
        }

        /**
         * checks if company has owner (if company has owner, notifications are created but not sent)
         */
        fun hasCompanyOwner(companyId: UUID): Boolean {
            val companyOwner =
                companyRolesManager.getCompanyRoleAssignmentsByParameters(
                    companyRole = CompanyRole.CompanyOwner,
                    companyId = companyId.toString(),
                    userId = null,
                )

            return companyOwner.isNotEmpty()
        }
    }
