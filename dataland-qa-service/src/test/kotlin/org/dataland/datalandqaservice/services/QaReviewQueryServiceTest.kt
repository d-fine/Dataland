package org.dataland.datalandqaservice.services

import org.dataland.dataSourcingService.openApiClient.api.DataSourcingControllerApi
import org.dataland.dataSourcingService.openApiClient.model.DataSourcingPriorityByDataDimensions
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.QaReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetJudgementRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReportManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatalandBackendAccessor
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewQueryService
import org.dataland.datalandqaservice.repositories.QaReviewRepository
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QaReviewQueryServiceTest {
    private val mockQaReviewRepository: QaReviewRepository = mock<QaReviewRepository>()
    private val mockBackendAccessor: DatalandBackendAccessor = mock<DatalandBackendAccessor>()
    private val mockDataPointQaReportManager: DataPointQaReportManager = mock<DataPointQaReportManager>()
    private val mockDataSourcingService: DataSourcingControllerApi = mock<DataSourcingControllerApi>()
    private val mockDatasetJudgementRepository: DatasetJudgementRepository = mock<DatasetJudgementRepository>()
    private val mockMetaDataControllerApi: MetaDataControllerApi = mock<MetaDataControllerApi>()

    private val companyId: String = "dummyCompanyId"
    private val dataId: String = UUID.randomUUID().toString()
    private val reportingPeriod: String = "dummyReportingPeriod"
    private val uploaderId = "dummyUploaderId"
    private val dummyUserName = "dummyUserName"
    private val dummyUserId = UUID.randomUUID()
    private val framework = "dummyFramework"

    private val qaReviewEntity =
        QaReviewEntity(
            dataId = dataId,
            companyId = companyId,
            companyName = "dummyCompanyName",
            framework = framework,
            reportingPeriod = reportingPeriod,
            timestamp = 0L,
            qaStatus = QaStatus.Pending,
            triggeringUserId = uploaderId,
            comment = null,
        )

    private val datasetJudgementEntity =
        DatasetJudgementEntity(
            dataSetJudgementId = UUID.randomUUID(),
            datasetId = UUID.fromString(dataId),
            companyId = UUID.randomUUID(),
            dataType = DataTypeEnum.sfdr,
            reportingPeriod = reportingPeriod,
            judgementState = DatasetJudgementState.Pending,
            qaJudgeUserId = dummyUserId,
            qaJudgeUserName = dummyUserName,
            qaReporters = mutableListOf(),
            dataPoints = mutableListOf(),
        )

    private val dataId1: String = UUID.randomUUID().toString()
    private val dataId2: String = UUID.randomUUID().toString()

    private val judgementId1: UUID = UUID.randomUUID()
    private val judgementId2: UUID = UUID.randomUUID()
    private val judgeId1: UUID = UUID.randomUUID()
    private val judgeId2: UUID = UUID.randomUUID()

    private val qaReviewEntity1 =
        QaReviewEntity(
            dataId = dataId1,
            companyId = "company-a",
            companyName = "Company A",
            framework = "sfdr",
            reportingPeriod = "2024",
            timestamp = 1000L,
            qaStatus = QaStatus.Pending,
            triggeringUserId = "trigger-user-a",
            comment = "first",
        )
    private val qaReviewEntity2 =
        QaReviewEntity(
            dataId = dataId2,
            companyId = "company-b",
            companyName = "Company B",
            framework = "sfdr",
            reportingPeriod = "2023",
            timestamp = 2000L,
            qaStatus = QaStatus.Pending,
            triggeringUserId = "trigger-user-b",
            comment = "second",
        )

    private val datasetJudgementEntity1 =
        DatasetJudgementEntity(
            dataSetJudgementId = judgementId1,
            datasetId = UUID.fromString(dataId1),
            companyId = UUID.randomUUID(),
            dataType = DataTypeEnum.sfdr,
            reportingPeriod = "2024",
            judgementState = DatasetJudgementState.Pending,
            qaJudgeUserId = judgeId1,
            qaJudgeUserName = "Judge A",
            qaReporters = mutableListOf(),
            dataPoints = mutableListOf(),
        )
    private val datasetJudgementEntity2 =
        DatasetJudgementEntity(
            dataSetJudgementId = judgementId2,
            datasetId = UUID.fromString(dataId2),
            companyId = UUID.randomUUID(),
            dataType = DataTypeEnum.sfdr,
            reportingPeriod = "2023",
            judgementState = DatasetJudgementState.Pending,
            qaJudgeUserId = judgeId2,
            qaJudgeUserName = "Judge B",
            qaReporters = mutableListOf(),
            dataPoints = mutableListOf(),
        )

    private val expected =
        listOf(
            QaReviewResponse(
                dataId = dataId2,
                companyId = "company-b",
                companyName = "Company B",
                framework = "sfdr",
                reportingPeriod = "2023",
                timestamp = 2000L,
                qaStatus = QaStatus.Pending,
                qaJudgeUserId = judgeId2.toString(),
                qaJudgeUserName = "Judge B",
                datasetReviewId = judgementId2.toString(),
                numberQaReports = 5L,
                comment = "second",
                triggeringUserId = null,
                priorityOfAssociatedDataSourcing = 9,
            ),
            QaReviewResponse(
                dataId = dataId1,
                companyId = "company-a",
                companyName = "Company A",
                framework = "sfdr",
                reportingPeriod = "2024",
                timestamp = 1000L,
                qaStatus = QaStatus.Pending,
                qaJudgeUserId = judgeId1.toString(),
                qaJudgeUserName = "Judge A",
                datasetReviewId = judgementId1.toString(),
                numberQaReports = 3L,
                comment = "first",
                triggeringUserId = null,
                priorityOfAssociatedDataSourcing = 4,
            ),
        )

    private lateinit var qaReviewQueryService: QaReviewQueryService

    @BeforeEach
    fun setup() {
        reset(
            mockQaReviewRepository,
            mockBackendAccessor,
            mockDataPointQaReportManager,
            mockDataSourcingService,
            mockDatasetJudgementRepository,
            mockMetaDataControllerApi,
        )
        qaReviewQueryService =
            QaReviewQueryService(
                mockQaReviewRepository,
                mockBackendAccessor,
                mockDataPointQaReportManager,
                mockDataSourcingService,
                mockDatasetJudgementRepository,
                mockMetaDataControllerApi,
            )

        doReturn(emptySet<String>()).whenever(mockBackendAccessor).getCompanyIdsForCompanyName(any())
    }

    @Test
    fun `check that QaReviewResponse includes qa report count and reviewer user name`() {
        doReturn(listOf(qaReviewEntity))
            .whenever(mockQaReviewRepository)
            .getSortedAndFilteredQaReviewMetadataset(any(), any(), any())
        doReturn(mapOf("first" to "dp1", "second" to "dp2"))
            .whenever(mockMetaDataControllerApi)
            .getContainedDataPoints(eq(dataId))
        doReturn(2L)
            .whenever(mockDataPointQaReportManager)
            .countQaReportsForDataPointIds(any())
        doReturn(listOf(datasetJudgementEntity))
            .whenever(mockDatasetJudgementRepository)
            .findAllByDatasetId(any())

        val responses =
            AuthenticationMock.withAuthenticationMock(
                username = "user",
                userId = uploaderId,
                roles = setOf(DatalandRealmRole.ROLE_USER),
            ) {
                qaReviewQueryService.getInfoOnDatasets(
                    dataTypes = setOf(DataTypeEnum.sfdr),
                    reportingPeriods = setOf(reportingPeriod),
                    companyName = null,
                    qaStatus = QaStatus.Pending,
                    chunkSize = 10,
                    chunkIndex = 0,
                )
            }

        Assertions.assertEquals(1, responses.size)
        Assertions.assertEquals(2L, responses.first().numberQaReports)
        Assertions.assertEquals(dummyUserId.toString(), responses.first().qaJudgeUserId)
        Assertions.assertEquals(dummyUserName, responses.first().qaJudgeUserName)
    }

    @Test
    fun `check that getInfoOnPendingDatasets fetches correct data sourcing priority`() {
        val dummyPriorityByDataDimension =
            DataSourcingPriorityByDataDimensions(
                dataType = framework,
                reportingPeriod = reportingPeriod,
                companyId = companyId,
                priority = 4,
            )

        doReturn(listOf(dummyPriorityByDataDimension))
            .whenever(mockDataSourcingService)
            .getDataSourcingPriorities(any())

        doReturn(listOf(qaReviewEntity))
            .whenever(mockQaReviewRepository)
            .getPendingQaReviewMetadatasetsByCompany(any())

        val responses =
            AuthenticationMock.withAuthenticationMock(
                username = "user",
                userId = uploaderId,
                roles = setOf(DatalandRealmRole.ROLE_USER),
            ) {
                qaReviewQueryService.getInfoOnPendingDatasets(
                    companyName = null,
                )
            }

        Assertions.assertEquals(1, responses.size)
        Assertions.assertEquals(4, responses.first().priorityOfAssociatedDataSourcing)
    }

    @Test
    fun `check that getInfoOnPendingDatasets fetches data sourcing priority of null if it is not specified`() {
        doReturn(null)
            .whenever(mockDataSourcingService)
            .getDataSourcingPriorities(any())

        doReturn(listOf(qaReviewEntity))
            .whenever(mockQaReviewRepository)
            .getPendingQaReviewMetadatasetsByCompany(any())

        val responses =
            AuthenticationMock.withAuthenticationMock(
                username = "user",
                userId = uploaderId,
                roles = setOf(DatalandRealmRole.ROLE_USER),
            ) {
                qaReviewQueryService.getInfoOnPendingDatasets(
                    companyName = null,
                )
            }

        Assertions.assertEquals(1, responses.size)
        Assertions.assertNull(responses.first().priorityOfAssociatedDataSourcing)
    }

    @Test
    fun `check that getInfoOnPendingDatasets returns stable content and order`() {
        doReturn(listOf(qaReviewEntity2, qaReviewEntity1))
            .whenever(mockQaReviewRepository)
            .getPendingQaReviewMetadatasetsByCompany(any())
        doReturn(listOf(datasetJudgementEntity1, datasetJudgementEntity2))
            .whenever(mockDatasetJudgementRepository)
            .findAllWithDataPointsByDatasetIdIn(any())
        doReturn(mapOf("x" to "dp-1", "y" to "dp-2"))
            .whenever(mockMetaDataControllerApi)
            .getContainedDataPoints(eq(dataId1))
        doReturn(mapOf("x" to "dp-3"))
            .whenever(mockMetaDataControllerApi)
            .getContainedDataPoints(eq(dataId2))
        doReturn(mapOf("dp-1" to 1L, "dp-2" to 2L, "dp-3" to 5L))
            .whenever(mockDataPointQaReportManager)
            .countQaReportsForDataPointIdsBulk(any())
        doReturn(
            listOf(
                DataSourcingPriorityByDataDimensions(
                    dataType = "sfdr",
                    reportingPeriod = "2023",
                    companyId = "company-b",
                    priority = 9,
                ),
                DataSourcingPriorityByDataDimensions(
                    dataType = "sfdr",
                    reportingPeriod = "2024",
                    companyId = "company-a",
                    priority = 4,
                ),
            ),
        ).whenever(mockDataSourcingService).getDataSourcingPriorities(any())

        val actual =
            AuthenticationMock.withAuthenticationMock(
                username = "user",
                userId = "dummy-user",
                roles = setOf(DatalandRealmRole.ROLE_USER),
            ) {
                qaReviewQueryService.getInfoOnPendingDatasets(companyName = null)
            }

        Assertions.assertEquals(expected, actual)
    }
}
