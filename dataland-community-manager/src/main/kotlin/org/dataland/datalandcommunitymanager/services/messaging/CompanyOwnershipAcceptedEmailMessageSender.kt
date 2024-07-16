package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.services.DataRequestQueryManager
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
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
        val properties = mapOf(
            "companyId" to datalandCompanyId,
            "companyName" to companyName,
            "numberOfOpenDataRequestsForCompany" to getNumberOfOpenDataRequestsForCompany(datalandCompanyId).toString(),
        )
        val message = TemplateEmailMessage(
            TemplateEmailMessage.Type.SuccessfullyClaimedOwnership,
            TemplateEmailMessage.UserIdEmailRecipient(newCompanyOwnerId),
            properties,
        )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(message),
            MessageType.SendTemplateEmail,
            correlationId,
            ExchangeName.SendEmail,
            RoutingKeyNames.templateEmail,
        )
    }

    /**
     * Function that counts the number of data requests that a company has open
     * @param datalandCompanyId of the company to count the open data requests for
     * @return the number of opened data requests
     */
    fun getNumberOfOpenDataRequestsForCompany(datalandCompanyId: String): Int {
        return dataRequestQueryManager.getAggregatedDataRequests(
            identifierValue = datalandCompanyId,
            dataTypes = null,
            reportingPeriod = null,
            status = RequestStatus.Open,
        ).filter { it.count > 0 }.size
    }
}
