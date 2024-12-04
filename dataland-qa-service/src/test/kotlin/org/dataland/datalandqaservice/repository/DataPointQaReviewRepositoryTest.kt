package org.dataland.datalandqaservice.repository

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReviewRepository
import org.dataland.datalandqaservice.utils.TestJwtSecurityConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

    @Test
    fun `test getAllEntriesForTheReviewQueue`() {
        // Given
        val entity =
            DataPointQaReviewEntity(
                dataId = "1",
                companyId = "1",
                dataPointIdentifier = "identifier",
                reportingPeriod = "2023-01",
                qaStatus = QaStatus.Pending,
                timestamp = System.currentTimeMillis(),
                companyName = "company",
                triggeringUserId = "user",
                comment = "comment",
            )
        dataPointQaReviewRepository.save(entity)

        // When
        val result = dataPointQaReviewRepository.getAllEntriesForTheReviewQueue()

        // Then
        assertEquals(1, result.size)
        assertEquals(QaStatus.Pending, result[0].qaStatus)
    }
}
