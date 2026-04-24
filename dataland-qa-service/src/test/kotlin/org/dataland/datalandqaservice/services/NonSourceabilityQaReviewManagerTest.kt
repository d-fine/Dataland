package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandqaservice.entities.NonSourceableQaReviewInformationEntity
import org.dataland.datalandqaservice.repositories.NonSourceableQaReviewRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class NonSourceabilityQaReviewManagerTest {
    companion object {
        private const val DEFAULT_COMPANY_ID = "company-1"
        private const val DEFAULT_REVIEWER_ID = "reviewer-1"
    }

    private val repository: NonSourceableQaReviewRepository = mock()
    private val cloudEventMessageHandler: CloudEventMessageHandler = mock()
    private val objectMapper = jacksonObjectMapper().findAndRegisterModules()
    private lateinit var manager: NonSourceabilityQaReviewManager

    @BeforeEach
    fun setUp() {
        manager = NonSourceabilityQaReviewManager(repository, cloudEventMessageHandler, objectMapper)
    }

    private fun entity(
        nonSourceabilityId: String = "00000000-0000-0000-0000-000000000001",
        companyId: String = DEFAULT_COMPANY_ID,
        dataType: String = "eutaxonomy-financials",
        reportingPeriod: String = "2023",
        qaStatus: QaStatus = QaStatus.Pending,
    ) = NonSourceableQaReviewInformationEntity(
        nonSourceabilityId = nonSourceabilityId,
        companyId = companyId,
        dataType = dataType,
        reportingPeriod = reportingPeriod,
        qaStatus = qaStatus,
        reason = null,
        uploaderUserId = "uploader-1",
        uploadTime = Instant.now().toEpochMilli(),
    )

    // ─── getReviews ───────────────────────────────────────────────────────

    @Test
    fun `getReviews returns all entries when no filters are specified`() {
        val entities = listOf(entity("id-1"), entity("id-2"))
        whenever(repository.findByQaStatusFilter(null)).thenReturn(entities)

        val result = manager.getReviews(null, null, null, null, 10, 0)

        assertEquals(2, result.size)
    }

    @Test
    fun `getReviews filters by companyId`() {
        val entities = listOf(entity(companyId = DEFAULT_COMPANY_ID), entity(companyId = "company-2"))
        whenever(repository.findByQaStatusFilter(null)).thenReturn(entities)

        val result = manager.getReviews(DEFAULT_COMPANY_ID, null, null, null, 10, 0)

        assertEquals(1, result.size)
        assertEquals(DEFAULT_COMPANY_ID, result.first().companyId)
    }

    @Test
    fun `getReviews filters by dataType`() {
        val entities = listOf(entity(dataType = "eutaxonomy-financials"), entity(dataType = "sfdr"))
        whenever(repository.findByQaStatusFilter(null)).thenReturn(entities)

        val result = manager.getReviews(null, "sfdr", null, null, 10, 0)

        assertEquals(1, result.size)
        assertEquals("sfdr", result.first().dataType)
    }

    @Test
    fun `getReviews filters by reportingPeriod`() {
        val entities = listOf(entity(reportingPeriod = "2023"), entity(reportingPeriod = "2024"))
        whenever(repository.findByQaStatusFilter(null)).thenReturn(entities)

        val result = manager.getReviews(null, null, "2024", null, 10, 0)

        assertEquals(1, result.size)
        assertEquals("2024", result.first().reportingPeriod)
    }

    @Test
    fun `getReviews applies pagination via chunkSize and chunkIndex`() {
        val entities = (1..5).map { entity("id-$it") }
        whenever(repository.findByQaStatusFilter(null)).thenReturn(entities)

        val page0 = manager.getReviews(null, null, null, null, 2, 0)
        val page1 = manager.getReviews(null, null, null, null, 2, 1)
        val page2 = manager.getReviews(null, null, null, null, 2, 2)

        assertEquals(2, page0.size)
        assertEquals(2, page1.size)
        assertEquals(1, page2.size)
    }

    @Test
    fun `getReviews delegates qaStatus filter to repository`() {
        val pending = listOf(entity(qaStatus = QaStatus.Pending))
        whenever(repository.findByQaStatusFilter(QaStatus.Pending)).thenReturn(pending)

        val result = manager.getReviews(null, null, null, QaStatus.Pending, 10, 0)

        assertEquals(1, result.size)
        assertEquals(QaStatus.Pending, result.first().qaStatus)
        verify(repository).findByQaStatusFilter(QaStatus.Pending)
    }

    // ─── getQueue ─────────────────────────────────────────────────────────

    @Test
    fun `getQueue returns pending entries`() {
        val pending = listOf(entity(qaStatus = QaStatus.Pending))
        whenever(repository.findByQaStatusFilter(QaStatus.Pending)).thenReturn(pending)

        val result = manager.getQueue()

        assertEquals(1, result.size)
        assertEquals(QaStatus.Pending, result.first().qaStatus)
        verify(repository).findByQaStatusFilter(QaStatus.Pending)
    }

    // ─── postDecision ─────────────────────────────────────────────────────

    @Test
    fun `postDecision accepted updates entity and emits QA_ACCEPTED event`() {
        val e = entity()
        whenever(repository.findByNonSourceabilityId(e.nonSourceabilityId)).thenReturn(e)
        whenever(repository.save(e)).thenReturn(e)

        val result = manager.postDecision(e.nonSourceabilityId, QaStatus.Accepted, null, DEFAULT_REVIEWER_ID, "corr-1")

        assertEquals(QaStatus.Accepted, result.qaStatus)
        assertEquals(DEFAULT_REVIEWER_ID, result.reviewerUserId)
        assertNull(result.qaComment)
        verify(cloudEventMessageHandler).buildCEMessageAndSendToQueue(
            any(),
            eq(MessageType.NON_SOURCEABILITY_QA_ACCEPTED),
            eq("corr-1"),
            eq(ExchangeName.QA_SERVICE_NON_SOURCEABILITY_DECISIONS),
            eq(RoutingKeyNames.NON_SOURCEABILITY_QA_DECISION),
        )
    }

    @Test
    fun `postDecision rejected updates entity and emits QA_REJECTED event`() {
        val e = entity()
        whenever(repository.findByNonSourceabilityId(e.nonSourceabilityId)).thenReturn(e)
        whenever(repository.save(e)).thenReturn(e)

        val result = manager.postDecision(e.nonSourceabilityId, QaStatus.Rejected, "Not applicable", DEFAULT_REVIEWER_ID, "corr-2")

        assertEquals(QaStatus.Rejected, result.qaStatus)
        assertEquals("Not applicable", result.qaComment)
        verify(cloudEventMessageHandler).buildCEMessageAndSendToQueue(
            any(),
            eq(MessageType.NON_SOURCEABILITY_QA_REJECTED),
            eq("corr-2"),
            eq(ExchangeName.QA_SERVICE_NON_SOURCEABILITY_DECISIONS),
            eq(RoutingKeyNames.NON_SOURCEABILITY_QA_DECISION),
        )
    }

    @Test
    fun `postDecision throws ResourceNotFoundApiException when entity not found`() {
        whenever(repository.findByNonSourceabilityId(any())).thenReturn(null)

        assertThrows<ResourceNotFoundApiException> {
            manager.postDecision("non-existent-id", QaStatus.Accepted, null, DEFAULT_REVIEWER_ID, "corr-3")
        }

        verify(cloudEventMessageHandler, never()).buildCEMessageAndSendToQueue(any(), any(), any(), any(), any())
    }

    @Test
    fun `postDecision throws IllegalArgumentException when qaStatus is Pending`() {
        assertThrows<IllegalArgumentException> {
            manager.postDecision("some-id", QaStatus.Pending, null, DEFAULT_REVIEWER_ID, "corr-4")
        }

        verify(repository, never()).findByNonSourceabilityId(any())
        verify(cloudEventMessageHandler, never()).buildCEMessageAndSendToQueue(any(), any(), any(), any(), any())
    }
}
