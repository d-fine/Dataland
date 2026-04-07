package org.dataland.datalandqaservice.repository

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.NonSourceableQaReviewInformationEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.NonSourceableQaReviewRepository
import org.dataland.datalandqaservice.utils.TestJwtSecurityConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.util.UUID

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(
    classes = [
        DatalandQaService::class,
        TestJwtSecurityConfig::class,
    ],
    properties = ["spring.profiles.active=nodb"],
)
class NonSourceableQaReviewRepositoryTest {
    @Autowired
    private lateinit var nonSourceableQaReviewRepository: NonSourceableQaReviewRepository

    private fun getDummyEntity(
        nonSourceabilityId: UUID = UUID.randomUUID(),
        companyId: String = "company-id-1",
        dataType: String = "sfdr",
        reportingPeriod: String = "2023",
        qaStatus: QaStatus = QaStatus.Pending,
        uploadTime: Long = System.currentTimeMillis(),
    ): NonSourceableQaReviewInformationEntity =
        NonSourceableQaReviewInformationEntity(
            id = null,
            nonSourceabilityId = nonSourceabilityId,
            companyId = companyId,
            dataType = dataType,
            reportingPeriod = reportingPeriod,
            reason = "reason",
            uploaderUserId = "uploader-user",
            uploadTime = uploadTime,
            qaStatus = qaStatus,
            reviewerUserId = null,
            qaComment = null,
            createdAt = uploadTime,
            updatedAt = uploadTime,
        )

    @Test
    fun `test findByNonSourceabilityId returns matching review item`() {
        val nonSourceabilityId = UUID.randomUUID()
        val saved = nonSourceableQaReviewRepository.save(getDummyEntity(nonSourceabilityId = nonSourceabilityId))

        val result = nonSourceableQaReviewRepository.findByNonSourceabilityId(nonSourceabilityId)

        assertEquals(saved.id, result?.id)
        assertEquals(nonSourceabilityId, result?.nonSourceabilityId)
    }

    @Test
    fun `test findPendingReviewQueue only contains pending sorted oldest first`() {
        val newestPending =
            nonSourceableQaReviewRepository.save(
                getDummyEntity(
                    qaStatus = QaStatus.Pending,
                    uploadTime = 200,
                ),
            )
        nonSourceableQaReviewRepository.save(getDummyEntity(qaStatus = QaStatus.Accepted, uploadTime = 150))
        val oldestPending =
            nonSourceableQaReviewRepository.save(
                getDummyEntity(
                    qaStatus = QaStatus.Pending,
                    uploadTime = 100,
                ),
            )

        val result = nonSourceableQaReviewRepository.findPendingReviewQueue()

        assertEquals(2, result.size)
        assertEquals(oldestPending.id, result.first().id)
        assertEquals(newestPending.id, result.last().id)
        assertTrue(result.all { it.qaStatus == QaStatus.Pending })
    }

    @Test
    fun `test findByFilter applies optional filters correctly`() {
        nonSourceableQaReviewRepository.save(
            getDummyEntity(
                companyId = "company-id-1",
                dataType = "sfdr",
                reportingPeriod = "2023",
                qaStatus = QaStatus.Pending,
            ),
        )
        nonSourceableQaReviewRepository.save(
            getDummyEntity(
                companyId = "company-id-1",
                dataType = "lksg",
                reportingPeriod = "2023",
                qaStatus = QaStatus.Rejected,
            ),
        )
        nonSourceableQaReviewRepository.save(
            getDummyEntity(
                companyId = "company-id-2",
                dataType = "sfdr",
                reportingPeriod = "2022",
                qaStatus = QaStatus.Pending,
            ),
        )

        val result =
            nonSourceableQaReviewRepository.findByFilter(
                companyId = "company-id-1",
                dataType = "sfdr",
                reportingPeriod = "2023",
                qaStatus = QaStatus.Pending,
            )

        assertEquals(1, result.size)
        assertEquals("company-id-1", result.first().companyId)
        assertEquals("sfdr", result.first().dataType)
        assertEquals("2023", result.first().reportingPeriod)
        assertEquals(QaStatus.Pending, result.first().qaStatus)
    }
}
