package org.dataland.datalandqaservice.repository

import org.assertj.core.api.Assertions.assertThat
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReportRepository
import org.dataland.datalandqaservice.utils.TestJwtSecurityConfig
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
class DataPointQaReportRepositoryTest {
    @Autowired private lateinit var repository: DataPointQaReportRepository

    private val report1 =
        DataPointQaReportEntity(
            qaReportId = "1",
            comment = "comment1",
            verdict = QaReportDataPointVerdict.QaAccepted,
            correctedData = null,
            dataPointId = "dp1",
            dataPointType = "typeA",
            reporterUserId = "user1",
            uploadTime = 1L,
            active = true,
        )
    private val report2 =
        DataPointQaReportEntity(
            qaReportId = "2",
            comment = "comment2",
            verdict = QaReportDataPointVerdict.QaAccepted,
            correctedData = null,
            dataPointId = "dp2",
            dataPointType = "typeA",
            reporterUserId = "user2",
            uploadTime = 2L,
            active = true,
        )
    private val report3 =
        DataPointQaReportEntity(
            qaReportId = "3",
            comment = "comment3",
            verdict = QaReportDataPointVerdict.QaRejected,
            correctedData = null,
            dataPointId = "dp3",
            dataPointType = "typeB",
            reporterUserId = "user3",
            uploadTime = 3L,
            active = false,
        )

    @Test
    fun `countByDataPointIdIn returns correct count`() {
        repository.saveAll(listOf(report1, report2, report3))

        val count = repository.countByDataPointIdIn(setOf("dp1", "dp3"))

        assertThat(count).isEqualTo(1)
    }
}
