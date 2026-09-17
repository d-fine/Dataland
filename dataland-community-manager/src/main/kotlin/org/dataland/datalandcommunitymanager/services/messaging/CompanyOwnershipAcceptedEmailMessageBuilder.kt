package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.CompanyOwnershipClaimApprovedEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A class that manages generating email messages when company ownership requests have been accepted
 */

@Component
class CompanyOwnershipAcceptedEmailMessageBuilder(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    //@Autowired private val dataRequestQueryManager: DataRequestQueryManager,
) {
    /**
     * Function that generates the message object for company ownership request acceptance mails
     * @param newCompanyOwnerId of the user that has received company ownership
     * @param datalandCompanyId of the company that the user has received company ownership for
     * @param companyName of the company that the user has received company ownership for
     * @param correlationId of the current user process
     */
    fun buildCompanyOwnershipAcceptanceExternalEmailAndSendCEMessage(
        newCompanyOwnerId: String,
        datalandCompanyId: String,
        companyName: String,
        correlationId: String,
    ) {
        val emailData =
            CompanyOwnershipClaimApprovedEmailContent(
                companyId = datalandCompanyId,
                companyName = companyName
            )
        val message =
            EmailMessage(
                emailData,
                listOf(EmailRecipient.UserId(newCompanyOwnerId)),
                emptyList(), emptyList(),
            )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SEND_EMAIL,
            correlationId,
            ExchangeName.SEND_EMAIL,
            RoutingKeyNames.EMAIL,
        )
    }
}
