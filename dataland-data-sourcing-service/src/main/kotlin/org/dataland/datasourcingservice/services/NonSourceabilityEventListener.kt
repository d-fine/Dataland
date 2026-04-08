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
 * Handles non-sourceability lifecycle events routed by the backend and QA service.
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
     * Handles a non-sourceability-created event from the backend and transitions the matching
     * data sourcing object to [DataSourcingState.NonSourceableVerification].
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.DATA_SOURCING_SERVICE_NON_SOURCEABILITY_CREATED,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.BACKEND_DATA_NONSOURCEABLE, declare = "false"),
                key = [RoutingKeyNames.NON_SOURCEABILITY_CREATED],
            ),
        ],
    )
    fun onNonSourceabilityCreated(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) messageType: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.rejectMessageOnException {
            MessageQueueUtils.validateMessageType(messageType, MessageType.NON_SOURCEABILITY_CREATED)
            val event = MessageQueueUtils.readMessagePayload<NonSourceabilityLifecycleEvent>(payload)
            CorrelationLogging.withNonSourceabilityContext(correlationId, event.nonSourceabilityId) {
                validateNonSourceabilityId(event.nonSourceabilityId)
                transitionToVerification(event, correlationId)
            }
        }
    }

    /**
     * Handles a non-sourceability-auto-accepted event from the backend (bypassQa=true fast-path) and
     * transitions the matching data sourcing object directly to [DataSourcingState.NonSourceable].
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.DATA_SOURCING_SERVICE_NON_SOURCEABILITY_AUTO_ACCEPTED,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.BACKEND_DATA_NONSOURCEABLE, declare = "false"),
                key = [RoutingKeyNames.NON_SOURCEABILITY_AUTO_ACCEPTED],
            ),
        ],
    )
    fun onNonSourceabilityAutoAccepted(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) messageType: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.rejectMessageOnException {
            MessageQueueUtils.validateMessageType(messageType, MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED)
            val event = MessageQueueUtils.readMessagePayload<NonSourceabilityLifecycleEvent>(payload)
            CorrelationLogging.withNonSourceabilityContext(correlationId, event.nonSourceabilityId) {
                validateNonSourceabilityId(event.nonSourceabilityId)
                transitionToNonSourceable(event, correlationId)
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
