package org.dataland.datalandcommunitymanager.converters

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandcommunitymanager.events.NotificationEventType

/**
 * Converts NotificationEventType entries in Entities to database values and vice versa
 */
@Converter
class NotificationEventTypeAttributeConverter : AttributeConverter<NotificationEventType?, String> {
    override fun convertToDatabaseColumn(notificationEventType: NotificationEventType?): String? = notificationEventType?.toString()

    override fun convertToEntityAttribute(notificationEventTypeAsString: String?): NotificationEventType? =
        if (notificationEventTypeAsString.isNullOrEmpty()) {
            null
        } else {
            NotificationEventType
                .valueOf(notificationEventTypeAsString)
        }
}
