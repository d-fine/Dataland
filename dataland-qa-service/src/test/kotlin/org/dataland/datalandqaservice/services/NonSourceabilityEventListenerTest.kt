package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
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
    private val objectMapper = jacksonObjectMapper().findAndRegisterModules()

    @BeforeEach
    fun setUp() {
        listener = NonSourceabilityEventListener(repository)
    }

    private fun event(nonSourceabilityId: String = "00000000-0000-0000-0000-000000000001") =
        NonSourceabilityLifecycleEvent(
            nonSourceabilityId = nonSourceabilityId,
            companyId = "company-1",
            dataType = "eutaxonomy-financials",
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

        listener.processCreatedEvent(event())

        verify(repository, never()).save(any())
    }

    @Test
    fun `onNonSourceabilityCreated throws reject exception for wrong message type`() {
        val payload = objectMapper.writeValueAsString(event())
        assertThrows<MessageQueueRejectException> {
            listener.onNonSourceabilityCreated(payload, MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED)
        }
    }
}
