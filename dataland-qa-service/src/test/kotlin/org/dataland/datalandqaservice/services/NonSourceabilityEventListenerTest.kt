package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.datalandqaservice.entities.NonSourceableQaReviewInformationEntity
import org.dataland.datalandqaservice.repositories.NonSourceableQaReviewRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class NonSourceabilityEventListenerTest {
    companion object {
        private const val NON_SOURCEABILITY_ID = "00000000-0000-0000-0000-000000000001"
        private const val COMPANY_ID = "company-1"
        private const val DATA_TYPE = "eutaxonomy-financials"
    }

    private val repository: NonSourceableQaReviewRepository = mock()
    private lateinit var listener: NonSourceabilityEventListener
    private val objectMapper = jacksonObjectMapper().findAndRegisterModules()

    @BeforeEach
    fun setUp() {
        listener = NonSourceabilityEventListener(repository)
    }

    private fun event(nonSourceabilityId: String = NON_SOURCEABILITY_ID) =
        NonSourceabilityLifecycleEvent(
            nonSourceabilityId = nonSourceabilityId,
            companyId = COMPANY_ID,
            dataType = DATA_TYPE,
            reportingPeriod = "2023",
        )

    @Test
    fun `processCreatedEvent persists QA review record with Pending status`() {
        whenever(repository.findByNonSourceabilityId(any())).thenReturn(null)

        listener.processCreatedEvent(event())

        verify(repository).save(any<NonSourceableQaReviewInformationEntity>())
    }

    @Test
    fun `processCreatedEvent is idempotent skips when review already exists`() {
        val existing =
            NonSourceableQaReviewInformationEntity(
                nonSourceabilityId = NON_SOURCEABILITY_ID,
                companyId = COMPANY_ID,
                dataType = DATA_TYPE,
                reportingPeriod = "2023",
                qaStatus = QaStatus.Pending,
                reason = null,
                uploaderUserId = "",
                uploadTime = Instant.now().toEpochMilli(),
            )
        whenever(repository.findByNonSourceabilityId(any())).thenReturn(existing)

        listener.processCreatedEvent(event())

        verify(repository, never()).save(any())
    }

    @Test
    fun `processAutoAcceptedEvent persists QA review record with Accepted status and no reviewer`() {
        whenever(repository.findByNonSourceabilityId(any())).thenReturn(null)

        listener.processAutoAcceptedEvent(event())

        val captor = argumentCaptor<NonSourceableQaReviewInformationEntity>()
        verify(repository).save(captor.capture())
        val savedEntity = captor.firstValue
        assertEquals(QaStatus.Accepted, savedEntity.qaStatus)
        assertNull(savedEntity.reviewerUserId)
    }

    @Test
    fun `processAutoAcceptedEvent is idempotent skips when review already exists`() {
        val existing =
            NonSourceableQaReviewInformationEntity(
                nonSourceabilityId = NON_SOURCEABILITY_ID,
                companyId = COMPANY_ID,
                dataType = DATA_TYPE,
                reportingPeriod = "2023",
                qaStatus = QaStatus.Accepted,
                reason = null,
                uploaderUserId = "",
                uploadTime = Instant.now().toEpochMilli(),
            )
        whenever(repository.findByNonSourceabilityId(any())).thenReturn(existing)

        listener.processAutoAcceptedEvent(event())

        verify(repository, never()).save(any())
    }

    @Test
    fun `onNonSourceabilitySubmission throws reject exception for unexpected message type`() {
        val payload = objectMapper.writeValueAsString(event())
        assertThrows<MessageQueueRejectException> {
            listener.onNonSourceabilitySubmission(payload, "unexpectedMessageType")
        }
    }
}
