package org.dataland.datalandqaservice.repository

import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReviewRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DataPointQaReviewItemFilter
import org.dataland.datalandqaservice.utils.TestJwtSecurityConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(
    classes = [
        DatalandQaService::class,
        TestJwtSecurityConfig::class,
    ],
    properties = ["spring.profiles.active=nodb"],
)
class DataPointQaReviewRepositoryTest {
    @Autowired
    private lateinit var dataPointQaReviewRepository: DataPointQaReviewRepository

    private val dummyCompanyId = "dummy-company-id"
    private val dummyDataPointIdentifier = "dummy-identifier"
    private val dummyReportingPeriod = "2023"
    private val differentDataId = "different-data-id"

    private fun getDummyEntity(
        dataId: String = "dummy-data-id",
        companyId: String = dummyCompanyId,
        dataPointIdentifier: String = dummyDataPointIdentifier,
        reportingPeriod: String = dummyReportingPeriod,
        qaStatus: QaStatus = QaStatus.Accepted,
        timestamp: Long = System.currentTimeMillis(),
    ): DataPointQaReviewEntity {
        Thread.sleep(5)
        return DataPointQaReviewEntity(
            null, dataId, companyId, "dummy-company", dataPointIdentifier, reportingPeriod,
            timestamp, qaStatus, "dummy-user", "dummy-comment",
        )
    }

    private val dummyDataPointDimensions =
        BasicDataPointDimensions(
            companyId = dummyCompanyId,
            dataPointIdentifier = dummyDataPointIdentifier,
            reportingPeriod = dummyReportingPeriod,
        )

    private val emptyFilter =
        DataPointQaReviewItemFilter(
            companyId = null,
            dataPointIdentifier = null,
            reportingPeriod = null,
            qaStatus = null,
        )

    @Test
    fun `test getAllEntriesForTheReviewQueue`() {
        dataPointQaReviewRepository.save(getDummyEntity())
        dataPointQaReviewRepository.save(getDummyEntity(qaStatus = QaStatus.Rejected))
        val pendingEntity = dataPointQaReviewRepository.save(getDummyEntity(qaStatus = QaStatus.Pending))

        val result = dataPointQaReviewRepository.getAllEntriesForTheReviewQueue()

        assertEquals(1, result.size)
        assertEquals(pendingEntity, result.first())
    }

    @Test
    fun `check that setting the latest only flag works as expected`() {
        val firstEntity = dataPointQaReviewRepository.save(getDummyEntity(qaStatus = QaStatus.Pending))
        val secondEntity = dataPointQaReviewRepository.save(getDummyEntity())
        val latestResults = dataPointQaReviewRepository.findByFilterLatestOnly(emptyFilter, 10, 0)
        assertEquals(1, latestResults.size)
        assertEquals(secondEntity, latestResults.first())
        val allResults = dataPointQaReviewRepository.findByFilter(emptyFilter, 10, 0)
        assertEquals(2, allResults.size)
        assertEquals(secondEntity, allResults.first())
        assertEquals(firstEntity, allResults.last())
    }

    @Test
    fun `check that the currently active data ID is null if no accepted data sets exist`() {
        dataPointQaReviewRepository.save(getDummyEntity(qaStatus = QaStatus.Pending))
        dataPointQaReviewRepository.save(getDummyEntity(dataId = differentDataId, qaStatus = QaStatus.Pending))
        val results = dataPointQaReviewRepository.getDataIdOfCurrentlyActiveDataPoint(dummyDataPointDimensions)
        assertEquals(null, results)
    }

    @Test
    fun `check that the previously active data ID is returned if the later data set is rejected`() {
        val firstEntity = dataPointQaReviewRepository.save(getDummyEntity())
        dataPointQaReviewRepository.save(getDummyEntity(dataId = differentDataId))
        dataPointQaReviewRepository.save(getDummyEntity(dataId = differentDataId, qaStatus = QaStatus.Rejected))
        val results = dataPointQaReviewRepository.getDataIdOfCurrentlyActiveDataPoint(dummyDataPointDimensions)
        assertEquals(firstEntity.dataId, results)
    }
}
