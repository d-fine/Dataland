package org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetJudgementRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

/**
 * Integration test running against a real database, verifying that deleting a pending dataset
 * judgement removes only the judgement's own entities and does not affect independently owned
 * QA report rows.
 */
@SpringBootTest(
    classes = [DatalandQaService::class],
    properties = ["spring.profiles.active=containerized-db"],
)
class DatasetJudgementServiceDeletionDatabaseSafetyTest(
    @Autowired private val datasetJudgementService: DatasetJudgementService,
    @Autowired private val datasetJudgementRepository: DatasetJudgementRepository,
    @Autowired private val dataPointQaReportRepository: DataPointQaReportRepository,
) : BaseIntegrationTest() {
    private lateinit var dataPointId: String
    private lateinit var qaReportEntity: DataPointQaReportEntity
    private lateinit var datasetJudgementEntity: DatasetJudgementEntity

    @BeforeEach
    fun setup() {
        dataPointId = UUID.randomUUID().toString()

        qaReportEntity =
            dataPointQaReportRepository.save(
                DataPointQaReportEntity(
                    qaReportId = UUID.randomUUID().toString(),
                    comment = "Looks good",
                    verdict = QaReportDataPointVerdict.QaAccepted,
                    correctedData = null,
                    dataPointId = dataPointId,
                    dataPointType = "dummyType",
                    reporterUserId = UUID.randomUUID().toString(),
                    uploadTime = 1234567890L,
                    active = true,
                ),
            )

        val dataPointJudgementEntity =
            DataPointJudgementEntity(
                dataPointType = "dummyType",
                dataPointId = dataPointId,
                qaReports = mutableListOf(qaReportEntity),
                acceptedSource = AcceptedDataPointSource.Qa,
                reporterUserIdOfAcceptedQaReport = UUID.fromString(qaReportEntity.reporterUserId),
                customValue = null,
            )

        datasetJudgementEntity =
            datasetJudgementRepository.save(
                DatasetJudgementEntity(
                    dataSetJudgementId = UUID.randomUUID(),
                    datasetId = UUID.randomUUID(),
                    companyId = UUID.randomUUID(),
                    dataType = DataTypeEnum.sfdr,
                    reportingPeriod = "2024",
                    judgementState = DatasetJudgementState.Pending,
                    qaJudgeUserId = UUID.randomUUID(),
                    qaJudgeUserName = "Dummy Judge",
                    qaReporters = mutableListOf(),
                    dataPoints = mutableListOf(),
                ).apply {
                    addAssociatedDataPoints(dataPointJudgementEntity)
                },
            )
    }

    @Test
    fun `deleteDatasetJudgement removes the judgement and its data points but keeps QA reports`() {
        datasetJudgementService.deleteDatasetJudgement(datasetJudgementEntity.dataSetJudgementId)
        val qaReportById = dataPointQaReportRepository.findById(qaReportEntity.qaReportId)

        assertFalse(datasetJudgementRepository.findById(datasetJudgementEntity.dataSetJudgementId).isPresent)
        assertTrue(qaReportById.isPresent)
        assertEquals(
            qaReportEntity,
            qaReportById.get(),
        )
    }

    @Test
    fun `deleteDatasetJudgement does not delete the datasetJudgementEntity when it throws the ConflictApiException`() {
        datasetJudgementEntity.judgementState = DatasetJudgementState.FinishedWithDatasetAcceptance
        datasetJudgementRepository.save(datasetJudgementEntity)

        assertTrue(datasetJudgementRepository.findById(datasetJudgementEntity.dataSetJudgementId).isPresent)
    }
}
