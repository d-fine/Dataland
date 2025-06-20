package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequest
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.data.PortfolioUpdatePayload
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.keycloakAdapter.auth.DatalandInternalAuthentication
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

/**
 * Listener Service for Portfolio Updates (Companies and Monitoring status) relevant for automatic Bulk Data Requests.
 */
@Component
class PortfolioUpdateListenerService
    @Autowired
    constructor(
        private val requestManager: BulkDataRequestManager,
        private val objectMapper: ObjectMapper,
        private val requestEmailManager: RequestEmailManager,
    ) {
        /**
         * Creates Bulk Data Requests from Portfolio Update Payloads.
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            QueueNames.USER_SERVICE_PORTFOLIO_UPDATE,
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.USER_SERVICE_EVENTS, declare = "false"),
                    key = [RoutingKeyNames.PORTFOLIO_UPDATE],
                ),
            ],
        )
        fun processPortfolioMonitoringEvent(
            message: Message,
            @Payload payload: String,
            @Header(MessageHeaderKey.TYPE) messageType: String,
        ) {
            val receivedRoutingKey = message.messageProperties.receivedRoutingKey
            MessageQueueUtils.rejectMessageOnException {
                when (receivedRoutingKey) {
                    RoutingKeyNames.PORTFOLIO_UPDATE -> {
                        MessageQueueUtils.validateMessageType(messageType, MessageType.PORTFOLIO_UPDATE)
                        val messagePayload =
                            MessageQueueUtils.readMessagePayload<PortfolioUpdatePayload>(payload, objectMapper)
                        // MessageQueueUtils.validateDataId(messagePayload.portfolioId)

                        val companyIdentifiers = messagePayload.companyIds
                        val reportingPeriods = messagePayload.reportingPeriods
                        val userId = messagePayload.userId
                        val datalandInternalAuthentication = DatalandInternalAuthentication(userId)

                        val dataTypes: Set<DataTypeEnum> =
                            messagePayload.monitoredFrameworks
                                .mapNotNull { key -> dataTypeEnumMap[key] }
                                .toSet()

                        requestManager.processBulkDataRequest(
                            BulkDataRequest(
                                companyIdentifiers,
                                dataTypes,
                                reportingPeriods,
                                false,
                            ),
                            datalandInternalAuthentication,
                        )
                    }

                    else -> throw MessageQueueRejectException(
                        "Routing Key '$receivedRoutingKey' unknown. " +
                            "Expected Routing Key ${RoutingKeyNames.DATASET_UPLOAD} or ${RoutingKeyNames.METAINFORMATION_PATCH}",
                    )
                }
            }
        }

        private val dataTypeEnumMap =
            mapOf(
                "sfdr" to DataTypeEnum.sfdr,
                "eutaxonomy-financials" to DataTypeEnum.eutaxonomyMinusFinancials,
                "eutaxonomy-non-financials" to DataTypeEnum.eutaxonomyMinusNonMinusFinancials,
                "nuclear-and-gas" to DataTypeEnum.nuclearMinusAndMinusGas,
            )
    }
