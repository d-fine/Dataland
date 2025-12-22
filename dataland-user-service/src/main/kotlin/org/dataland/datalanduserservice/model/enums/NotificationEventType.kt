package org.dataland.datalanduserservice.model.enums

/**
 * This enum class contains all notification events that are being processed to create daily/weekly/monthly
 * notifications
 */
enum class NotificationEventType {
    AvailableEvent,
    UpdatedEvent,
    NonSourceableEvent,
}
