package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.repositories.NonSourceabilityDataRepository
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Listens to non-sourceability QA decision events (accepted / rejected) emitted by the QA service
 * and applies the resulting state change to [NonSourceabilityInformationEntity].
 *
 * Fail-fast validation: events with malformed or unresolvable nonSourceabilityId are
 * discarded with an error log and a [MessageQueueRejectException] so they route to the dead-letter
 * exchange rather than causing silent state corruption.
 */
@Service
class NonSourceabilityQaDecisionListener(
    @Autowired private val nonSourceabilityDataRepository: NonSourceabilityDataRepository,
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
                        QueueNames.BACKEND_NON_SOURCEABILITY_QA_DECISION,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.QA_SERVICE_NON_SOURCEABILITY_DECISIONS),
                key = [RoutingKeyNames.NON_SOURCEABILITY_QA_DECISION],
            ),
        ],
    )
    fun onNonSourceabilityQaDecision(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) messageType: String,
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
            processQaDecisionEvent(event, messageType)
        }
    }

    /**
     * Applies the QA accepted or rejected state to the backend canonical entity.
     * Exposed as internal fun to allow direct unit-test invocation.
     */
    @Transactional
    internal fun processQaDecisionEvent(
        event: NonSourceabilityLifecycleEvent,
        messageType: String,
    ) {
        val nonSourceabilityId = parseAndValidateId(event.nonSourceabilityId)
        val entity =
            nonSourceabilityDataRepository.findById(nonSourceabilityId).orElseThrow {
                logger.error(
                    "Received QA decision event for unknown nonSourceabilityId=${event.nonSourceabilityId}.Discarding.",
                )
                MessageQueueRejectException(
                    "Unknown nonSourceabilityId=${event.nonSourceabilityId} in QA decision event",
                )
            }

        when (messageType) {
            MessageType.NON_SOURCEABILITY_QA_ACCEPTED -> {
                entity.qaStatus = QaStatus.Accepted
                entity.currentlyActive = true
                logger.info(
                    "Set qaStatus=Accepted and currentlyActive=true for nonSourceabilityId=${entity.nonSourceabilityId}",
                )
            }

            MessageType.NON_SOURCEABILITY_QA_REJECTED -> {
                entity.qaStatus = QaStatus.Rejected
                entity.currentlyActive = false
                logger.info(
                    "Set qaStatus=Rejected and currentlyActive=false for nonSourceabilityId=${entity.nonSourceabilityId}",
                )
            }

            else -> {
                logger.error(
                    "Unexpected message type $messageType received in NonSourceabilityQaDecisionListener. Discarding.",
                )
                throw MessageQueueRejectException("Unexpected message type $messageType in QA decision listener")
            }
        }
        nonSourceabilityDataRepository.save(entity)
    }

    private fun parseAndValidateId(rawId: String): UUID {
        try {
            return UUID.fromString(rawId)
        } catch (e: IllegalArgumentException) {
            logger.error(
                "Malformed nonSourceabilityId='$rawId' received in QA decision event. Discarding.",
                e,
            )
            throw MessageQueueRejectException("Malformed nonSourceabilityId='$rawId'", e)
        }
    }
}
