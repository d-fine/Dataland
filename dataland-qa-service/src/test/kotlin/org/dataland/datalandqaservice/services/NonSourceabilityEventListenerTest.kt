package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.datalandqaservice.repositories.NonSourceableQaReviewRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class NonSourceabilityEventListenerTest {
    private val repository = mock<NonSourceableQaReviewRepository>()
    private val listener = NonSourceabilityEventListener(jacksonObjectMapper(), repository)

    @Test
    fun `created lifecycle event creates pending QA review row`() {
        val nonSourceabilityId = UUID.randomUUID()
        whenever(repository.existsById(nonSourceabilityId)).thenReturn(false)

        listener.applyEvent(
            NonSourceabilityLifecycleEvent(
                nonSourceabilityId = nonSourceabilityId.toString(),
                companyId = UUID.randomUUID().toString(),
                dataType = "sfdr",
                reportingPeriod = "2025",
                eventType = NonSourceabilityEventType.CREATED,
                reason = "No data source available",
                uploaderUserId = "uploader-1",
                uploadTime = 12345L,
            ),
        )

        val entityCaptor = argumentCaptor<org.dataland.datalandqaservice.entities.NonSourceableQaReviewInformationEntity>()
        verify(repository).save(entityCaptor.capture())
        assertEquals(nonSourceabilityId, entityCaptor.firstValue.nonSourceabilityId)
        assertEquals(QaStatus.Pending, entityCaptor.firstValue.qaStatus)
        assertEquals("No data source available", entityCaptor.firstValue.reason)
        assertEquals("uploader-1", entityCaptor.firstValue.uploaderUserId)
        assertEquals(12345L, entityCaptor.firstValue.uploadTime)
    }

    @Test
    fun `auto accepted lifecycle event does not create QA review row`() {
        listener.applyEvent(
            NonSourceabilityLifecycleEvent(
                nonSourceabilityId = UUID.randomUUID().toString(),
                companyId = UUID.randomUUID().toString(),
                dataType = "sfdr",
                reportingPeriod = "2025",
                eventType = NonSourceabilityEventType.AUTO_ACCEPTED,
            ),
        )

        verify(repository, never()).save(org.mockito.kotlin.any())
    }
}
