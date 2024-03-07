package org.dataland.datalandbackend.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

/**
 * A class that manages generating emails messages for bulk and single data requests
 */
@Component
class EmailMessageSender(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(EmailMessageSender::class.java)

    /**
     * Function that generates the message object for single data request mails
     */
    fun sendSingleDataRequestInternalMessage(
        userId: String,
        datalandCompanyId: String,
        message: InternalEmailMessage,
    ) {
        val correlationId = UUID.randomUUID().toString()
        logger.info(
            "User with Id $userId has submitted a single data request for company with" +
                " Id $datalandCompanyId and correlationId $correlationId",
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
