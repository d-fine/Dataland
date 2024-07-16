package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
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
        val properties = mapOf(
            "User" to userAuthentication.userDescription,
            "E-Mail" to userAuthentication.username,
            "Company (Dataland ID)" to datalandCompanyId,
            "First Name" to userAuthentication.firstName,
            "Last Name" to userAuthentication.lastName,
            "Company Name" to companyName,
            "Comment" to comment,
        )
        val message = InternalEmailMessage(
            "Dataland Company Ownership Request",
            "A company ownership request has been submitted",
            "Company Ownership Request",
            properties,
        )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SendInternalEmail,
            correlationId,
            ExchangeName.SendEmail,
            RoutingKeyNames.internalEmail,
        )
    }
}
