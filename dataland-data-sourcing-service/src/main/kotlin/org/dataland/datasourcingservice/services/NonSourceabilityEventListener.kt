package org.dataland.datasourcingservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
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
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Consumes non-sourceability lifecycle events and applies data-sourcing state transitions.
 */
@Component
class NonSourceabilityEventListener(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val dataSourcingQueryManager: DataSourcingQueryManager,
    @Autowired private val dataSourcingManager: DataSourcingManager,
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
                        QueueNames.DATA_SOURCING_SERVICE_NON_SOURCEABILITY_EVENTS,
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
     * Applies a non-sourceability lifecycle event by updating the data-sourcing state.
     *
     * @param event the non-sourceability lifecycle event
     */
    fun applyEvent(event: NonSourceabilityLifecycleEvent) {
        parseUuid(event.nonSourceabilityId, "nonSourceabilityId")?.let {
            parseUuid(event.companyId, "companyId")?.let { companyId ->
                val targetState = mapEventTypeToDataSourcingState(event.eventType)
                findMatchingDataSourcing(companyId, event)?.let { matchingDataSourcing ->
                    dataSourcingManager.patchDataSourcingEntityById(
                        UUID.fromString(matchingDataSourcing.dataSourcingId),
                        DataSourcingPatch(state = targetState),
                    )
                }
            }
        }
    }

    private fun parseUuid(
        value: String,
        fieldName: String,
    ): UUID? =
        runCatching { UUID.fromString(value) }
            .getOrElse {
                logger.error(
                    "Discarding non-sourceability lifecycle event because $fieldName is malformed: $value",
                )
                null
            }

    private fun mapEventTypeToDataSourcingState(eventType: NonSourceabilityEventType): DataSourcingState =
        when (eventType) {
            NonSourceabilityEventType.CREATED,
            NonSourceabilityEventType.QA_REJECTED,
            -> DataSourcingState.NonSourceableVerification

            NonSourceabilityEventType.AUTO_ACCEPTED,
            NonSourceabilityEventType.QA_ACCEPTED,
            -> DataSourcingState.NonSourceable
        }

    private fun findMatchingDataSourcing(
        companyId: UUID,
        event: NonSourceabilityLifecycleEvent,
    ): org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing? {
        val matchingDataSourcing =
            dataSourcingQueryManager
                .searchDataSourcings(
                    companyId = companyId,
                    dataType = event.dataType,
                    reportingPeriod = event.reportingPeriod,
                    state = null,
                    chunkSize = 1,
                    chunkIndex = 0,
                ).firstOrNull()

        if (matchingDataSourcing == null) {
            logger.error(
                "Discarding non-sourceability lifecycle event because no data-sourcing " +
                    "record was found for nonSourceabilityId ${event.nonSourceabilityId}",
            )
        }
        return matchingDataSourcing
    }
}
