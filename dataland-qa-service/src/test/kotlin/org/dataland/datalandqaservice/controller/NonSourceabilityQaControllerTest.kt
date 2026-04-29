package org.dataland.datalandqaservice.controller

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.model.NonSourceableQaReviewInformation
import org.dataland.datalandqaservice.services.NonSourceabilityQaReviewManager
import org.dataland.datalandqaservice.utils.UtilityFunctions.withReviewerAuthentication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

/**
 * Unit tests for [NonSourceabilityQaController] covering:
 * - QA controller tests for accepted/rejected decision on POST /nonSourceable/{nonSourceabilityId}
 * - QA controller tests for GET /nonSourceable and GET /nonSourceable/queue
 */
class NonSourceabilityQaControllerTest {
    companion object {
        private const val DEFAULT_NON_SOURCEABILITY_ID = "00000000-0000-0000-0000-000000000001"
    }

    private val manager: NonSourceabilityQaReviewManager = mock()
    private val controller = NonSourceabilityQaController(manager)

    private fun review(
        nonSourceabilityId: String = DEFAULT_NON_SOURCEABILITY_ID,
        qaStatus: QaStatus = QaStatus.Pending,
    ) = NonSourceableQaReviewInformation(
        nonSourceabilityId = nonSourceabilityId,
        companyId = "company-1",
        dataType = "eutaxonomy-financials",
        reportingPeriod = "2023",
        qaStatus = qaStatus,
        reason = null,
        uploaderUserId = "uploader-1",
        uploadTime = 1000L,
        reviewerUserId = null,
        qaComment = null,
    )

    // ─── POST decision ────────────────────────────────────────────────────

    @Test
    fun `postNonSourceabilityDecision returns 200 with accepted review`() {
        val accepted = review(qaStatus = QaStatus.Accepted)
        whenever(manager.postDecision(any(), any(), anyOrNull(), any())).thenReturn(accepted)

        withReviewerAuthentication {
            val request =
                org.dataland.datalandqaservice.model.NonSourceabilityDecisionRequest(
                    qaStatus = QaStatus.Accepted,
                    qaComment = null,
                )
            val result = controller.postNonSourceabilityDecision(DEFAULT_NON_SOURCEABILITY_ID, request)

            assertEquals(HttpStatus.OK, result.statusCode)
            assertEquals(QaStatus.Accepted, result.body?.qaStatus)
            verify(manager).postDecision(any(), any(), anyOrNull(), any())
        }
    }

    @Test
    fun `postNonSourceabilityDecision returns 200 with rejected review and comment`() {
        val rejected = review(qaStatus = QaStatus.Rejected)
        whenever(manager.postDecision(any(), any(), anyOrNull(), any())).thenReturn(rejected)

        withReviewerAuthentication {
            val request =
                org.dataland.datalandqaservice.model.NonSourceabilityDecisionRequest(
                    qaStatus = QaStatus.Rejected,
                    qaComment = "Not applicable",
                )
            val result = controller.postNonSourceabilityDecision(DEFAULT_NON_SOURCEABILITY_ID, request)

            assertEquals(HttpStatus.OK, result.statusCode)
            assertEquals(QaStatus.Rejected, result.body?.qaStatus)
        }
    }

    @Test
    fun `postNonSourceabilityDecision propagates not found exception from manager`() {
        val exception = ResourceNotFoundApiException("Non-sourceability review not found", "No review exists")
        doThrow(exception)
            .whenever(manager)
            .postDecision(any(), any(), anyOrNull(), any())

        withReviewerAuthentication {
            val request =
                org.dataland.datalandqaservice.model.NonSourceabilityDecisionRequest(
                    qaStatus = QaStatus.Accepted,
                    qaComment = null,
                )
            assertThrows<ResourceNotFoundApiException> {
                controller.postNonSourceabilityDecision("00000000-0000-0000-0000-000000000099", request)
            }
        }
    }

    @Test
    fun `postNonSourceabilityDecision returns 400 with pending status`() {
        withReviewerAuthentication {
            val request =
                org.dataland.datalandqaservice.model.NonSourceabilityDecisionRequest(
                    qaStatus = QaStatus.Pending,
                    qaComment = null,
                )
            val result = controller.postNonSourceabilityDecision(DEFAULT_NON_SOURCEABILITY_ID, request)
            assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        }
    }

    // ─── GET listing and queue ────────────────────────────────────────────

    @Test
    fun `getNonSourceableReviews delegates to manager and returns matching entries`() {
        val reviews = listOf(review("id-1", QaStatus.Accepted), review("id-2", QaStatus.Pending))
        whenever(manager.getReviews(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any())).thenReturn(reviews)

        val result = controller.getNonSourceableReviews(null, null, null, null, 10, 0)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(2, result.body?.size)
    }

    @Test
    fun `getNonSourceableReviews filters by qaStatus via manager`() {
        val pending = listOf(review(qaStatus = QaStatus.Pending))
        whenever(manager.getReviews(null, null, null, QaStatus.Pending, 10, 0)).thenReturn(pending)

        val result = controller.getNonSourceableReviews(null, null, null, QaStatus.Pending, 10, 0)

        assertEquals(1, result.body?.size)
        assertEquals(QaStatus.Pending, result.body?.first()?.qaStatus)
    }

    @Test
    fun `getNonSourceableQueue delegates to manager getQueue`() {
        val pending = listOf(review(qaStatus = QaStatus.Pending))
        whenever(manager.getQueue()).thenReturn(pending)

        val result = controller.getNonSourceableQueue()

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(1, result.body?.size)
        verify(manager).getQueue()
    }
}
