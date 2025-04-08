package org.dataland.datalandcommunitymanager.services.messaging

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.services.DataRequestSummaryNotificationService
import org.dataland.datalandcommunitymanager.services.InvestorRelationshipsNotificationService
import org.dataland.datalandcommunitymanager.utils.NotificationUtils
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

/**
 * Scheduler class for notifications sent out by the Community Manager.
 */
@Service
class NotificationScheduler
    @Autowired
    constructor(
        private val notificationEventRepository: NotificationEventRepository,
        private val notificationUtils: NotificationUtils,
        private val investorRelationshipsNotificationService: InvestorRelationshipsNotificationService,
        private val dataRequestSummaryNotificationService: DataRequestSummaryNotificationService,
    ) {
        private val logger = LoggerFactory.getLogger(this.javaClass)

        /**
         * Scheduled method to send emails for unprocessed notification events.
         * Runs every Sunday at midnight.
         */
        @Scheduled(cron = "*/3 * * * * ?")
        fun scheduledWeeklyEmailSending() {
            logger.info("scheduledWeeklyEmailSending") // toto: remove
            processNotificationEvents(
                "Investor Relationships",
                listOf(NotificationEventType.InvestorRelationshipsEvent),
                investorRelationshipsNotificationService::processNotificationEvents,
            )

            processNotificationEvents(
                "Data Request Summary",
                listOf(
                    NotificationEventType.AvailableEvent,
                    NotificationEventType.UpdatedEvent,
                    NotificationEventType.NonSourceableEvent,
                ),
                dataRequestSummaryNotificationService::processNotificationEvents,
            )
        }

        /**
         * Processes and marks notification events as processed for a given purpose.
         * @param emailPurpose A string representing the purpose of the email being sent, used for log messages.
         * @param eventTypes A list of NotificationEventType to filter the events that need processing.
         * @param processFunction A function reference to handle the processing of events.
         */
        private fun processNotificationEvents(
            emailPurpose: String,
            eventTypes: List<NotificationEventType>,
            processFunction: (List<NotificationEventEntity>) -> Unit,
        ) {
            logger.info("Starting processNotificationEvents for: $emailPurpose") // toto: remove
            val unprocessedEvents =
                notificationEventRepository.findAllByNotificationEventTypesAndIsProcessedFalse(eventTypes)
            logger.info("Found ${unprocessedEvents.size} unprocessed events for types: $eventTypes") // toto: remove
            if (unprocessedEvents.isNotEmpty()) {
                try {
                    logger.info("Processing events: ${unprocessedEvents.map { it }}") // toto: remove
                    processFunction(unprocessedEvents)
                    logger.info("Processed events successfully for: $emailPurpose") // toto: remove
                    notificationUtils.markEventsAsProcessed(unprocessedEvents)
                    logger.info("Marked events as processed for: $emailPurpose") // toto: remove
                } catch (unsupportedOperationException: UnsupportedOperationException) {
                    logError(emailPurpose, unsupportedOperationException)
                } catch (clientException: ClientException) {
                    logError(emailPurpose, clientException)
                } catch (amqpException: AmqpException) {
                    logError(emailPurpose, amqpException)
                }
            }
        }

        /**
         * Logs error message with a formatted template.
         * @param purpose The purpose of the notification event.
         * @param exception The exception thrown.
         */
        private fun logError(
            purpose: String,
            exception: Exception,
        ) {
            val exceptionName = exception::class.simpleName ?: "UnknownException"
            logger.error(
                "Failed to process $purpose notification events due to $exceptionName Exception: ${exception.message}",
                exception,
            )
        }
    }
