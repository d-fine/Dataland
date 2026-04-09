package org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.datalandqaservice.entities.NonSourceableQaReviewInformationEntity
import org.dataland.datalandqaservice.repositories.NonSourceableQaReviewRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class NonSourceabilityEventListenerTest {
    private val repository: NonSourceableQaReviewRepository = mock()
    private lateinit var listener: NonSourceabilityEventListener

    @BeforeEach
    fun setUp() {
        listener = NonSourceabilityEventListener(repository)
    }

    private fun event(
        eventType: NonSourceabilityEventType = NonSourceabilityEventType.NON_SOURCEABILITY_CREATED,
        nonSourceabilityId: String = "00000000-0000-0000-0000-000000000001",
    ) = NonSourceabilityLifecycleEvent(
        nonSourceabilityId = nonSourceabilityId,
        companyId = "company-1",
        dataType = "eutaxonomy-financials",
        reportingPeriod = "2023",
        eventType = eventType,
    )

    @Test
    fun `processCreatedEvent persists QA review record with Pending status`() {
        whenever(repository.findByNonSourceabilityId(any())).thenReturn(null)

        listener.processCreatedEvent(event(), "corr-1")

        verify(repository).save(any())
    }

    @Test
    fun `processCreatedEvent is idempotent - skips when review already exists`() {
        val existing =
            NonSourceableQaReviewInformationEntity(
                nonSourceabilityId = "00000000-0000-0000-0000-000000000001",
                companyId = "company-1",
                dataType = "eutaxonomy-financials",
                reportingPeriod = "2023",
                qaStatus = QaStatus.Pending,
                reason = null,
                uploaderUserId = "",
                uploadTime = Instant.now().toEpochMilli(),
            )
        whenever(repository.findByNonSourceabilityId(any())).thenReturn(existing)

        listener.processCreatedEvent(event(), "corr-2")

        verify(repository, never()).save(any())
    }

    @Test
    fun `processCreatedEvent throws reject exception for wrong event type`() {
        assertThrows<MessageQueueRejectException> {
            listener.processCreatedEvent(event(eventType = NonSourceabilityEventType.NON_SOURCEABILITY_QA_ACCEPTED), "corr-3")
        }
    }
}
