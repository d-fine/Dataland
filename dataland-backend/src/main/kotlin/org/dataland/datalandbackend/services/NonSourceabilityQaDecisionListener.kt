package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.NonSourceabilityInformationEntity
import org.dataland.datalandbackend.repositories.NonSourceabilityDataRepository
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
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
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Applies QA decision lifecycle events to backend canonical non-sourceability records.
 */
@Component
class NonSourceabilityQaDecisionListener(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val nonSourceabilityDataRepository: NonSourceabilityDataRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Handles the consumption of QA decision lifecycle events from the message queue.
     *
     * @param payload the message payload containing the lifecycle event
     * @param type the message type header to validate
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.BACKEND_NON_SOURCEABILITY_EVENTS,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.BACKEND_DATA_NONSOURCEABLE, declare = "false"),
                key = [RoutingKeyNames.NON_SOURCEABILITY_LIFECYCLE],
            ),
        ],
    )
    fun consumeLifecycleEvent(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.NON_SOURCEABILITY_LIFECYCLE)
        MessageQueueUtils.rejectMessageOnException {
            consume(payload)
        }
    }

    /**
     * Deserializes and applies a non-sourceability lifecycle event.
     *
     * @param payload the serialized event payload
     */
    fun consume(payload: String) {
        val event =
            runCatching {
                objectMapper.readValue(payload, NonSourceabilityLifecycleEvent::class.java)
            }.getOrElse {
                logger.error("Discarding non-sourceability QA decision event due to malformed payload", it)
                return
            }

        applyEvent(event)
    }

    /**
     * Applies a non-sourceability QA decision to the canonical record.
     * Updates the QA status and currently-active flag based on acceptance or rejection.
     *
     * @param event the non-sourceability lifecycle event
     */
    fun applyEvent(event: NonSourceabilityLifecycleEvent) {
        if (isQaDecisionEvent(event)) {
            parseNonSourceabilityId(event)?.let { parsedId ->
                findEntityOrLogError(parsedId, event)?.let { entity ->
                    updateEntityBasedOnDecision(entity, event)
                    nonSourceabilityDataRepository.save(entity)
                }
            }
        }
    }

    private fun isQaDecisionEvent(event: NonSourceabilityLifecycleEvent): Boolean =
        event.eventType in setOf(NonSourceabilityEventType.QA_ACCEPTED, NonSourceabilityEventType.QA_REJECTED)

    private fun parseNonSourceabilityId(event: NonSourceabilityLifecycleEvent): UUID? =
        runCatching { UUID.fromString(event.nonSourceabilityId) }
            .getOrElse {
                logger.error(
                    "Discarding non-sourceability QA decision event because nonSourceabilityId is malformed: " +
                        event.nonSourceabilityId,
                )
                null
            }

    private fun findEntityOrLogError(
        parsedId: UUID,
        event: NonSourceabilityLifecycleEvent,
    ): NonSourceabilityInformationEntity? {
        val entity = nonSourceabilityDataRepository.findById(parsedId).orElse(null)
        if (entity == null) {
            logger.error(
                "Discarding non-sourceability QA decision event because nonSourceabilityId was not found: " +
                    event.nonSourceabilityId,
            )
        }
        return entity
    }

    private fun updateEntityBasedOnDecision(
        entity: NonSourceabilityInformationEntity,
        event: NonSourceabilityLifecycleEvent,
    ) {
        when (event.eventType) {
            NonSourceabilityEventType.QA_ACCEPTED -> {
                entity.qaStatus = QaStatus.Accepted
                entity.currentlyActive = true
            }

            NonSourceabilityEventType.QA_REJECTED -> {
                entity.qaStatus = QaStatus.Rejected
                entity.currentlyActive = false
            }

            else -> {
                // Should not reach here due to isQaDecisionEvent filter
            }
        }
    }
}
