package org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetJudgementRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementFinalizationService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementSupportService
import org.dataland.datalandqaservice.utils.NoBackendRequestQaReportConfiguration
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID

@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(
    classes = [DatalandQaService::class, NoBackendRequestQaReportConfiguration::class],
    properties = ["spring.profiles.active=nodb"],
)
class DatasetJudgementStateRollbackTest(
    @Autowired private val service: DatasetJudgementService,
    @Autowired private val repository: DatasetJudgementRepository,
    @Autowired private val jdbcTemplate: JdbcTemplate,
) {
    @MockitoBean private lateinit var supportService: DatasetJudgementSupportService

    @MockitoBean private lateinit var finalizationService: DatasetJudgementFinalizationService

    @Test
    fun `failed finalization rolls back the judgement transaction`() {
        val judgeId = UUID.randomUUID()
        val judgement =
            repository.save(
                DatasetJudgementEntity(
                    dataSetJudgementId = UUID.randomUUID(),
                    datasetId = UUID.randomUUID(),
                    companyId = UUID.randomUUID(),
                    dataType = DataTypeEnum.sfdr,
                    reportingPeriod = "2025",
                    judgementState = DatasetJudgementState.Pending,
                    qaJudgeUserId = judgeId,
                    qaJudgeUserName = "Judge",
                    qaReporters = mutableListOf(),
                    dataPoints = mutableListOf(),
                ),
            )
        AuthenticationMock.mockSecurityContext("judge@example.com", judgeId.toString(), setOf(DatalandRealmRole.ROLE_ADMIN))
        whenever(supportService.getDatasetJudgementEntityById(judgement.dataSetJudgementId))
            .thenAnswer { repository.findById(judgement.dataSetJudgementId).orElseThrow() }
        doAnswer {
            jdbcTemplate.update(
                "UPDATE dataset_judgement SET judgement_state = ? WHERE dataset_judgement_id = ?",
                DatasetJudgementState.FinishedWithDatasetAcceptance.toString(),
                judgement.dataSetJudgementId,
            )
            throw InvalidInputApiException("Replacement invalid", "Backend rejected replacement")
        }.whenever(finalizationService).handleAcceptance(any())

        assertThrows<InvalidInputApiException> {
            service.setJudgementState(judgement.dataSetJudgementId, DatasetJudgementState.FinishedWithDatasetAcceptance)
        }
        assertEquals(DatasetJudgementState.Pending, repository.findById(judgement.dataSetJudgementId).orElseThrow().judgementState)
    }
}
