package org.dataland.datalanduserservice.service

import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.data.PortfolioUpdatePayload
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.InternalEmailContentTable
import org.dataland.datalandmessagequeueutils.messages.email.Value
import org.dataland.datalanduserservice.model.SupportRequestData
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Publisher Service for Portfolio Updates (Companies and Monitoring status).
 */
@Service
class MessageQueuePublisherService
    @Autowired
    constructor(
        private val cloudEventMessageHandler: CloudEventMessageHandler,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)
        private val objectMapper = defaultObjectMapper

        /**
         * Method to publish a portfolio update payload
         * @param portfolioId The ID of the portfolio from which the request is initiated
         * @param companyIds The set of requested company IDs
         * @param monitoredFrameworks The set of requested frameworks
         * @param reportingPeriods The set of requested reporting periods
         * @param correlationId The correlation ID of the request initiating the event
         */
        fun publishPortfolioUpdate(
            portfolioId: String,
            companyIds: Set<String>,
            monitoredFrameworks: Set<String>,
            reportingPeriods: Set<String>,
            correlationId: String,
        ) {
            val datalandAuthentication = DatalandAuthentication.fromContext()
            val userId = datalandAuthentication.userId
            val userRoles = datalandAuthentication.roles.map { it.name }

            logger
                .info(
                    "Publish the update payload of portfolio with ID '$portfolioId'. Correlation ID: '$correlationId'.",
                )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                body =
                    objectMapper.writeValueAsString(
                        PortfolioUpdatePayload(
                            portfolioId,
                            companyIds,
                            monitoredFrameworks,
                            reportingPeriods,
                            userId,
                            userRoles,
                        ),
                    ),
                type = MessageType.PORTFOLIO_UPDATE,
                correlationId = correlationId,
                exchange = ExchangeName.USER_SERVICE_EVENTS,
                routingKey = RoutingKeyNames.PORTFOLIO_UPDATE,
            )
        }

        /**
         * Method to publish a support request
         * @param supportRequestData Contains topic and message of the request
         */
        fun publishSupportRequest(supportRequestData: SupportRequestData) {
            val datalandJwtAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication
            val correlationId = UUID.randomUUID().toString()

            val internalEmailContentTable =
                InternalEmailContentTable(
                    "User Portfolio Support Request",
                    "A user has submitted a request for support.",
                    listOf(
                        "User" to Value.Text(datalandJwtAuthentication.userId),
                        "E-Mail" to Value.Text(datalandJwtAuthentication.username),
                        "First Name" to Value.Text(datalandJwtAuthentication.firstName),
                        "Last Name" to Value.Text(datalandJwtAuthentication.lastName),
                        "Topic" to Value.Text(supportRequestData.topic),
                        "Message" to Value.Text(supportRequestData.message),
                    ),
                )
            val message =
                EmailMessage(
                    internalEmailContentTable,
                    listOf(EmailRecipient.Internal),
                    listOf(EmailRecipient.InternalCc),
                    emptyList(),
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
