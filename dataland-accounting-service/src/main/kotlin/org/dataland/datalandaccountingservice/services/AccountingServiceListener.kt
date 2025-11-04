package org.dataland.datalandaccountingservice.services

import org.dataland.datalandaccountingservice.entities.BilledRequestEntity
import org.dataland.datalandaccountingservice.model.BilledRequestEntityId
import org.dataland.datalandaccountingservice.repositories.BilledRequestRepository
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.RequestSetToProcessingMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Listener class handling RabbitMQ messages sent to the accounting service.
 */
@Service("AccountingServiceListener")
class AccountingServiceListener(
    @Autowired private val billedRequestRepository: BilledRequestRepository,
) {
    private val logger = LoggerFactory.getLogger(AccountingServiceListener::class.java)

    /**
     * Creates a billed request when a request is patched to the "Processing" state and the company to bill does not
     * already have a billed request for the given data sourcing ID.
     * @param payload the message payload
     * @param type the message type
     * @param correlationId the correlation ID of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.ACCOUNTING_SERVICE_REQUEST_PROCESSING,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.DATA_SOURCING_SERVICE_REQUEST_EVENTS),
                key = [RoutingKeyNames.REQUEST_PATCH],
            ),
        ],
    )
    @Transactional
    fun createBilledRequestOnRequestPatchToStateProcessing(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.REQUEST_SET_TO_PROCESSING)
        val requestSetToProcessingMessage = MessageQueueUtils.readMessagePayload<RequestSetToProcessingMessage>(payload)

        logger.info(
            "Received a message to create a billed request in order to bill company ${requestSetToProcessingMessage.billedCompanyId} " +
                "under data sourcing ID ${requestSetToProcessingMessage.dataSourcingId}. " +
                "Requested company ID is ${requestSetToProcessingMessage.requestedCompanyId}, " +
                "reporting period is ${requestSetToProcessingMessage.requestedReportingPeriod}, " +
                "and requested framework is ${requestSetToProcessingMessage.requestedFramework}. " +
                "Correlation ID: $correlationId.",
        )

        MessageQueueUtils.rejectMessageOnException {
            val existingBilledRequest =
                billedRequestRepository.findByIdOrNull(
                    BilledRequestEntityId(
                        billedCompanyId = UUID.fromString(requestSetToProcessingMessage.billedCompanyId),
                        dataSourcingId = UUID.fromString(requestSetToProcessingMessage.dataSourcingId),
                    ),
                )

            if (existingBilledRequest != null) {
                logger.info(
                    "Billed request for billed company ID ${requestSetToProcessingMessage.billedCompanyId} " +
                        "and data sourcing ID ${requestSetToProcessingMessage.dataSourcingId} already exists. " +
                        "Skipping creation. Correlation ID: $correlationId.",
                )
                return@rejectMessageOnException
            }

            billedRequestRepository.save(
                BilledRequestEntity(
                    billedCompanyId = UUID.fromString(requestSetToProcessingMessage.billedCompanyId),
                    dataSourcingId = UUID.fromString(requestSetToProcessingMessage.dataSourcingId),
                    requestedCompanyId = UUID.fromString(requestSetToProcessingMessage.requestedCompanyId),
                    requestedReportingPeriod = requestSetToProcessingMessage.requestedReportingPeriod,
                    requestedFramework = requestSetToProcessingMessage.requestedFramework,
                ),
            )
        }
    }
}
