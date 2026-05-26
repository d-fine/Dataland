package org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.model.UploadedDataPoint
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReviewManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementFinalizationService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.dataland.datalandqaservice.utils.MockDatasetJudgementEntityForTest
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.util.UUID

class DatasetJudgementFinalizationServiceTest {
    private val dataPointControllerApi = mock<DataPointControllerApi>()
    private val dataPointQaReviewManager = mock<DataPointQaReviewManager>()
    private val qaReviewManager = mock<QaReviewManager>()

    private val service =
        DatasetJudgementFinalizationService(
            dataPointControllerApi,
            dataPointQaReviewManager,
            qaReviewManager,
        )

    private lateinit var dummyDatasetJudgement: DatasetJudgementEntity

    @BeforeEach
    fun setup() {
        AuthenticationMock.mockSecurityContext(
            "data.admin@example.com",
            MockDatasetJudgementEntityForTest.dummyUserId.toString(),
        )
        dummyDatasetJudgement = MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()
    }

    private fun verifyReviewTasksCaptor(
        tasksCaptor: KArgumentCaptor<List<DataPointQaReviewManager.ReviewDataPointTask>>,
        expectedQaStatus: QaStatus,
    ) {
        val expectedDataPointIds = dummyDatasetJudgement.dataPoints.map { it.dataPointId }.toSet()
        assertEquals(dummyDatasetJudgement.dataPoints.size, tasksCaptor.firstValue.size)
        assertTrue(tasksCaptor.firstValue.all { it.qaStatus == expectedQaStatus })
        assertTrue(tasksCaptor.firstValue.all { it.triggeringUserId == MockDatasetJudgementEntityForTest.dummyUserId.toString() })
        assertEquals(expectedDataPointIds, tasksCaptor.firstValue.map { it.dataPointId }.toSet())
    }

    private fun verifyUploadedDataPoint(expectedDataPoint: String) {
        val uploadCaptor = argumentCaptor<UploadedDataPoint>()
        verify(dataPointControllerApi).postDataPoint(uploadCaptor.capture(), any())
        assertEquals(expectedDataPoint, uploadCaptor.firstValue.dataPoint)
        assertEquals(dummyDatasetJudgement.companyId.toString(), uploadCaptor.firstValue.companyId)
        assertEquals(dummyDatasetJudgement.reportingPeriod, uploadCaptor.firstValue.reportingPeriod)
    }

    @Test
    fun `handleRejection calls changeQaStatus with Rejected and with overwriteDataPointQaStatus true`() {
        service.handleRejection(dummyDatasetJudgement)

        verify(qaReviewManager).changeQaStatus(
            dataId = dummyDatasetJudgement.datasetId.toString(),
            qaStatus = QaStatus.Rejected,
            comment = null,
            overwriteDataPointQaStatus = true,
        )
    }

    @Test
    fun `handleAcceptance throws when a data point has no accepted source`() {
        assertThrows<InvalidInputApiException> {
            service.handleAcceptance(dummyDatasetJudgement)
        }

        verify(dataPointQaReviewManager, never()).reviewDataPoints(any())
        verify(qaReviewManager, never()).changeQaStatus(any(), any(), any(), any())
    }

    @Test
    fun `handleAcceptance with Original source does not upload a replacement and sets data point to Accepted`() {
        dummyDatasetJudgement.dataPoints.forEach { it.acceptedSource = AcceptedDataPointSource.Original }

        service.handleAcceptance(dummyDatasetJudgement)

        verify(dataPointControllerApi, never()).postDataPoint(any(), any())

        val tasksCaptor = argumentCaptor<List<DataPointQaReviewManager.ReviewDataPointTask>>()
        verify(dataPointQaReviewManager).reviewDataPoints(tasksCaptor.capture())
        verifyReviewTasksCaptor(tasksCaptor, QaStatus.Accepted)

        verify(qaReviewManager).changeQaStatus(
            dataId = dummyDatasetJudgement.datasetId.toString(),
            qaStatus = QaStatus.Accepted,
            comment = null,
            overwriteDataPointQaStatus = false,
        )
    }

    @Test
    fun `handleAcceptance with Custom source uploads the custom value and sets data point to Rejected`() {
        dummyDatasetJudgement.dataPoints.forEach {
            it.acceptedSource = AcceptedDataPointSource.Custom
            it.customValue = MockDatasetJudgementEntityForTest.CUSTOM_VALUE
        }

        service.handleAcceptance(dummyDatasetJudgement)

        verifyUploadedDataPoint(MockDatasetJudgementEntityForTest.CUSTOM_VALUE)

        verify(dataPointQaReviewManager, never()).reviewDataPoints(any())
        val tasksCaptor = argumentCaptor<List<DataPointQaReviewManager.ReviewDataPointTask>>()
        verify(dataPointQaReviewManager).saveDataPointReviewEntitiesOnly(tasksCaptor.capture())
        verifyReviewTasksCaptor(tasksCaptor, QaStatus.Rejected)
    }

