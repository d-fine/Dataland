package org.dataland.datasourcingservice.services

import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.logging.CorrelationLogging
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingPatch
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Handles the QA decision events for non-sourceability emitted by the QA service.
 *
 * On QA_ACCEPTED: transitions the matching data sourcing object to [DataSourcingState.NonSourceable].
 * On QA_REJECTED: keeps the data sourcing object in [DataSourcingState.NonSourceableVerification]
 *   to signal that the entry requires manual follow-up.
 */
@Service
class NonSourceabilityQaDecisionListener(
    @Autowired private val dataSourcingQueryManager: DataSourcingQueryManager,
    @Autowired private val dataSourcingManager: DataSourcingManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Handles an incoming non-sourceability QA decision event (accepted or rejected) from the QA service.
     * Validates the message type, deserializes the payload, and delegates to [processQaDecisionEvent].
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.DATA_SOURCING_SERVICE_NON_SOURCEABILITY_QA_DECISION,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.QA_SERVICE_NON_SOURCEABILITY_DECISIONS, declare = "false"),
                key = [RoutingKeyNames.NON_SOURCEABILITY_QA_ACCEPTED, RoutingKeyNames.NON_SOURCEABILITY_QA_REJECTED],
            ),
        ],
    )
    fun onNonSourceabilityQaDecision(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) messageType: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.rejectMessageOnException {
            if (messageType != MessageType.NON_SOURCEABILITY_QA_ACCEPTED &&
                messageType != MessageType.NON_SOURCEABILITY_QA_REJECTED
            ) {
                throw MessageQueueRejectException(
                    "Unexpected message type \"$messageType\" in non-sourceability QA decision listener",
                )
            }
            val event = MessageQueueUtils.readMessagePayload<NonSourceabilityLifecycleEvent>(payload)
            CorrelationLogging.withNonSourceabilityContext(correlationId, event.nonSourceabilityId) {
                processQaDecisionEvent(event, correlationId)
            }
        }
    }

    @Transactional
    internal fun processQaDecisionEvent(
        event: NonSourceabilityLifecycleEvent,
        correlationId: String,
    ) {
        when (event.eventType) {
            NonSourceabilityEventType.NON_SOURCEABILITY_QA_ACCEPTED ->
                transitionToNonSourceable(event, correlationId)
            NonSourceabilityEventType.NON_SOURCEABILITY_QA_REJECTED ->
                keepInVerification(event, correlationId)
            else -> {
                logger.error(
                    "Unexpected event type ${event.eventType} in NonSourceabilityQaDecisionListener " +
                        "(correlationId=$correlationId). Discarding.",
                )
                throw MessageQueueRejectException("Unexpected event type ${event.eventType} in QA decision listener")
            }
        }
    }

    private fun transitionToNonSourceable(
        event: NonSourceabilityLifecycleEvent,
        correlationId: String,
    ) {
        val sourcing = findSourcingForEvent(event, correlationId) ?: return
        if (sourcing.state == DataSourcingState.NonSourceable) {
            logger.info("Idempotent skip: already NonSourceable for nonSourceabilityId=${event.nonSourceabilityId}")
            return
        }
        dataSourcingManager.patchDataSourcingEntityById(
            UUID.fromString(sourcing.dataSourcingId),
            DataSourcingPatch(state = DataSourcingState.NonSourceable),
        )
        logger.info(
            "Transitioned dataSourcingId=${sourcing.dataSourcingId} to NonSourceable via QA accepted " +
                "(correlationId=$correlationId, nonSourceabilityId=${event.nonSourceabilityId})",
        )
    }

    private fun keepInVerification(
        event: NonSourceabilityLifecycleEvent,
        correlationId: String,
    ) {
        val sourcing = findSourcingForEvent(event, correlationId)
        if (sourcing == null) return
        if (sourcing.state != DataSourcingState.NonSourceableVerification) {
            logger.warn(
                "QA rejected for nonSourceabilityId=${event.nonSourceabilityId} but data sourcing is in " +
                    "state ${sourcing.state} instead of NonSourceableVerification. No transition performed " +
                    "(correlationId=$correlationId).",
            )
            return
        }
        logger.info(
            "QA rejected: dataSourcingId=${sourcing.dataSourcingId} stays in NonSourceableVerification " +
                "(correlationId=$correlationId, nonSourceabilityId=${event.nonSourceabilityId})",
        )
    }

    private fun findSourcingForEvent(
        event: NonSourceabilityLifecycleEvent,
        correlationId: String,
    ) = dataSourcingQueryManager
        .searchDataSourcings(
            companyId = UUID.fromString(event.companyId),
            dataType = event.dataType,
            reportingPeriod = event.reportingPeriod,
            state = null,
            chunkSize = 1,
            chunkIndex = 0,
        ).firstOrNull()
        .also { sourcing ->
            if (sourcing == null) {
                logger.info(
                    "No data sourcing object found for companyId=${event.companyId}, dataType=${event.dataType}, " +
                        "reportingPeriod=${event.reportingPeriod} (correlationId=$correlationId, " +
                        "nonSourceabilityId=${event.nonSourceabilityId}). Skipping.",
                )
            }
        }
}
