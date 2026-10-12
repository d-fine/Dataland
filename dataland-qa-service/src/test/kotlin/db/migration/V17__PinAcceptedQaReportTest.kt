package db.migration

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.services.utils.BaseFlywayMigrationTest
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetJudgementRepository
import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.util.UUID

@SpringBootTest(
    classes = [DatalandQaService::class],
    properties = ["spring.profiles.active=containerized-db"],
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Suppress("ClassName")
class V17__PinAcceptedQaReportTest : BaseFlywayMigrationTest() {
    @Autowired
    lateinit var datasetJudgementRepository: DatasetJudgementRepository

    @Autowired
    lateinit var dataPointQaReportRepository: DataPointQaReportRepository

    private val reporter = UUID.randomUUID()
    private val pinnableReportId = UUID.randomUUID().toString()

    // data point id -> judgement data point entity id
    private val dataPointJudgementIds = mutableMapOf<String, UUID>()

    override fun getFlywayBaselineVersion(): String = "16"

    override fun getFlywayTargetVersion(): String = "17"

    override fun setupBeforeMigration() {
        saveQaReport("one-report", pinnableReportId)
        saveQaReport("two-reports", UUID.randomUUID().toString())
        saveQaReport("two-reports", UUID.randomUUID().toString())
        saveQaReport("finished", UUID.randomUUID().toString())
        saveQaReport("finished", UUID.randomUUID().toString())

        saveJudgement(
            DatasetJudgementState.Pending,
            dataPointJudgement("one-report", AcceptedDataPointSource.Qa),
            dataPointJudgement("two-reports", AcceptedDataPointSource.Qa),
            dataPointJudgement("no-report", AcceptedDataPointSource.Qa),
            dataPointJudgement("original", AcceptedDataPointSource.Original),
        )
        saveJudgement(
            DatasetJudgementState.FinishedWithDatasetAcceptance,
            dataPointJudgement("finished", AcceptedDataPointSource.Qa),
        )
    }

    private fun saveQaReport(
        dataPointId: String,
        qaReportId: String,
    ) {
        dataPointQaReportRepository.save(
            DataPointQaReportEntity(
                qaReportId = qaReportId,
                comment = "",
                verdict = QaReportDataPointVerdict.QaRejected,
                correctedData = null,
                dataPointId = dataPointId,
                dataPointType = "dummyType",
                reporterUserId = reporter.toString(),
                uploadTime = 0L,
                active = true,
            ),
        )
    }

    private fun dataPointJudgement(
        dataPointId: String,
        acceptedSource: AcceptedDataPointSource,
    ) = DataPointJudgementEntity(
        dataPointType = "dummyType-$dataPointId",
        dataPointId = dataPointId,
        acceptedSource = acceptedSource,
        reporterUserIdOfAcceptedQaReport = if (acceptedSource == AcceptedDataPointSource.Qa) reporter else null,
        customValue = null,
    ).also { dataPointJudgementIds[dataPointId] = it.id }

    private fun saveJudgement(
        state: DatasetJudgementState,
        vararg dataPointJudgements: DataPointJudgementEntity,
    ) {
        datasetJudgementRepository.save(
            DatasetJudgementEntity(
                dataSetJudgementId = UUID.randomUUID(),
                datasetId = UUID.randomUUID(),
                companyId = UUID.randomUUID(),
                dataType = DataTypeEnum.sfdr,
                reportingPeriod = "2024",
                judgementState = state,
                qaJudgeUserId = UUID.randomUUID(),
                qaJudgeUserName = "Dummy Judge",
                qaReporters = mutableListOf(),
                dataPoints = mutableListOf(),
            ).apply { dataPointJudgements.forEach { addAssociatedDataPoints(it) } },
        )
    }

    private fun migratedDataPoint(dataPointId: String): DataPointJudgementEntity =
        datasetJudgementRepository
            .findAll()
            .flatMap { it.dataPoints }
            .single { it.id == dataPointJudgementIds.getValue(dataPointId) }

    @Test
    fun `pins the selection when the reporter has exactly one report`() {
        val dataPoint = migratedDataPoint("one-report")
        assertEquals(AcceptedDataPointSource.Qa, dataPoint.acceptedSource)
        assertEquals(reporter, dataPoint.reporterUserIdOfAcceptedQaReport)
        assertEquals(pinnableReportId, dataPoint.acceptedQaReportId)
    }

    @Test
    fun `resets the selection when the reporter has several reports`() {
        assertReset(migratedDataPoint("two-reports"))
    }

    @Test
    fun `resets the selection when the reporter has no report`() {
        assertReset(migratedDataPoint("no-report"))
    }

    @Test
    fun `leaves non-QA selections untouched`() {
        val dataPoint = migratedDataPoint("original")
        assertEquals(AcceptedDataPointSource.Original, dataPoint.acceptedSource)
        assertNull(dataPoint.acceptedQaReportId)
    }

    @Test
    fun `leaves finished judgements untouched`() {
        val dataPoint = migratedDataPoint("finished")
        assertEquals(AcceptedDataPointSource.Qa, dataPoint.acceptedSource)
        assertEquals(reporter, dataPoint.reporterUserIdOfAcceptedQaReport)
        assertNull(dataPoint.acceptedQaReportId)
    }

    private fun assertReset(dataPoint: DataPointJudgementEntity) {
        assertNull(dataPoint.acceptedSource)
        assertNull(dataPoint.reporterUserIdOfAcceptedQaReport)
        assertNull(dataPoint.acceptedQaReportId)
    }

    @Test
    fun `skips the migration on fresh databases without judgement tables`() {
        val connection = mock<Connection>()
        val metaData = mock<DatabaseMetaData>()
        val tables = mock<ResultSet>()
        val context = mock<Context>()
        whenever(context.connection).thenReturn(connection)
        whenever(connection.metaData).thenReturn(metaData)
        whenever(metaData.getTables(isNull(), isNull(), any<String>(), isNull())).thenReturn(tables)
        whenever(tables.next()).thenReturn(false)

        V17__PinAcceptedQaReport().migrate(context)

        verify(connection, never()).createStatement()
    }
}
