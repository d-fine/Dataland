package org.dataland.datasourcingservice.serviceTests

import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.dataland.datasourcingservice.services.DataSourcingQueryManager
import org.dataland.datasourcingservice.services.NonSourceabilityQaDecisionListener
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

/**
 * Unit tests for [NonSourceabilityQaDecisionListener] data-sourcing service.
 *
 * Covers:
 * - QA accepted event transitions sourcing to NonSourceable
 * - QA rejected event keeps sourcing in NonSourceableVerification (no patch)
 * - Unexpected event type throws [MessageQueueRejectException]
 * - No matching sourcing → graceful skip
 * - Already NonSourceable on accepted event → idempotent skip
 */
class NonSourceabilityQaAcceptedConsumerTest {
    private val queryManager: DataSourcingQueryManager = mock()
    private val sourcingManager: DataSourcingManager = mock()
    private val listener = NonSourceabilityQaDecisionListener(queryManager, sourcingManager)

    private fun buildEvent(companyId: String = UUID.randomUUID().toString()) =
        NonSourceabilityLifecycleEvent(
            nonSourceabilityId = UUID.randomUUID().toString(),
            companyId = companyId,
            dataType = "eutaxonomy-non-financials",
            reportingPeriod = "2024",
        )

    private fun stubSourcing(state: DataSourcingState): StoredDataSourcing =
        mock<StoredDataSourcing>().also {
            whenever(it.dataSourcingId).thenReturn(UUID.randomUUID().toString())
            whenever(it.state).thenReturn(state)
        }

    @Test
    fun `accepted event transitions sourcing to NonSourceable`() {
        val stored = stubSourcing(DataSourcingState.NonSourceableVerification)
        whenever(queryManager.searchDataSourcings(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .thenReturn(listOf(stored))
        whenever(sourcingManager.patchDataSourcingEntityById(any(), any())).thenReturn(stored)

        listener.processQaDecisionEvent(buildEvent(), MessageType.NON_SOURCEABILITY_QA_ACCEPTED, "corr-1")

        verify(sourcingManager).patchDataSourcingEntityById(any(), any())
    }

    @Test
    fun `accepted event is idempotent when sourcing already NonSourceable`() {
        val stored = stubSourcing(DataSourcingState.NonSourceable)
        whenever(queryManager.searchDataSourcings(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .thenReturn(listOf(stored))

        listener.processQaDecisionEvent(buildEvent(), MessageType.NON_SOURCEABILITY_QA_ACCEPTED, "corr-2")

        verify(sourcingManager, never()).patchDataSourcingEntityById(any(), any())
    }

    @Test
    fun `rejected event keeps sourcing in NonSourceableVerification no patch performed`() {
        val stored = stubSourcing(DataSourcingState.NonSourceableVerification)
        whenever(queryManager.searchDataSourcings(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .thenReturn(listOf(stored))

        listener.processQaDecisionEvent(buildEvent(), MessageType.NON_SOURCEABILITY_QA_REJECTED, "corr-3")

        verify(sourcingManager, never()).patchDataSourcingEntityById(any(), any())
    }

    @Test
    fun `graceful skip when no matching sourcing found`() {
        whenever(queryManager.searchDataSourcings(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .thenReturn(emptyList())

        listener.processQaDecisionEvent(buildEvent(), MessageType.NON_SOURCEABILITY_QA_ACCEPTED, "corr-4")

        verify(sourcingManager, never()).patchDataSourcingEntityById(any(), any())
    }

    @Test
    fun `unexpected event type throws reject exception`() {
        assertThrows<MessageQueueRejectException> {
            listener.processQaDecisionEvent(buildEvent(), MessageType.NON_SOURCEABILITY_CREATED, "corr-5")
        }
    }
}
