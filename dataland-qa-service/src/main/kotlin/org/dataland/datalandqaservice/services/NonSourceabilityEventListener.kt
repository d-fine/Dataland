package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datalandqaservice.entities.NonSourceableQaReviewInformationEntity
import org.dataland.datalandqaservice.repositories.NonSourceableQaReviewRepository
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
 * Consumes backend non-sourceability lifecycle events and creates QA-side review records.
 */
@Component
class NonSourceabilityEventListener(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val nonSourceableQaReviewRepository: NonSourceableQaReviewRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Consumes a non-sourceability lifecycle event from the message queue.
     *
     * @param payload the serialized event payload
     * @param type the message type header
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.QA_SERVICE_NON_SOURCEABILITY_EVENTS,
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
    fun consume(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.NON_SOURCEABILITY_LIFECYCLE)
        MessageQueueUtils.rejectMessageOnException {
            val event = objectMapper.readValue(payload, NonSourceabilityLifecycleEvent::class.java)
            applyEvent(event)
        }
    }

    /**
     * Applies a non-sourceability CREATED event by creating a QA review entry.
     * Only processes CREATED events; other event types are ignored.
     *
     * @param event the non-sourceability lifecycle event
     */
    fun applyEvent(event: NonSourceabilityLifecycleEvent) {
        if (event.eventType == NonSourceabilityEventType.CREATED) {
            parseNonSourceabilityId(event)?.let { nonSourceabilityId ->
                if (!nonSourceableQaReviewRepository.existsById(nonSourceabilityId)) {
                    createAndSaveQaReviewEntity(nonSourceabilityId, event)
                } else {
                    logger.info("Skipping duplicate non-sourceability CREATED event for id $nonSourceabilityId")
                }
            }
        }
    }

    private fun parseNonSourceabilityId(event: NonSourceabilityLifecycleEvent): UUID? =
        runCatching { UUID.fromString(event.nonSourceabilityId) }
            .getOrElse {
                logger.error(
                    "Discarding non-sourceability CREATED event because nonSourceabilityId is malformed: " +
                        event.nonSourceabilityId,
                )
                null
            }

    private fun createAndSaveQaReviewEntity(
        nonSourceabilityId: UUID,
        event: NonSourceabilityLifecycleEvent,
    ) {
        val qaReviewEntity =
            NonSourceableQaReviewInformationEntity(
                nonSourceabilityId = nonSourceabilityId,
                companyId = event.companyId,
                dataType = event.dataType,
                reportingPeriod = event.reportingPeriod,
                qaStatus = QaStatus.Pending,
                reason = event.reason ?: "",
                uploaderUserId = event.uploaderUserId ?: "",
                uploadTime = event.uploadTime ?: 0L,
            )
        nonSourceableQaReviewRepository.save(qaReviewEntity)
    }
}
