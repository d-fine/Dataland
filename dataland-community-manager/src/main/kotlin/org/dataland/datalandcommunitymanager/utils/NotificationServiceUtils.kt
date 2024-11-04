package org.dataland.datalandcommunitymanager.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandcommunitymanager.services.NotificationService.NotificationEmailType
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage

/**
 * A utils class for the NotificationService class, containing methods that are only needed because of the missing
 * functionality of the email service.
 */
object NotificationServiceUtils {
    /**
     * Sends an internal message about the ir email to the queue.
     */
    fun sendInternalMessageToQueue(
        objectMapper: ObjectMapper,
        cloudEventMessageHandler: CloudEventMessageHandler,
        notificationEmailType: NotificationEmailType,
        emailProperties: Map<String, String?>,
        correlationId: String,
    ) {
        val keyMap =
            mapOf(
                "baseUrl" to "Environment",
                "companyId" to "Company (Dataland ID)",
                "companyName" to "Company Name",
                "year" to "Reporting periods",
                "numberOfDays" to "Number of days",
                "framework" to "Framework",
                "frameworks" to "Frameworks",
            )

        val internalEmailProperties = (
            emailProperties.mapKeys { keyMap[it.key] ?: "Unknown key" } +
                mapOf("Notification Email Type" to notificationEmailType.toString())
        )

        val message =
            InternalEmailMessage(
                "Dataland Notification Email has been send",
                "An IR Notification Email has been send",
                "IR Notification Email has been send",
                internalEmailProperties,
            )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SEND_INTERNAL_EMAIL,
            correlationId,
            ExchangeName.SEND_EMAIL,
            RoutingKeyNames.INTERNAL_EMAIL,
        )
    }
}
