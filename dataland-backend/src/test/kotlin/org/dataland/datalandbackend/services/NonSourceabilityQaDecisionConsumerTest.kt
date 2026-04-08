package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.NonSourceabilityInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.repositories.NonSourceabilityDataRepository
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional
import java.util.UUID

class NonSourceabilityQaDecisionConsumerTest {
    private val repository = mock<NonSourceabilityDataRepository>()
    private val listener =
        NonSourceabilityQaDecisionListener(
            ObjectMapper(),
            repository,
        )

    @Test
    fun `discard malformed nonSourceabilityId without persistence changes`() {
        listener.applyEvent(
            NonSourceabilityLifecycleEvent(
                nonSourceabilityId = "not-a-uuid",
                companyId = UUID.randomUUID().toString(),
                dataType = "sfdr",
                reportingPeriod = "2025",
                eventType = NonSourceabilityEventType.QA_ACCEPTED,
            ),
        )

        verify(repository, never()).findById(any())
        verify(repository, never()).save(any())
    }

    @Test
    fun `discard unknown nonSourceabilityId without persistence changes`() {
        val unknownId = UUID.randomUUID()
        whenever(repository.findById(unknownId)).thenReturn(Optional.empty())

        listener.applyEvent(
            NonSourceabilityLifecycleEvent(
                nonSourceabilityId = unknownId.toString(),
                companyId = UUID.randomUUID().toString(),
                dataType = "sfdr",
                reportingPeriod = "2025",
                eventType = NonSourceabilityEventType.QA_REJECTED,
            ),
        )

        verify(repository).findById(unknownId)
        verify(repository, never()).save(any())
    }

    @Test
    fun `qa accepted event updates canonical row to accepted and active`() {
        val knownId = UUID.randomUUID()
        val entity =
            NonSourceabilityInformationEntity(
                nonSourceabilityId = knownId,
                companyId = UUID.randomUUID().toString(),
                dataType = DataType("sfdr"),
                reportingPeriod = "2025",
                qaStatus = QaStatus.Pending,
                uploaderUserId = "uploader",
                uploadTime = 100L,
                currentlyActive = false,
                reason = "pending",
                bypassQa = false,
            )
        whenever(repository.findById(knownId)).thenReturn(Optional.of(entity))

        listener.applyEvent(
            NonSourceabilityLifecycleEvent(
                nonSourceabilityId = knownId.toString(),
                companyId = entity.companyId,
                dataType = entity.dataType.toString(),
                reportingPeriod = entity.reportingPeriod,
                eventType = NonSourceabilityEventType.QA_ACCEPTED,
            ),
        )

        val savedCaptor = argumentCaptor<NonSourceabilityInformationEntity>()
        verify(repository).save(savedCaptor.capture())
        assertEquals(QaStatus.Accepted, savedCaptor.firstValue.qaStatus)
        assertTrue(savedCaptor.firstValue.currentlyActive)
    }

    @Test
    fun `qa rejected event updates canonical row to rejected and inactive`() {
        val knownId = UUID.randomUUID()
        val entity =
            NonSourceabilityInformationEntity(
                nonSourceabilityId = knownId,
                companyId = UUID.randomUUID().toString(),
                dataType = DataType("sfdr"),
                reportingPeriod = "2025",
                qaStatus = QaStatus.Pending,
                uploaderUserId = "uploader",
                uploadTime = 200L,
                currentlyActive = true,
                reason = "pending",
                bypassQa = false,
            )
        whenever(repository.findById(knownId)).thenReturn(Optional.of(entity))

        listener.applyEvent(
            NonSourceabilityLifecycleEvent(
                nonSourceabilityId = knownId.toString(),
                companyId = entity.companyId,
                dataType = entity.dataType.toString(),
                reportingPeriod = entity.reportingPeriod,
                eventType = NonSourceabilityEventType.QA_REJECTED,
            ),
        )

        val savedCaptor = argumentCaptor<NonSourceabilityInformationEntity>()
        verify(repository).save(savedCaptor.capture())
        assertEquals(QaStatus.Rejected, savedCaptor.firstValue.qaStatus)
        assertTrue(!savedCaptor.firstValue.currentlyActive)
    }
}
