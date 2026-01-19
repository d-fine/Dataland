package org.dataland.datasourcingservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.RequestSetToProcessingMessage
import org.dataland.datalandmessagequeueutils.messages.RequestSetToWithdrawnMessage
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.RequestState
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service for sending RabbitMQ messages from the data sourcing service to other services.
 */
@Service("DataSourcingServiceMessageSender")
class DataSourcingServiceMessageSender(
    private val cloudEventMessageHandler: CloudEventMessageHandler,
    private val objectMapper: ObjectMapper,
) {
    /**
     * Sends a RabbitMQ message to the accounting service after a request had its state changed to Processing.
     */
    fun sendMessageToAccountingServiceOnRequestProcessing(
        dataSourcingEntity: DataSourcingEntity,
        requestEntity: RequestEntity,
    ) {
        val requestSetToProcessingMessage =
            RequestSetToProcessingMessage(
                triggeringUserId = requestEntity.userId.toString(),
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

    /**
     * Sends a RabbitMQ message to the accounting service after a request in state Processing or Processed
     * had its state changed to Withdrawn.
     */
    fun sendMessageToAccountingServiceOnRequestWithdrawn(
        dataSourcingEntity: DataSourcingEntity,
        requestEntity: RequestEntity,
    ) {
        val userIdsAssociatedRequestsForSameTriple =
            dataSourcingEntity.associatedRequests
                .filter {
                    it.state in
                        listOf(
                            RequestState.Processed,
                            RequestState.Processing,
                        ) &&
                        it.id != requestEntity.id
                }.map { it.userId }
        val requestSetToWithdrawnMessage =
            RequestSetToWithdrawnMessage(
                triggeringUserId = requestEntity.userId.toString(),
                dataSourcingId = dataSourcingEntity.dataSourcingId.toString(),
                requestedCompanyId = requestEntity.companyId.toString(),
                requestedReportingPeriod = requestEntity.reportingPeriod,
                requestedFramework = requestEntity.dataType,
                userIdsAssociatedRequestsForSameTriple = userIdsAssociatedRequestsForSameTriple.map { it.toString() },
            )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(requestSetToWithdrawnMessage),
            type = MessageType.REQUEST_SET_TO_WITHDRAWN,
            correlationId = UUID.randomUUID().toString(),
            exchange = ExchangeName.DATA_SOURCING_SERVICE_REQUEST_EVENTS,
            routingKey = RoutingKeyNames.REQUEST_WITHDRAWN,
        )
    }
}
