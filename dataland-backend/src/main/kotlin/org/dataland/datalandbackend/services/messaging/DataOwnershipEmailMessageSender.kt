package org.dataland.datalandbackend.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A class that manages generating emails messages for bulk and single data requests
 */
@Component
class DataOwnershipEmailMessageSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyRepository: StoredCompanyRepository
) {
    /**
     * Function that generates the message object for single data request mails
     */
    fun sendDataOwnershipInternalEmailMessage(
        userAuthentication: DatalandJwtAuthentication,
        datalandCompanyId: String,
        comment: String?,
        correlationId: String,
    ) {
        val companyName = companyRepository.findById(datalandCompanyId).get().companyName
        val properties = mapOf(
            "User" to userAuthentication.userDescription,
            "Company (Dataland ID)" to datalandCompanyId,
            "Company Name" to companyName,
            "Comment" to comment,
        )
        val message = InternalEmailMessage(
            "Dataland Data Ownership Request",
            "A data ownership request has been submitted",
            "Data Ownership Request",
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