    @Test
    fun `handleAcceptance with Custom source throws when customValue is null`() {
        dummyDatasetJudgement.dataPoints.forEach {
            it.acceptedSource = AcceptedDataPointSource.Custom
            it.customValue = null
        }

        assertThrows<InvalidInputApiException> {
            service.handleAcceptance(dummyDatasetJudgement)
        }

        verify(dataPointControllerApi, never()).postDataPoint(any(), any())
        verify(qaReviewManager, never()).changeQaStatus(any(), any(), any(), any())
    }

    @Test
    fun `handleAcceptance with Qa source uploads the corrected data and sets data point to Rejected`() {
        val correctedData = """{"value": 99}"""
        dummyDatasetJudgement.dataPoints.forEach { dataPoint ->
            dataPoint.acceptedSource = AcceptedDataPointSource.Qa
            dataPoint.reporterUserIdOfAcceptedQaReport = MockDatasetJudgementEntityForTest.dummyUserId
            dataPoint.qaReports.forEach { it.correctedData = correctedData }
        }

        service.handleAcceptance(dummyDatasetJudgement)

        verifyUploadedDataPoint(correctedData)

        verify(dataPointQaReviewManager, never()).reviewDataPoints(any())
        val tasksCaptor = argumentCaptor<List<DataPointQaReviewManager.ReviewDataPointTask>>()
        verify(dataPointQaReviewManager).saveDataPointReviewEntitiesOnly(tasksCaptor.capture())
        verifyReviewTasksCaptor(tasksCaptor, QaStatus.Rejected)
    }

    @Test
    fun `handleAcceptance with Qa source throws when no matching QA report is found`() {
        dummyDatasetJudgement.dataPoints.forEach { dataPoint ->
            dataPoint.acceptedSource = AcceptedDataPointSource.Qa
            dataPoint.reporterUserIdOfAcceptedQaReport = UUID.randomUUID()
        }

        assertThrows<InvalidInputApiException> {
            service.handleAcceptance(dummyDatasetJudgement)
        }

        verify(dataPointControllerApi, never()).postDataPoint(any(), any())
        verify(qaReviewManager, never()).changeQaStatus(any(), any(), any(), any())
    }

    @Test
    fun `handleAcceptance with Qa source throws when the accepted QA report has no corrected data`() {
        dummyDatasetJudgement.dataPoints.forEach { dataPoint ->
            dataPoint.acceptedSource = AcceptedDataPointSource.Qa
            dataPoint.reporterUserIdOfAcceptedQaReport = MockDatasetJudgementEntityForTest.dummyUserId
            dataPoint.qaReports.forEach { it.correctedData = null }
        }

        assertThrows<InvalidInputApiException> {
            service.handleAcceptance(dummyDatasetJudgement)
        }

        verify(dataPointControllerApi, never()).postDataPoint(any(), any())
        verify(qaReviewManager, never()).changeQaStatus(any(), any(), any(), any())
    }

    @Test
    fun `handleAcceptance with mixed sources routes each data point to the correct review path`() {
        val correctedData = """{"value": 99}"""

        dummyDatasetJudgement.dataPoints.forEach { it.acceptedSource = AcceptedDataPointSource.Original }

        dummyDatasetJudgement.dataPoints.add(
            MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity().dataPoints.first().also {
                it.acceptedSource = AcceptedDataPointSource.Custom
                it.customValue = MockDatasetJudgementEntityForTest.CUSTOM_VALUE
            },
        )

        dummyDatasetJudgement.dataPoints.add(
            MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity().dataPoints.first().also {
                it.acceptedSource = AcceptedDataPointSource.Qa
                it.reporterUserIdOfAcceptedQaReport = MockDatasetJudgementEntityForTest.dummyUserId
                it.qaReports.first().correctedData = correctedData
                it.qaReports.first().verdict = QaReportDataPointVerdict.QaRejected
            },
        )

        service.handleAcceptance(dummyDatasetJudgement)

        val acceptedTasksCaptor = argumentCaptor<List<DataPointQaReviewManager.ReviewDataPointTask>>()
        verify(dataPointQaReviewManager).reviewDataPoints(acceptedTasksCaptor.capture())
        assertEquals(1, acceptedTasksCaptor.firstValue.size)

        val rejectedTasksCaptor = argumentCaptor<List<DataPointQaReviewManager.ReviewDataPointTask>>()
        verify(dataPointQaReviewManager).saveDataPointReviewEntitiesOnly(rejectedTasksCaptor.capture())
        assertEquals(2, rejectedTasksCaptor.firstValue.size)

        verify(dataPointControllerApi, org.mockito.kotlin.times(2)).postDataPoint(any(), any())
    }
}
