package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.InternalEmailContentTable
import org.dataland.datalandmessagequeueutils.messages.email.Value
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A class that manages generating email messages for company ownership requests
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 */
@Component
class CompanyOwnershipRequestedEmailMessageSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
) {
    /**
     * Function that generates the message object for company ownership request mails
     * @param userAuthentication of the user who wants to become a company owner
     * @param datalandCompanyId of the company that the user wants to become a company owner for
     * @param comment from the user in association with the request
     * @param correlationId of the current user process
     */
    fun sendCompanyOwnershipInternalEmailMessage(
        userAuthentication: DatalandJwtAuthentication,
        datalandCompanyId: String,
        companyName: String,
        comment: String?,
        correlationId: String,
    ) {
        val internalEmailContentTable =
            InternalEmailContentTable(
                "Dataland Company Ownership Request",
                "A company ownership request has been submitted",
                "Company Ownership Request",
                listOf(
                    "User" to Value.Text(userAuthentication.userDescription),
                    "E-Mail" to Value.Text(userAuthentication.username),
                    "Company (Dataland ID)" to Value.Text(datalandCompanyId),
                    "First Name" to Value.Text(userAuthentication.firstName),
                    "Last Name" to Value.Text(userAuthentication.lastName),
                    "Company Name" to Value.Text(companyName),
                    "Comment" to Value.Text(comment ?: "empty comment"),
                ),
            )
        val message =
            EmailMessage(internalEmailContentTable, listOf(EmailRecipient.Internal), listOf(EmailRecipient.InternalCc), emptyList())
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SEND_EMAIL,
            correlationId,
            ExchangeName.SEND_EMAIL,
            RoutingKeyNames.EMAIL,
        )
    }
}
