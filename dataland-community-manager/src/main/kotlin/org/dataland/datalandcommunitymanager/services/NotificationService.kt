package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.services.messaging.NotificationEmailSender
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
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
        val companyDataControllerApi: CompanyDataControllerApi,
        val notificationEmailSender: NotificationEmailSender,
    ) {
        private val logger = LoggerFactory.getLogger(this.javaClass)

        /**
         * Checks if there are unprocessed notification events.
         * If yes, sends Investor Relationship and/or Data Request Summary notification emails.
         */
        @Scheduled(cron = "0 0 0 ? * SUN")
        fun scheduledWeeklyEmailSending() {
            // Investor Relationship Emails
            val unprocessedInvestorRelationshipEvents =
                notificationEventRepository
                    .findAllByCompanyIdAndNotificationEventTypeAndIsProcessedFalse(
                        companyId = UUID.randomUUID(), // careful: do we really want to search by companyId here?
                        notificationEventType = NotificationEventType.InvestorRelationshipsEvent,
                    )
            if (unprocessedInvestorRelationshipEvents.isNotEmpty()) {
                processInvestorRelationshipEvents(unprocessedInvestorRelationshipEvents)
                markEventsAsProcessed(unprocessedInvestorRelationshipEvents)
            }

            // Data Request Summary Emails
            val dataRequestSummaryEventTypes =
                listOf(NotificationEventType.AvailableEvent, NotificationEventType.UpdatedEvent, NotificationEventType.NonSourceableEvent)
            val unprocessedDataRequestSummaryEvents =
                notificationEventRepository
                    .findAllByUserIdAndNotificationEventTypeInAndIsProcessedFalse(dataRequestSummaryEventTypes)
            if (unprocessedDataRequestSummaryEvents.isNotEmpty()) {
                processDataRequestSummaryEvents(unprocessedDataRequestSummaryEvents)
                markEventsAsProcessed(unprocessedDataRequestSummaryEvents)
            }
        }

        /**
         * Processes investor relationship events and sends emails to appropriate recipients.
         */
        private fun processInvestorRelationshipEvents(events: List<NotificationEventEntity>) {
            val eventsGroupedByCompany = events.groupBy { it.companyId }
            eventsGroupedByCompany.forEach { (companyId, companyEvents) ->
                val companyInfo = companyDataControllerApi.getCompanyInfo(companyId.toString())
                val emailReceivers = companyInfo.companyContactDetails
                val correlationId = UUID.randomUUID().toString() // toto: generate oder get

                if (!hasCompanyOwner(companyId) && !emailReceivers.isNullOrEmpty()) {
                    logger.info(
                        "Requirements for Investor Relationship notification are met. " +
                            "Sending notification emails. CorrelationId: $correlationId",
                    )
                    notificationEmailSender.sendExternalAndInternalInvestorRelationshipSummaryEmail(
                        unprocessedEvents = companyEvents,
                        companyId = companyId,
                        receiver = emailReceivers,
                        correlationId = correlationId,
                    )
                }
            }
        }

        /**
         * Processes data request summary events and sends emails to appropriate recipients.
         */
        private fun processDataRequestSummaryEvents(events: List<NotificationEventEntity>) {
//            toto
            if (events.isEmpty()) return // line added so events is used and detekt does not complain
            notificationEmailSender.sendDataRequestSummaryEmail() // toto
        }

        /**
         * Marks all given events as processed by setting isProcessed to true.
         */
        private fun markEventsAsProcessed(events: List<NotificationEventEntity>) {
            events.forEach { event ->
                event.isProcessed = true
            }
            notificationEventRepository.saveAll(events) // Batch save to update processed status
            logger.info("Marked ${events.size} events as processed.")
        }

        /*
        /**
         * Gets last notification event for a specific company and elementary event type
         * @param companyId for which a notification event might have happened
         * @param elementaryEventType of the elementary events for which the notification event was created
         * @return last notificationEvent (null if no previous notification event for this company exists)
         */
        fun getLastNotificationEventOrNull(
            companyId: UUID,
            elementaryEventType: NotificationEventType,
        ): NotificationEventEntity? =
            notificationEventRepository
                .findNotificationEventByCompanyIdAndElementaryEventType(
                    companyId,
                    elementaryEventType,
                ).maxByOrNull { it.creationTimestamp }

        /**
         * Gets days passed since a notification event for a specific company.
         * @param notificationEvent The notification event
         * @return time passed in days
         */
        fun getDaysPassedSinceNotificationEvent(notificationEvent: NotificationEventEntity): Long? =
            Duration.between(Instant.ofEpochMilli(notificationEvent.creationTimestamp), Instant.now()).toDays()

        /**
         * Checks if a notification event is older than threshold in days. If no notification event is specified this
         * function returns always null.
         * @param notificationEvent The notification event or null
         * @return if last notification event for company is older than threshold in days
         */
        fun isNotificationEventOlderThanThreshold(notificationEvent: NotificationEventEntity?): Boolean =
            notificationEvent == null ||
                Duration
                    .between(Instant.ofEpochMilli(notificationEvent.creationTimestamp), Instant.now())
                    .toDays() > notificationThresholdDays

        /**

         * Checks if company has owner (if company has owner, notifications are set to processed but not sent)
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
         */
    }
