package org.dataland.datalandqaservice.controller

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller.DatasetReviewController
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.util.UUID

class DatasetReviewControllerTest {
    private val datasetReviewService: DatasetReviewService = mock()
    private val controller = DatasetReviewController(datasetReviewService)

    private fun dummyResponse(dataSetReviewId: UUID = UUID.randomUUID()): DatasetReviewResponse =
        DatasetReviewResponse(
            dataSetReviewId = dataSetReviewId.toString(),
            datasetId = UUID.randomUUID().toString(),
            companyId = UUID.randomUUID().toString(),
            dataType = "sfdr",
            reportingPeriod = "2025",
            reviewState = DatasetReviewState.Pending,
            qaJudgeUserId = UUID.randomUUID().toString(),
            qaJudgeUserName = "Dummy User",
            qaReporterCompanies = emptyList(),
            dataPoints = emptyMap(),
        )

    @Test
    fun `getDatasetReviewsById delegates to service and returns expected body`() {
        val datasetReviewId = UUID.randomUUID()
        val response = dummyResponse(datasetReviewId)

        whenever(datasetReviewService.getDatasetReviewById(datasetReviewId))
            .thenReturn(response)

        val result = controller.getDatasetReview(datasetReviewId.toString())

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(response, result.body)
        verify(datasetReviewService).getDatasetReviewById(datasetReviewId)
    }

    @Test
    fun `getDatasetReviewsByDatasetId delegates to service and returns expected body`() {
        val datasetId = UUID.randomUUID()
        val response = dummyResponse()

        whenever(datasetReviewService.getDatasetReviewsByDatasetId(datasetId))
            .thenReturn(listOf(response))

        val result = controller.getDatasetReviewsByDatasetId(datasetId.toString())

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(listOf(response), result.body)

        val uuidCaptor = argumentCaptor<UUID>()
        verify(datasetReviewService).getDatasetReviewsByDatasetId(uuidCaptor.capture())
        assertEquals(datasetId, uuidCaptor.firstValue)
    }

    @Test
    fun `postDatasetReview delegates to service`() {
        val datasetId = UUID.randomUUID()
        val expectedResponse = dummyResponse()

        whenever(datasetReviewService.postDatasetReview(datasetId))
            .thenReturn(expectedResponse)

        val result = controller.postDatasetReview(datasetId.toString())

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(expectedResponse, result.body)
        verify(datasetReviewService).postDatasetReview(datasetId)
    }

    @Test
    fun `setReviewer delegates to service`() {
        val id = UUID.randomUUID()
        val serviceResponse = mock<DatasetReviewResponse>()

        whenever(datasetReviewService.setReviewer(id))
            .thenReturn(serviceResponse)

        val result = controller.setReviewer(id.toString())

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(serviceResponse, result.body)

        val idCaptor = argumentCaptor<UUID>()
        verify(datasetReviewService).setReviewer(idCaptor.capture())
        assertEquals(id, idCaptor.firstValue)
    }

    @Test
    fun `setReviewState delegates to service`() {
        val id = UUID.randomUUID()
        val state = DatasetReviewState.Finished
        val serviceResponse = mock<DatasetReviewResponse>()

        whenever(datasetReviewService.setReviewState(id, state))
            .thenReturn(serviceResponse)

        val result = controller.setReviewState(id.toString(), state)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(serviceResponse, result.body)

        val idCaptor = argumentCaptor<UUID>()
        verify(datasetReviewService).setReviewState(idCaptor.capture(), eq(state))
        assertEquals(id, idCaptor.firstValue)
    }

    @Test
    fun `setAcceptedSource delegates to service with all params`() {
        val reviewId = UUID.randomUUID()
        val dataPointType = "dummyType"
        val acceptedSource = AcceptedDataPointSource.Qa
        val companyId = UUID.randomUUID().toString()
        val serviceResponse = mock<DatasetReviewResponse>()

        whenever(
            datasetReviewService.setAcceptedSource(reviewId, dataPointType, acceptedSource, companyId, null),
        ).thenReturn(serviceResponse)

        val result = controller.setAcceptedSource(reviewId.toString(), dataPointType, acceptedSource, companyId, null)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(serviceResponse, result.body)

        val idCaptor = argumentCaptor<UUID>()
        verify(datasetReviewService).setAcceptedSource(
            idCaptor.capture(),
            eq(dataPointType),
            eq(acceptedSource),
            eq(companyId),
            isNull(),
        )
        assertEquals(reviewId, idCaptor.firstValue)
    }
}
