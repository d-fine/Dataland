package org.dataland.datalandqaservice.controller

import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller.DatasetReviewController
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.ReviewDetailsPatch
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.util.UUID

class DatasetReviewControllerTest {
    private val datasetJudgementService: DatasetJudgementService = mock()
    private val controller = DatasetReviewController(datasetJudgementService)

    private fun dummyResponse(dataSetReviewId: UUID = UUID.randomUUID()): DatasetJudgementResponse =
        DatasetJudgementResponse(
            dataSetReviewId = dataSetReviewId.toString(),
            datasetId = UUID.randomUUID().toString(),
            companyId = UUID.randomUUID().toString(),
            dataType = "sfdr",
            reportingPeriod = "2025",
            reviewState = DatasetReviewState.Pending,
            qaJudgeUserId = UUID.randomUUID().toString(),
            qaJudgeUserName = "Dummy User",
            qaReporters = emptyList(),
            dataPoints = emptyMap(),
        )

    @Test
    fun `getDatasetReviewsById delegates to service and returns expected body`() {
        val datasetReviewId = UUID.randomUUID()
        val response = dummyResponse(datasetReviewId)

        whenever(datasetJudgementService.getDatasetReviewById(datasetReviewId))
            .thenReturn(response)

        val result = controller.getDatasetReview(datasetReviewId.toString())

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(response, result.body)
        verify(datasetJudgementService).getDatasetReviewById(datasetReviewId)
    }

    @Test
    fun `getDatasetReviewsByDatasetId delegates to service and returns expected body`() {
        val datasetId = UUID.randomUUID()
        val response = dummyResponse()

        whenever(datasetJudgementService.getDatasetReviewsByDatasetId(datasetId))
            .thenReturn(listOf(response))

        val result = controller.getDatasetReviewsByDatasetId(datasetId.toString())

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(listOf(response), result.body)

        val uuidCaptor = argumentCaptor<UUID>()
        verify(datasetJudgementService).getDatasetReviewsByDatasetId(uuidCaptor.capture())
        assertEquals(datasetId, uuidCaptor.firstValue)
    }

    @Test
    fun `postDatasetReview delegates to service`() {
        val datasetId = UUID.randomUUID()
        val expectedResponse = dummyResponse()

        whenever(datasetJudgementService.postDatasetReview(datasetId))
            .thenReturn(expectedResponse)

        val result = controller.postDatasetReview(datasetId.toString())

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(expectedResponse, result.body)
        verify(datasetJudgementService).postDatasetReview(datasetId)
    }

    @Test
    fun `setReviewer delegates to service`() {
        val id = UUID.randomUUID()
        val serviceResponse = mock<DatasetJudgementResponse>()

        whenever(datasetJudgementService.setReviewer(id))
            .thenReturn(serviceResponse)

        val result = controller.setReviewer(id.toString())

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(serviceResponse, result.body)

        val idCaptor = argumentCaptor<UUID>()
        verify(datasetJudgementService).setReviewer(idCaptor.capture())
        assertEquals(id, idCaptor.firstValue)
    }

    @Test
    fun `setReviewState delegates to service`() {
        val id = UUID.randomUUID()
        val state = DatasetReviewState.Finished
        val serviceResponse = mock<DatasetJudgementResponse>()

        whenever(datasetJudgementService.setReviewState(id, state))
            .thenReturn(serviceResponse)

        val result = controller.setReviewState(id.toString(), state)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(serviceResponse, result.body)

        val idCaptor = argumentCaptor<UUID>()
        verify(datasetJudgementService).setReviewState(idCaptor.capture(), eq(state))
        assertEquals(id, idCaptor.firstValue)
    }

    @Test
    fun `patchReviewDetails delegates to service with all params`() {
        val reviewId = UUID.randomUUID()
        val dataPointType = "dummyType"
        val patch = ReviewDetailsPatch(AcceptedDataPointSource.Qa, UUID.randomUUID().toString(), null)
        val serviceResponse = mock<DatasetJudgementResponse>()

        whenever(
            datasetJudgementService.patchReviewDetails(reviewId, dataPointType, patch),
        ).thenReturn(serviceResponse)

        val result = controller.patchReviewDetails(reviewId.toString(), dataPointType, patch)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(serviceResponse, result.body)

        val idCaptor = argumentCaptor<UUID>()
        verify(datasetJudgementService).patchReviewDetails(
            idCaptor.capture(),
            eq(dataPointType),
            eq(patch),
        )
        assertEquals(reviewId, idCaptor.firstValue)
    }
}
