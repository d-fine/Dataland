package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.entities.ElementaryEventEntity
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.ElementaryEventType
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.services.messaging.NotificationEmailSender
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
        val notificationEventRepository: NotificationEventRepository,
        val elementaryEventRepository: ElementaryEventRepository,
        val companyDataControllerApi: CompanyDataControllerApi,
        val notificationEmailSender: NotificationEmailSender,
        val companyRolesManager: CompanyRolesManager,
        @Value("\${dataland.community-manager.notification-threshold-days:30}")
        private val notificationThresholdDays: Int,
        @Value("\${dataland.community-manager.notification-elementaryevents-threshold:10}")
        private val elementaryEventsThreshold: Int,
    ) {
        private val logger = LoggerFactory.getLogger(this.javaClass)

        /**
         * Enum that contains all possible types of notification emails that might be triggered.
         * TODO
         */
        sealed class NotificationEmailType {
            data object Single : NotificationEmailType()

            data class Summary(
                val daysSinceLastNotificationEmail: Long?
            ) : NotificationEmailType()
        }

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
            val notificationEmailType =
                determineNotificationEmailType(latestElementaryEvent, unprocessedElementaryEvents)
                    ?: return

            logger.info(
                "Requirements for notification event are met. " +
                        "Creating notification event and sending notification emails. CorrelationId: $correlationId",
            )

            createNotificationEventAndReferenceIt(latestElementaryEvent, unprocessedElementaryEvents)

            if (!hasCompanyOwner(companyId) && !emailReceivers.isNullOrEmpty()) {
                notificationEmailSender.sendExternalAndInternalNotificationEmail(
                    notificationEmailType, latestElementaryEvent, unprocessedElementaryEvents,
                    companyInfo.companyName, emailReceivers, correlationId
                )
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
            val lastNotificationEvent = getLastNotificationEventOrNull(
                latestElementaryEvent.companyId,
                latestElementaryEvent.elementaryEventType,
            )
            val isLastNotificationEventOlderThanThreshold = isNotificationEventOlderThanThreshold(
                lastNotificationEvent
            )
            return when {
                isLastNotificationEventOlderThanThreshold && unprocessedElementaryEvents.size == 1 ->
                    NotificationEmailType.Single
                isLastNotificationEventOlderThanThreshold ||
                    unprocessedElementaryEvents.size >= elementaryEventsThreshold ->
                    NotificationEmailType.Summary(
                        lastNotificationEvent?.let(::getDaysPassedSinceNotificationEvent)
                    )
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
         * @param notificationEvent TODO
         * @return time passed in days, or null if there is no last notification event
         */
        fun getDaysPassedSinceNotificationEvent(
            notificationEvent: NotificationEventEntity,
        ): Long? =
            Duration.between(Instant.ofEpochMilli(notificationEvent.creationTimestamp), Instant.now()).toDays()

        /**
         * Checks if last notification event for company is older than threshold in days
         * param notificationEvent TODO
         * @return if last notification event for company is older than threshold in days
         */
        fun isNotificationEventOlderThanThreshold(
            notificationEvent: NotificationEventEntity?
        ): Boolean {
            return notificationEvent == null ||
                Duration
                    .between(Instant.ofEpochMilli(notificationEvent.creationTimestamp), Instant.now())
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
