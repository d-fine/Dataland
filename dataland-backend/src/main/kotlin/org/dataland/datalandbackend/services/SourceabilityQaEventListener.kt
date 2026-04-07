package org.dataland.datalandbackend.services

import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaNonSourceabilityAcceptedEventPayload
import org.dataland.datalandmessagequeueutils.messages.QaNonSourceabilityRejectedEventPayload
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.slf4j.LoggerFactory
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
 * Listener for QA non-sourceability decision events consumed by backend.
 */
@Component("SourceabilityQaEventListener")
class SourceabilityQaEventListener(
    @Autowired private val sourceabilityDataManager: SourceabilityDataManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Processes QA non-sourceability decision events from QA service.
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.QA_DECISION_QUEUE,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS, declare = "false"),
                key = [RoutingKeyNames.QA_DECISION_ACCEPTED, RoutingKeyNames.QA_DECISION_REJECTED],
            ),
        ],
    )
    fun processQaNonSourceabilityDecisionEvents(
        @Payload payload: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        MessageQueueUtils.rejectMessageOnException {
            when (type) {
                MessageType.QA_NON_SOURCEABILITY_ACCEPTED -> {
                    val eventPayload = MessageQueueUtils.readMessagePayload<QaNonSourceabilityAcceptedEventPayload>(payload)
                    sourceabilityDataManager.processQaNonSourceabilityAcceptedEvent(eventPayload, correlationId)
                    logger.info(
                        "Consumed ${MessageType.QA_NON_SOURCEABILITY_ACCEPTED} for nonSourceabilityId " +
                            "${eventPayload.nonSourceabilityId} (correlationId: $correlationId)",
                    )
                }

                MessageType.QA_NON_SOURCEABILITY_REJECTED -> {
                    val eventPayload = MessageQueueUtils.readMessagePayload<QaNonSourceabilityRejectedEventPayload>(payload)
                    sourceabilityDataManager.processQaNonSourceabilityRejectedEvent(eventPayload, correlationId)
                    logger.info(
                        "Consumed ${MessageType.QA_NON_SOURCEABILITY_REJECTED} for nonSourceabilityId " +
                            "${eventPayload.nonSourceabilityId} (correlationId: $correlationId)",
                    )
                }

                else -> {
                    throw MessageQueueRejectException("Unsupported QA non-sourceability message type $type")
                }
            }
        }
    }
}
