package org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaConfigEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaConfigRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.dataland.datalandqaservice.utils.MockDatasetJudgementEntityForTest
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildDataPointJudgementEntity
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.buildQaReport
import org.dataland.datalandqaservice.utils.PreApprovalServiceTestUtils.dummyReporter1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional

/**
 * Tests that the [PreApprovalConfig] is correctly loaded from (or found missing in) the database on
 * [PreApprovalService] startup, and that the service behaves correctly when the config is unavailable.
 */
class PreApprovalConfigLoadingTest {
    @Test
    fun `config is loaded from the database on startup`() {
        val seededConfig =
            PreApprovalConfig(
                exemptFields = mapOf(DataTypeEnum.sfdr to setOf("some-field")),
                samplingProbability = 0.33,
                decimalRelativeThreshold = 0.25,
                integerAbsoluteThreshold = 3,
                autoPreApprovalEnabled = false,
                submitUserId = "some-previous-submitter",
            )
        val repository = PreApprovalServiceTestUtils.buildQaConfigRepositoryMock(seededConfig)
        val service =
            PreApprovalService(
                qaConfigRepository = repository,
                significanceCheckService = PreApprovalServiceTestUtils.significanceCheckService,
                datasetJudgementSupportService = PreApprovalServiceTestUtils.mockSupportServiceWithNoLiveDataset(),
            )

        service.initializeConfig()

        assertEquals(seededConfig, service.config)
    }

    @Test
    fun `config remains unavailable and startup does not throw when no row exists in the database`() {
        val repository = mock<QaConfigRepository>()
        whenever(repository.findById(QaConfigEntity.QA_CONFIG_SINGLETON_ID)).thenReturn(Optional.empty())
        val service =
            PreApprovalService(
                qaConfigRepository = repository,
                significanceCheckService = PreApprovalServiceTestUtils.significanceCheckService,
                datasetJudgementSupportService = PreApprovalServiceTestUtils.mockSupportServiceWithNoLiveDataset(),
            )

        service.initializeConfig()

        // However, accessing the PreApprovalConfig should throw an error.
        assertThrows<InternalServerErrorApiException> { service.config }
    }

    @Test
    fun `preApproveDataPoints is a no-op and does not throw when the config is unavailable`() {
        val repository = mock<QaConfigRepository>()
        whenever(repository.findById(QaConfigEntity.QA_CONFIG_SINGLETON_ID)).thenReturn(Optional.empty())
        val service =
            PreApprovalService(
                qaConfigRepository = repository,
                significanceCheckService = PreApprovalServiceTestUtils.significanceCheckService,
                datasetJudgementSupportService = PreApprovalServiceTestUtils.mockSupportServiceWithNoLiveDataset(),
            )
        service.initializeConfig()

        val dataPoint =
            buildDataPointJudgementEntity(
                qaReports =
                    listOf(
                        buildQaReport(
                            reporterUserId = dummyReporter1,
                            verdict = QaReportDataPointVerdict.QaAccepted,
                        ),
                    ),
            )
        val datasetJudgementEntity =
            MockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity().also { entity ->
                entity.dataPoints.clear()
                entity.dataPoints.add(dataPoint)
            }

        val result = service.preApproveDataPoints(datasetJudgementEntity)

        assertEquals(datasetJudgementEntity, result)
        assertEquals(null, dataPoint.acceptedSource)
    }
}
