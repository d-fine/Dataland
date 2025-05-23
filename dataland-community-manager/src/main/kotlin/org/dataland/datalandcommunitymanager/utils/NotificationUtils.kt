package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Class holding utility functions used by the DataRequestSummary and the InvestorRelationShip NotificationService
 */
@Service
class NotificationUtils(
    @Autowired private val notificationEventRepository: NotificationEventRepository,
) {
    /**
     * Processes notification events and sends emails to appropriate recipients.
     * @param events List of unprocessed notification events.
     */
    fun markEventsAsProcessed(events: List<NotificationEventEntity>) {
        events.forEach { event ->
            event.isProcessed = true
        }
        notificationEventRepository.saveAll(events)
    }
}
