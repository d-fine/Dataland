package org.dataland.datalandqaservice.controller

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller.DatasetReviewController
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.org.dataland.datalandqaservice.model.DatasetReview
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewService
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
    private val datasetReviewService: DatasetReviewService = mock()
    private val controller = DatasetReviewController(datasetReviewService)

    @Test
    fun `getDatasetReviewsByDatasetId delegates to service and returns expected body`() {
        val datasetId = UUID.randomUUID()
        val response =
            DatasetReviewResponse(
                dataSetReviewId = UUID.randomUUID().toString(),
                datasetId = datasetId.toString(),
                companyId = UUID.randomUUID().toString(),
                reviewerUserId = UUID.randomUUID().toString(),
                reviewerUserName = "Dummy User",
                qaReports = emptySet(),
                dataType = "sfdr",
                reportingPeriod = "2025",
                status = DatasetReviewState.Pending,
                preapprovedDataPointIds = emptySet(),
                approvedQaReportIds = emptyMap(),
                approvedDataPointIds = emptyMap(),
                approvedCustomDataPointIds = emptyMap(),
            )

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
        val dataType = "sfdr"
        val reportingPeriod = "2026"
        val request =
            DatasetReview(
                datasetId = UUID.randomUUID().toString(),
                companyId = UUID.randomUUID().toString(),
                dataType = dataType,
                reportingPeriod = reportingPeriod,
            )
        val expectedResponse =
            DatasetReviewResponse(
                dataSetReviewId = UUID.randomUUID().toString(),
                datasetId = request.datasetId,
                companyId = request.companyId,
                reviewerUserId = UUID.randomUUID().toString(),
                reviewerUserName = "Dummy User",
                qaReports = emptySet(),
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                status = DatasetReviewState.Pending,
                preapprovedDataPointIds = emptySet(),
                approvedQaReportIds = emptyMap(),
                approvedDataPointIds = emptyMap(),
                approvedCustomDataPointIds = emptyMap(),
            )

        whenever(datasetReviewService.createDatasetReview(request))
            .thenReturn(expectedResponse)

        val result = controller.postDatasetReview(request)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(expectedResponse, result.body)
        verify(datasetReviewService).createDatasetReview(request)
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
    fun `setState delegates to service`() {
        val id = UUID.randomUUID()
        val state = DatasetReviewState.Finished
        val serviceResponse = mock<DatasetReviewResponse>()

        whenever(datasetReviewService.setState(id, state))
            .thenReturn(serviceResponse)

        val result = controller.setState(id.toString(), state)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(serviceResponse, result.body)

        val idCaptor = argumentCaptor<UUID>()
        verify(datasetReviewService).setState(idCaptor.capture(), eq(state))
        assertEquals(id, idCaptor.firstValue)
    }

    @Test
    fun `acceptOriginalDatapoint delegates to service`() {
        val reviewId = UUID.randomUUID()
        val dataPointId = UUID.randomUUID()
        val serviceResponse = mock<DatasetReviewResponse>()

        whenever(datasetReviewService.acceptOriginalDatapoint(reviewId, dataPointId))
            .thenReturn(serviceResponse)

        val result = controller.acceptOriginalDatapoint(reviewId.toString(), dataPointId.toString())

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(serviceResponse, result.body)

        val reviewIdCaptor = argumentCaptor<UUID>()
        val dataPointIdCaptor = argumentCaptor<UUID>()
        verify(datasetReviewService).acceptOriginalDatapoint(reviewIdCaptor.capture(), dataPointIdCaptor.capture())
        assertEquals(reviewId, reviewIdCaptor.firstValue)
        assertEquals(dataPointId, dataPointIdCaptor.firstValue)
    }

    @Test
    fun `acceptQaReport delegates to service`() {
        val reviewId = UUID.randomUUID()
        val qaReportId = UUID.randomUUID()
        val serviceResponse = mock<DatasetReviewResponse>()

        whenever(datasetReviewService.acceptQaReport(reviewId, qaReportId))
            .thenReturn(serviceResponse)

        val result = controller.acceptQaReport(reviewId.toString(), qaReportId.toString())

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(serviceResponse, result.body)

        val reviewIdCaptor = argumentCaptor<UUID>()
        val qaReportIdCaptor = argumentCaptor<UUID>()
        verify(datasetReviewService).acceptQaReport(reviewIdCaptor.capture(), qaReportIdCaptor.capture())
        assertEquals(reviewId, reviewIdCaptor.firstValue)
        assertEquals(qaReportId, qaReportIdCaptor.firstValue)
    }

    @Test
    fun `acceptCustomDataPoint delegates to service`() {
        val reviewId = UUID.randomUUID()
        val dataPoint = """{"some": "json"}"""
        val dataPointType = "dummyType"
        val serviceResponse = mock<DatasetReviewResponse>()

        whenever(datasetReviewService.acceptCustomDataPoint(reviewId, dataPoint, dataPointType))
            .thenReturn(serviceResponse)

        val result = controller.acceptCustomDataPoint(reviewId.toString(), dataPoint, dataPointType)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(serviceResponse, result.body)

        val idCaptor = argumentCaptor<UUID>()
        verify(datasetReviewService)
            .acceptCustomDataPoint(idCaptor.capture(), eq(dataPoint), eq(dataPointType))
        assertEquals(reviewId, idCaptor.firstValue)
    }
}
