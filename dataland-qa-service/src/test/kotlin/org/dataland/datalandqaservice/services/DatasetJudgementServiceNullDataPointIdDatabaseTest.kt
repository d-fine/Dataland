package org.dataland.datalandqaservice.services

import jakarta.persistence.EntityManager
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID

/**
 * Regression test running against a real database: a dataset judgement containing a data point judgement
 * without a data point id (e.g. a custom value for a data point that was never uploaded) must still be
 * readable. The QA report association joins on the nullable data_point_id column, for which Hibernate
 * may leave the collection null.
 */
@SpringBootTest(
    classes = [DatalandQaService::class],
    properties = ["spring.profiles.active=containerized-db"],
)
class DatasetJudgementServiceNullDataPointIdDatabaseTest(
    @Autowired private val datasetJudgementService: DatasetJudgementService,
    @Autowired private val datasetJudgementRepository: DatasetJudgementRepository,
    @Autowired private val dataPointQaReportRepository: DataPointQaReportRepository,
    @Autowired private val entityManager: EntityManager,
) : BaseIntegrationTest() {
    private val typeWithoutDataPoint = "dummyTypeWithoutDataPoint"
    private val typeWithDataPoint = "dummyTypeWithDataPoint"

    private fun persistJudgementMixingNullAndNonNullDataPointIds(): DatasetJudgementEntity {
        val dataPointId = UUID.randomUUID().toString()
        val qaReport =
            dataPointQaReportRepository.save(
                DataPointQaReportEntity(
                    qaReportId = UUID.randomUUID().toString(),
                    comment = "Looks good",
                    verdict = QaReportDataPointVerdict.QaAccepted,
                    correctedData = null,
                    dataPointId = dataPointId,
                    dataPointType = typeWithDataPoint,
                    reporterUserId = UUID.randomUUID().toString(),
                    uploadTime = 1234567890L,
                    active = true,
                ),
            )

        return datasetJudgementRepository.save(
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
                addAssociatedDataPoints(
                    DataPointJudgementEntity(
                        dataPointType = typeWithoutDataPoint,
                        dataPointId = null,
                        acceptedSource = AcceptedDataPointSource.entries[2],
                        reporterUserIdOfAcceptedQaReport = null,
                        customValue = "{\"value\": 42}",
                    ),
                )
                addAssociatedDataPoints(
                    DataPointJudgementEntity(
                        dataPointType = typeWithDataPoint,
                        dataPointId = dataPointId,
                        qaReports = mutableListOf(qaReport),
                        acceptedSource = null,
                        reporterUserIdOfAcceptedQaReport = null,
                        customValue = null,
                    ),
                )
            },
        )
    }

    @Test
    fun `getDatasetJudgementById succeeds when a data point judgement has no data point id`() {
        val datasetJudgementEntity = persistJudgementMixingNullAndNonNullDataPointIds()
        // BaseIntegrationTest runs in a single transaction. Flush and clear the persistence context so the
        // judgement is re-loaded from the database by Hibernate (which is where qaReports becomes null)
        // instead of returning the in-memory instances created above.
        entityManager.flush()
        entityManager.clear()

        val response = datasetJudgementService.getDatasetJudgementById(datasetJudgementEntity.dataSetJudgementId)

        assertEquals(datasetJudgementEntity.dataSetJudgementId.toString(), response.dataSetJudgementId)

        val dataPointWithoutId = response.dataPoints[typeWithoutDataPoint]
        assertNotNull(dataPointWithoutId)
        assertNull(dataPointWithoutId!!.dataPointId)
        assertTrue(dataPointWithoutId.qaReports.isEmpty())
        assertNull(dataPointWithoutId.acceptedQaReport)

        val dataPointWithId = response.dataPoints[typeWithDataPoint]
        assertNotNull(dataPointWithId)
        assertEquals(1, dataPointWithId!!.qaReports.size)
    }
}
