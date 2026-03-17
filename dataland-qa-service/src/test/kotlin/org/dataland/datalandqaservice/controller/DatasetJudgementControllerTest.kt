package org.dataland.datalandqaservice.controller

import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller.DatasetJudgementController
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.JudgementDetailsPatch
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

class DatasetJudgementControllerTest {
    private val datasetJudgementService: DatasetJudgementService = mock()
    private val controller = DatasetJudgementController(datasetJudgementService)

    private fun dummyResponse(dataSetJudgementId: UUID = UUID.randomUUID()): DatasetJudgementResponse =
        DatasetJudgementResponse(
            dataSetJudgementId = dataSetJudgementId.toString(),
            datasetId = UUID.randomUUID().toString(),
            companyId = UUID.randomUUID().toString(),
            dataType = "sfdr",
            reportingPeriod = "2025",
            judgementState = DatasetJudgementState.Pending,
            qaJudgeUserId = UUID.randomUUID().toString(),
            qaJudgeUserName = "Dummy User",
            qaReporters = emptyList(),
            dataPoints = emptyMap(),
        )

    @Test
    fun `getDatasetJudgementsById delegates to service and returns expected body`() {
        val datasetJudgementId = UUID.randomUUID()
        val response = dummyResponse(datasetJudgementId)

        whenever(datasetJudgementService.getDatasetJudgementById(datasetJudgementId))
            .thenReturn(response)

        val result = controller.getDatasetJudgement(datasetJudgementId.toString())

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(response, result.body)
        verify(datasetJudgementService).getDatasetJudgementById(datasetJudgementId)
    }

    @Test
    fun `getDatasetJudgementsByDatasetId delegates to service and returns expected body`() {
        val datasetId = UUID.randomUUID()
        val response = dummyResponse()

        whenever(datasetJudgementService.getDatasetJudgementsByDatasetId(datasetId))
            .thenReturn(listOf(response))

        val result = controller.getDatasetJudgementsByDatasetId(datasetId.toString())

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(listOf(response), result.body)

        val uuidCaptor = argumentCaptor<UUID>()
        verify(datasetJudgementService).getDatasetJudgementsByDatasetId(uuidCaptor.capture())
        assertEquals(datasetId, uuidCaptor.firstValue)
    }

    @Test
    fun `postDatasetJudgement delegates to service`() {
        val datasetId = UUID.randomUUID()
        val expectedResponse = dummyResponse()

        whenever(datasetJudgementService.postDatasetJudgement(datasetId))
            .thenReturn(expectedResponse)

        val result = controller.postDatasetJudgement(datasetId.toString())

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(expectedResponse, result.body)
        verify(datasetJudgementService).postDatasetJudgement(datasetId)
    }

    @Test
    fun `setJudge delegates to service`() {
        val id = UUID.randomUUID()
        val serviceResponse = mock<DatasetJudgementResponse>()

        whenever(datasetJudgementService.setJudge(id))
            .thenReturn(serviceResponse)

        val result = controller.setJudge(id.toString())

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(serviceResponse, result.body)

        val idCaptor = argumentCaptor<UUID>()
        verify(datasetJudgementService).setJudge(idCaptor.capture())
        assertEquals(id, idCaptor.firstValue)
    }

    @Test
    fun `setJudgementState delegates to service`() {
        val id = UUID.randomUUID()
        val state = DatasetJudgementState.Finished
        val serviceResponse = mock<DatasetJudgementResponse>()

        whenever(datasetJudgementService.setJudgementState(id, state))
            .thenReturn(serviceResponse)

        val result = controller.setJudgementState(id.toString(), state)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(serviceResponse, result.body)

        val idCaptor = argumentCaptor<UUID>()
        verify(datasetJudgementService).setJudgementState(idCaptor.capture(), eq(state))
        assertEquals(id, idCaptor.firstValue)
    }

    @Test
    fun `patchJudgementDetails delegates to service with all params`() {
        val judgeId = UUID.randomUUID()
        val dataPointType = "dummyType"
        val patch = JudgementDetailsPatch(AcceptedDataPointSource.Qa, UUID.randomUUID().toString(), null)
        val serviceResponse = mock<DatasetJudgementResponse>()

        whenever(
            datasetJudgementService.patchJudgementDetails(judgeId, dataPointType, patch),
        ).thenReturn(serviceResponse)

        val result = controller.patchJudgementDetails(judgeId.toString(), dataPointType, patch)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(serviceResponse, result.body)

        val idCaptor = argumentCaptor<UUID>()
        verify(datasetJudgementService).patchJudgementDetails(
            idCaptor.capture(),
            eq(dataPointType),
            eq(patch),
        )
        assertEquals(judgeId, idCaptor.firstValue)
    }
}
