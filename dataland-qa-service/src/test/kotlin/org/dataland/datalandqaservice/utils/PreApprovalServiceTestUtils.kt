package org.dataland.datalandqaservice.utils

import com.fasterxml.jackson.databind.JsonNode
import org.dataland.datalandqaservice.configurations.PreApprovalExemptFieldsConfig
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementSupportService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

/**
 * Shared fixtures and builder helpers used by [PreApprovalService] tests
 * (split across `PreApprovalServiceTest` and `PreApprovalCheckResultsTest`).
 */
object PreApprovalServiceTestUtils {
    val dummyReporter1: String = UUID.randomUUID().toString()
    val dummyReporter2: String = UUID.randomUUID().toString()
    val significanceCheckService = SignificanceCheckService()

    fun buildQaReport(
        reporterUserId: String,
        verdict: QaReportDataPointVerdict,
        dataPointId: String = UUID.randomUUID().toString(),
    ): DataPointQaReportEntity =
        DataPointQaReportEntity(
            qaReportId = UUID.randomUUID().toString(),
            comment = "",
            verdict = verdict,
            correctedData = null,
            dataPointId = dataPointId,
            dataPointType = MockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
            reporterUserId = reporterUserId,
            uploadTime = 1000L,
            active = true,
        )

    fun buildDataPointJudgementEntity(
        qaReports: List<DataPointQaReportEntity>,
        dataPointType: String = MockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
        dataPointId: String = UUID.randomUUID().toString(),
    ): DataPointJudgementEntity =
        DataPointJudgementEntity(
            dataPointType = dataPointType,
            dataPointId = dataPointId,
            qaReports = qaReports.toMutableList(),
            acceptedSource = null,
            reporterUserIdOfAcceptedQaReport = null,
            customValue = null,
            reasonForCustomDataPoint = null,
        )

    /**
     * Creates a mock [DatasetJudgementSupportService] that returns no live dataset.
     * Use this for tests that cover pre-existing behaviour unrelated to significance checks.
     */
    fun mockSupportServiceWithNoLiveDataset(): DatasetJudgementSupportService =
        mock<DatasetJudgementSupportService>().also {
            whenever(it.getDataPointsOfLatestActiveDataset(any(), any())).thenReturn(null)
        }

    /**
     * Builds a [PreApprovalService] pre-configured to skip the significance check
     * (no live dataset). Use this for all tests that verify pre-existing behaviour.
     */
    fun buildServiceWithoutLiveDataset(
        autoPreApprovalEnabled: Boolean,
        exemptFieldsConfig: PreApprovalExemptFieldsConfig = PreApprovalExemptFieldsConfig(),
    ): PreApprovalService =
        PreApprovalService(
            autoPreApprovalEnabled = autoPreApprovalEnabled,
            exemptFieldsConfig = exemptFieldsConfig,
            significanceCheckService = significanceCheckService,
            datasetJudgementSupportService = mockSupportServiceWithNoLiveDataset(),
        )

    @Suppress("LongParameterList", "kotlin:S107")
    fun buildServiceWithLiveDatasetForSignificanceCheck(
        originalDataPointId: String,
        liveDataPointId: String,
        originalValueNode: JsonNode?,
        liveValueNode: JsonNode?,
        baseTypeId: String,
        dpType: String,
        liveDataPointMap: Map<String, String> = mapOf(dpType to liveDataPointId),
        significanceCheckService: SignificanceCheckService = this.significanceCheckService,
    ): PreApprovalService {
        val supportServiceMock = mock<DatasetJudgementSupportService>()
        whenever(supportServiceMock.getDataPointsOfLatestActiveDataset(any(), any())).thenReturn(liveDataPointMap)
        whenever(supportServiceMock.getDataPointValueNode(originalDataPointId)).thenReturn(originalValueNode)
        whenever(supportServiceMock.getDataPointValueNode(liveDataPointId)).thenReturn(liveValueNode)
        whenever(supportServiceMock.resolveBaseTypeId(dpType)).thenReturn(baseTypeId)
        return PreApprovalService(
            autoPreApprovalEnabled = true,
            exemptFieldsConfig = PreApprovalExemptFieldsConfig(),
            significanceCheckService = significanceCheckService,
            datasetJudgementSupportService = supportServiceMock,
        ).also {
            it.patchConfig(PreApprovalConfig(samplingProbability = 0.0))
        }
    }

    fun runWorkflow(
        service: PreApprovalService,
        reports: List<DataPointQaReportEntity>,
        dataPointType: String = MockDatasetJudgementEntityForTest.DUMMY_DATA_POINT_TYPE,
    ): AcceptedDataPointSource? {
        val dataPoint = buildDataPointJudgementEntity(reports, dataPointType = dataPointType)
        val entity = MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()
        entity.dataPoints.clear()
        entity.dataPoints.add(dataPoint)
        return service
            .preApproveDataPoints(entity)
            .dataPoints
            .first()
            .acceptedSource
    }
}
