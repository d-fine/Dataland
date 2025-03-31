import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.services.DataRequestSummaryNotificationService
import org.dataland.datalandcommunitymanager.services.InvestorRelationshipNotificationService
import org.dataland.datalandcommunitymanager.utils.NotificationUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class NotificationScheduler
    @Autowired
    constructor(
        private val notificationEventRepository: NotificationEventRepository,
        private val notificationUtils: NotificationUtils,
        private val investorRelationshipNotificationService: InvestorRelationshipNotificationService,
        private val dataRequestSummaryNotificationService: DataRequestSummaryNotificationService,
    ) {
        /**
         * Scheduled method to send emails for unprocessed notification events.
         * Runs every Sunday at midnight.
         */
        @Scheduled(cron = "0 0 0 ? * SUN")
        fun scheduledWeeklyEmailSending() {
            // Find and process Investor Relationship Events
            val unprocessedInvestorRelationshipEvents =
                notificationEventRepository.findAllByNotificationEventTypesAndIsProcessedFalse(
                    listOf(NotificationEventType.InvestorRelationshipsEvent),
                )
            if (unprocessedInvestorRelationshipEvents.isNotEmpty()) {
                investorRelationshipNotificationService.processNotificationEvents(unprocessedInvestorRelationshipEvents)
                notificationUtils.markEventsAsProcessed(unprocessedInvestorRelationshipEvents)
            }

            // Find and process Data Request Summary Events
            val dataRequestSummaryEventTypes =
                listOf(
                    NotificationEventType.AvailableEvent,
                    NotificationEventType.UpdatedEvent,
                    NotificationEventType.NonSourceableEvent,
                )

            val unprocessedDataRequestSummaryEvents =
                notificationEventRepository.findAllByNotificationEventTypesAndIsProcessedFalse(dataRequestSummaryEventTypes)

            if (unprocessedDataRequestSummaryEvents.isNotEmpty()) {
                dataRequestSummaryNotificationService.processNotificationEvents(unprocessedDataRequestSummaryEvents)
                notificationUtils.markEventsAsProcessed(unprocessedDataRequestSummaryEvents)
            }
        }
    }
