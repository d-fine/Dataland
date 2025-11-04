package org.dataland.datasourcingservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.RequestSetToProcessingMessage
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.entities.RequestEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service for sending RabbitMQ messages from the data sourcing service to other services.
 */
@Service("DataSourcingServiceMessageSender")
class DataSourcingServiceMessageSender
    @Autowired
    constructor(
        private val cloudEventMessageHandler: CloudEventMessageHandler,
        private val objectMapper: ObjectMapper,
    ) {
        /**
         * Sends a RabbitMQ message to the accounting service after a request had its state changed to Processing.
         */
        fun sendMessageToAccountingServiceOnRequestProcessing(
            billedCompanyId: String,
            dataSourcingEntity: DataSourcingEntity,
            requestEntity: RequestEntity,
        ) {
            val requestSetToProcessingMessage =
                RequestSetToProcessingMessage(
                    billedCompanyId = billedCompanyId,
                    dataSourcingId = dataSourcingEntity.dataSourcingId.toString(),
                    requestedCompanyId = requestEntity.companyId.toString(),
                    requestedReportingPeriod = requestEntity.reportingPeriod,
                    requestedFramework = requestEntity.dataType,
                )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                body = objectMapper.writeValueAsString(requestSetToProcessingMessage),
                type = MessageType.REQUEST_SET_TO_PROCESSING,
                correlationId = UUID.randomUUID().toString(),
                exchange = ExchangeName.DATA_SOURCING_SERVICE_REQUEST_EVENTS,
                routingKey = RoutingKeyNames.REQUEST_PATCH,
            )
        }
    }
