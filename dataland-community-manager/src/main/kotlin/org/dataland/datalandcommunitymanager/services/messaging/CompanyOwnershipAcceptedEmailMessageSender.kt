package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandcommunitymanager.services.DataRequestQueryManager
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.CompanyOwnershipClaimApproved
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A class that manages generating email messages when company ownership requests have been accepted
 */

@Component
class CompanyOwnershipAcceptedEmailMessageSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val dataRequestQueryManager: DataRequestQueryManager,
) {
    /**
     * Function that generates the message object for company ownership request acceptance mails
     * @param newCompanyOwnerId of the user that has received company ownership
     * @param datalandCompanyId of the company that the user has received company ownership for
     * @param companyName of the company that the user has received company ownership for
     * @param correlationId of the current user process
     */
    fun sendCompanyOwnershipAcceptanceExternalEmailMessage(
        newCompanyOwnerId: String,
        datalandCompanyId: String,
        companyName: String,
        correlationId: String,
    ) {
        val emailData =
            CompanyOwnershipClaimApproved(
                companyId = datalandCompanyId,
                companyName = companyName,
                numberOfOpenDataRequestsForCompany = getNumberOfOpenDataRequestsForCompany(datalandCompanyId),
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

    /**
     * Function that counts the number of data requests that a company has open
     * @param datalandCompanyId of the company to count the open data requests for
     * @return the number of opened data requests
     */
    fun getNumberOfOpenDataRequestsForCompany(datalandCompanyId: String): Int =
        dataRequestQueryManager
            .getAggregatedOpenDataRequests(
                identifierValue = datalandCompanyId,
                dataTypes = null,
                reportingPeriod = null,
            ).filter { it.count > 0 }
            .size
}
