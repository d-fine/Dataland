package org.dataland.datalandbackend.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.services.KeycloakUserControllerApiService
import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.RequestStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A class that manages generating emails messages when data ownership request is accepted
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 */

@Component
class DataOwnershipSuccessfullyEmailMessageSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val keycloakUserControllerApiService: KeycloakUserControllerApiService,
    @Autowired private val requestControllerApi: RequestControllerApi,
) {
    /**
     * Function that generates the message object for data ownership request acceptance mails
     * @param newDataOwnerId the id of the user promoted to data owner
     * @param datalandCompanyId identifier of the company in dataland
     * @param companyName the name of the company
     * @param correlationId the correlation ID of the current user process
     */
    fun sendDataOwnershipAcceptanceInternalEmailMessage(
        newDataOwnerId: String,
        datalandCompanyId: String,
        companyName: String,
        correlationId: String,
    ) {
        val newDataOwnerEmail = keycloakUserControllerApiService.getEmailAddress(newDataOwnerId)
        val properties = mapOf(
            "companyId" to datalandCompanyId,
            "companyName" to companyName,
            "numberOfOpenDataRequestsForCompany" to getNumberOfOpenDataRequestsForCompany(datalandCompanyId).toString(),
        )
        val message = TemplateEmailMessage(
            TemplateEmailMessage.Type.ClaimedOwershipSucessfully,
            newDataOwnerEmail,
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
     * @param datalandCompanyId identifier of the company in dataland
     * @return the number of opened data requests
     */
    private fun getNumberOfOpenDataRequestsForCompany(datalandCompanyId: String): Int {
        return requestControllerApi.getAggregatedDataRequests(
            identifierValue = datalandCompanyId, status = RequestStatus.Open,
        ).filter { it.count > 0 }.size
    }
}
