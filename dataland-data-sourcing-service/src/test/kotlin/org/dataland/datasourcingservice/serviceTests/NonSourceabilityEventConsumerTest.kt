package org.dataland.datasourcingservice.serviceTests

import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.dataland.datasourcingservice.services.DataSourcingQueryManager
import org.dataland.datasourcingservice.services.NonSourceabilityEventListener
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

/**
 * Tests for the data-sourcing service non-sourceability event consumer.
 * Covers both fail-fast validation and transition behavior.
 */
class NonSourceabilityEventConsumerTest {
    private val queryManager: DataSourcingQueryManager = mock()
    private val sourcingManager: DataSourcingManager = mock()
    private lateinit var listener: NonSourceabilityEventListener

    @BeforeEach
    fun setUp() {
        listener = NonSourceabilityEventListener(queryManager, sourcingManager)
    }

    private fun buildEvent(nonSourceabilityId: String = UUID.randomUUID().toString()): NonSourceabilityLifecycleEvent =
        NonSourceabilityLifecycleEvent(
            nonSourceabilityId = nonSourceabilityId,
            companyId = UUID.randomUUID().toString(),
            dataType = "eutaxonomy-non-financials",
            reportingPeriod = "2024",
        )

    private fun stubStoredSourcing(state: DataSourcingState = DataSourcingState.Initialized): StoredDataSourcing =
        mock<StoredDataSourcing>().also {
            whenever(it.dataSourcingId).thenReturn(UUID.randomUUID().toString())
            whenever(it.state).thenReturn(state)
        }

    // ----- fail-fast validation -----

    @Test
    fun `data sourcing listener discards event with malformed nonSourceabilityId and throws reject exception`() {
        assertThrows(MessageQueueRejectException::class.java) {
            listener.validateNonSourceabilityId("!!invalid-id!!")
        }
    }

    @Test
    fun `data sourcing listener discards event with blank nonSourceabilityId and throws reject exception`() {
        assertThrows(MessageQueueRejectException::class.java) {
            listener.validateNonSourceabilityId("")
        }
    }

    // ----- transition behavior -----

    @Test
    fun `transitionToVerification patches sourcing to NonSourceableVerification when Active`() {
        val stored = stubStoredSourcing(DataSourcingState.Initialized)
        val event = buildEvent()
        whenever(queryManager.searchDataSourcings(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .thenReturn(listOf(stored))
        whenever(sourcingManager.patchDataSourcingEntityById(any(), any())).thenReturn(stored)

        listener.transitionToVerification(event, "corr-1")

        verify(sourcingManager).patchDataSourcingEntityById(any(), any())
    }

    @Test
    fun `transitionToVerification is idempotent when already in NonSourceableVerification`() {
        val stored = stubStoredSourcing(DataSourcingState.NonSourceableVerification)
        val event = buildEvent()
        whenever(queryManager.searchDataSourcings(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .thenReturn(listOf(stored))

        listener.transitionToVerification(event, "corr-2")

        verify(sourcingManager, never()).patchDataSourcingEntityById(any(), any())
    }

    @Test
    fun `transitionToVerification skips gracefully when no matching sourcing is found`() {
        val event = buildEvent()
        whenever(queryManager.searchDataSourcings(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .thenReturn(emptyList())

        listener.transitionToVerification(event, "corr-3")

        verify(sourcingManager, never()).patchDataSourcingEntityById(any(), any())
    }

    @Test
    fun `transitionToNonSourceable patches sourcing to NonSourceable state`() {
        val stored = stubStoredSourcing(DataSourcingState.NonSourceableVerification)
        val event = buildEvent()
        whenever(queryManager.searchDataSourcings(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .thenReturn(listOf(stored))
        whenever(sourcingManager.patchDataSourcingEntityById(any(), any())).thenReturn(stored)

        listener.transitionToNonSourceable(event, "corr-4")

        verify(sourcingManager).patchDataSourcingEntityById(any(), any())
    }

    @Test
    fun `transitionToNonSourceable is idempotent when already NonSourceable`() {
        val stored = stubStoredSourcing(DataSourcingState.NonSourceable)
        val event = buildEvent()
        whenever(queryManager.searchDataSourcings(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .thenReturn(listOf(stored))

        listener.transitionToNonSourceable(event, "corr-5")

        verify(sourcingManager, never()).patchDataSourcingEntityById(any(), any())
    }
}
