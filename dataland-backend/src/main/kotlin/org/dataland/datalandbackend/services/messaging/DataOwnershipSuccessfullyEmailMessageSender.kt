package org.dataland.datalandbackend.services.messaging

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
 * A class that manages generating emails messages when data ownership request is accepted
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 */

@Component
class DataOwnershipSuccessfullyEmailMessageSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
) {
    /**
     * Function that generates the message object for data ownership request acceptance mails
     * @param userAuthentication the DatalandAuthentication of the user who becomes a data owner
     * @param datalandCompanyId identifier of the company in dataland
     * @param comment the personal message from the user process
     * @param correlationId the correlation ID of the current user process
     */
    fun sendDataOwnershipAcceptanceInternalEmailMessage(
        userAuthentication: DatalandJwtAuthentication,
        datalandCompanyId: String,
        companyName: String,
        comment: String?,
        numberOfOpenDataRequestsForCompany: Int,
        correlationId: String,
    ) {
        val properties = mapOf(
            "User" to userAuthentication.userDescription,
            "Company (Dataland ID)" to datalandCompanyId,
            "Company Name" to companyName,
            "Comment" to comment,
            "Number of data requests open" to numberOfOpenDataRequestsForCompany.toString(),
        )
        val message = InternalEmailMessage(
            "Dataland Data Ownership Request Acceptance",
            "A data ownership request has been accepted",
            "Data Ownership Request Acceptance",
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
