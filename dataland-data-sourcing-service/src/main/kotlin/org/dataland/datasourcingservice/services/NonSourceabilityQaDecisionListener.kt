package org.dataland.datasourcingservice.services

import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Dedicated adapter for QA decision lifecycle events in data-sourcing.
 */
@Component
class NonSourceabilityQaDecisionListener(
    @Autowired private val nonSourceabilityEventListener: NonSourceabilityEventListener,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Applies a QA decision lifecycle event by delegating to the event listener.
     * Logs QA rejection decisions and delegates acceptance/rejection handling.
     *
     * @param event the non-sourceability lifecycle event
     */
    fun applyEvent(event: NonSourceabilityLifecycleEvent) {
        if (event.eventType !in setOf(NonSourceabilityEventType.QA_ACCEPTED, NonSourceabilityEventType.QA_REJECTED)) {
            return
        }

        if (event.eventType == NonSourceabilityEventType.QA_REJECTED) {
            logger.info(
                "Applying QA rejection for nonSourceabilityId {} by keeping data-sourcing in manual verification handling",
                event.nonSourceabilityId,
            )
        }
        nonSourceabilityEventListener.applyEvent(event)
    }
}
