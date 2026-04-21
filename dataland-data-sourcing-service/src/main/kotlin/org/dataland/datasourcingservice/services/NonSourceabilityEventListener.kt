package org.dataland.datasourcingservice.services

import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.logging.CorrelationLogging
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
 * Handles non-sourceability submission events from the backend.
 *
 * A single queue binds to [RoutingKeyNames.NON_SOURCEABILITY_SUBMISSION] and dispatches on
 * messageType:
 *   - [MessageType.NON_SOURCEABILITY_CREATED]       → [DataSourcingState.NonSourceableVerification]
 *   - [MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED] → [DataSourcingState.NonSourceable]
 *
 * Fail-fast validation (SOR-002): events with malformed or blank nonSourceabilityId are
 * discarded with an error log and a [MessageQueueRejectException].
 */
@Service
class NonSourceabilityEventListener(
    @Autowired private val dataSourcingQueryManager: DataSourcingQueryManager,
    @Autowired private val dataSourcingManager: DataSourcingManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Handles non-sourceability submission events from the backend and transitions the matching
     * data sourcing object to [DataSourcingState.NonSourceableVerification] (standard QA path) or
     * [DataSourcingState.NonSourceable] (admin bypass path) based on the event type.
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.DATA_SOURCING_SERVICE_NON_SOURCEABILITY_SUBMISSION,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.BACKEND_DATA_NONSOURCEABLE, declare = "false"),
                key = [RoutingKeyNames.NON_SOURCEABILITY_SUBMISSION],
            ),
        ],
    )
    fun onNonSourceabilitySubmission(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) messageType: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.rejectMessageOnException {
            if (messageType != MessageType.NON_SOURCEABILITY_CREATED &&
                messageType != MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED
            ) {
                throw MessageQueueRejectException(
                    "Unexpected message type \"$messageType\" in NonSourceabilityEventListener",
                )
            }
            val event = MessageQueueUtils.readMessagePayload<NonSourceabilityLifecycleEvent>(payload)
            CorrelationLogging.withNonSourceabilityContext(correlationId, event.nonSourceabilityId) {
                validateNonSourceabilityId(event.nonSourceabilityId)
                when (messageType) {
                    MessageType.NON_SOURCEABILITY_CREATED ->
                        transitionToVerification(event, correlationId)
                    MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED ->
                        transitionToNonSourceable(event, correlationId)
                }
            }
        }
    }

    /**
     * Handles a non-sourceability QA decision event from the QA service. Transitions the matching data
     * sourcing object to [DataSourcingState.NonSourceable] if accepted, or to [DataSourcingState.DocumentSourcingDone]
     * if rejected.
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
                key = [RoutingKeyNames.NON_SOURCEABILITY_QA_DECISION],
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
                when (messageType) {
                    MessageType.NON_SOURCEABILITY_QA_ACCEPTED ->
                        transitionToNonSourceable(event, correlationId)
                    MessageType.NON_SOURCEABILITY_QA_REJECTED ->
                        transitionToDocumentSourcingDone(event, correlationId)
                }
            }
        }
    }

    @Transactional
    internal fun transitionToVerification(
        event: NonSourceabilityLifecycleEvent,
        correlationId: String,
    ) {
        val sourcing = findSourcingForEvent(event, correlationId) ?: return
        if (sourcing.state == DataSourcingState.NonSourceableVerification) {
            logger.info("Idempotent skip: already in NonSourceableVerification for nonSourceabilityId=${event.nonSourceabilityId}")
            return
        }
        dataSourcingManager.patchDataSourcingEntityById(
            UUID.fromString(sourcing.dataSourcingId),
            DataSourcingPatch(state = DataSourcingState.NonSourceableVerification),
        )
        logger.info(
            "Transitioned dataSourcingId=${sourcing.dataSourcingId} to NonSourceableVerification " +
                "(correlationId=$correlationId, nonSourceabilityId=${event.nonSourceabilityId})",
        )
    }

    @Transactional
    internal fun transitionToNonSourceable(
        event: NonSourceabilityLifecycleEvent,
        correlationId: String,
    ) {
        val sourcing = findSourcingForEvent(event, correlationId) ?: return
        if (sourcing.state == DataSourcingState.NonSourceable) {
            logger.info("Idempotent skip: already in NonSourceable for nonSourceabilityId=${event.nonSourceabilityId}")
            return
        }
        dataSourcingManager.patchDataSourcingEntityById(
            UUID.fromString(sourcing.dataSourcingId),
            DataSourcingPatch(state = DataSourcingState.NonSourceable),
        )
        logger.info(
            "Transitioned dataSourcingId=${sourcing.dataSourcingId} to NonSourceable " +
                "(correlationId=$correlationId, nonSourceabilityId=${event.nonSourceabilityId})",
        )
    }

    @Transactional
    internal fun transitionToDocumentSourcingDone(
        event: NonSourceabilityLifecycleEvent,
        correlationId: String,
    ) {
        val sourcing = findSourcingForEvent(event, correlationId) ?: return
        if (sourcing.state == DataSourcingState.DocumentSourcingDone) {
            logger.info("Idempotent skip: already in DocumentSourcingDone for nonSourceabilityId=${event.nonSourceabilityId}")
            return
        }
        dataSourcingManager.patchDataSourcingEntityById(
            UUID.fromString(sourcing.dataSourcingId),
            DataSourcingPatch(state = DataSourcingState.DocumentSourcingDone),
        )
        logger.info(
            "Transitioned dataSourcingId=${sourcing.dataSourcingId} to DocumentSourcingDone " +
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
                        "nonSourceabilityId=${event.nonSourceabilityId}). Skipping state transition.",
                )
            }
        }

    /**
     * Validates that [nonSourceabilityId] is a non-blank valid UUID.
     * Exposed as internal fun to allow direct unit-test invocation.
     * @throws [MessageQueueRejectException] if validation fails.
     */
    internal fun validateNonSourceabilityId(nonSourceabilityId: String) {
        if (nonSourceabilityId.isBlank()) {
            throw MessageQueueRejectException("Received event with blank nonSourceabilityId. Discarding.")
        }
        try {
            UUID.fromString(nonSourceabilityId)
        } catch (e: IllegalArgumentException) {
            logger.error("Malformed nonSourceabilityId='$nonSourceabilityId'. Discarding.", e)
            throw MessageQueueRejectException("Malformed nonSourceabilityId='$nonSourceabilityId'", e)
        }
    }
}
