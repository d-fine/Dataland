package org.dataland.datalanduserservice.service

import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.data.PortfolioUpdatePayload
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Publisher Service for Portfolio Updates (Companies and Monitoring status) relevant for automatic Bulk Data Requests.
 */
@Service
class MessageQueuePublisher
    @Autowired
    constructor(
        private val cloudEventMessageHandler: CloudEventMessageHandler,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)
        private val objectMapper = defaultObjectMapper
        val datalandAuthentication = DatalandAuthentication.fromContext()
        val userId = datalandAuthentication.userId
        val userRoles = datalandAuthentication.roles

        /**
         * Method to publish a portfolio update payload
         * @param portfolioId The ID of the portfolio from which the request is initiated
         * @param companyIds The set of requested company IDs
         * @param monitoredFrameworks The set of requested frameworks
         * @param reportingPeriods The set of requested reporting periods
         * @param correlationId The correlation ID of the request initiating the event
         * @param userId The ID of the user that initiates the request
         */
        fun publishPortfolioUpdate(
            portfolioId: String,
            companyIds: Set<String>,
            monitoredFrameworks: Set<String>,
            reportingPeriods: Set<String>,
            correlationId: String,
        ) {
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
    }
