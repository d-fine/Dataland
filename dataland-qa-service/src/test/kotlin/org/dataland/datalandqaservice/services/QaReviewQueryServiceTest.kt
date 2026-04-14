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
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import jakarta.persistence.PersistenceException
import org.springframework.dao.DataAccessException
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

    data class TestSetResult(
        val qaReview: QaReviewEntity,
        val datasetJudgement: DatasetJudgementEntity,
        val response: QaReviewResponse,
        val dataSourcingPriority: DataSourcingPriorityByDataDimensions,
    )

    private fun buildTestSet(
        dataId: String,
        companyId: String,
        companyName: String,
        reportingPeriod: String,
        timestamp: Long,
        triggeringUserId: String,
        comment: String,
        judgementId: UUID,
        qaJudgeUserId: UUID,
        qaJudgeUserName: String,
        numberQaReports: Long,
        priority: Int,
    ): TestSetResult {
        val framework = "sfdr"
        val qaReview =
            QaReviewEntity(
                dataId = dataId,
                companyId = companyId,
                companyName = companyName,
                framework = framework,
                reportingPeriod = reportingPeriod,
                timestamp = timestamp,
                qaStatus = QaStatus.Pending,
                triggeringUserId = triggeringUserId,
                comment = comment,
            )

        val datasetJudgement =
            DatasetJudgementEntity(
                dataSetJudgementId = judgementId,
                datasetId = UUID.fromString(dataId),
                companyId = UUID.randomUUID(),
                dataType = DataTypeEnum.sfdr,
                reportingPeriod = reportingPeriod,
                judgementState = DatasetJudgementState.Pending,
                qaJudgeUserId = qaJudgeUserId,
                qaJudgeUserName = qaJudgeUserName,
                qaReporters = mutableListOf(),
                dataPoints = mutableListOf(),
            )

        val response =
            QaReviewResponse(
                dataId = dataId,
                companyId = companyId,
                companyName = companyName,
                framework = framework,
                reportingPeriod = reportingPeriod,
                timestamp = timestamp,
                qaStatus = QaStatus.Pending,
                qaJudgeUserId = qaJudgeUserId.toString(),
                qaJudgeUserName = qaJudgeUserName,
                datasetReviewId = judgementId.toString(),
                numberQaReports = numberQaReports,
                comment = comment,
                triggeringUserId = null,
                priorityOfAssociatedDataSourcing = priority,
            )

        val dataSourcingPriority =
            DataSourcingPriorityByDataDimensions(
                dataType = framework,
                reportingPeriod = reportingPeriod,
                companyId = companyId,
                priority = priority,
            )

        return TestSetResult(qaReview, datasetJudgement, response, dataSourcingPriority)
    }

    private val testSet1 = buildTestSet(
        dataId = dataId1,
        companyId = "company-a",
        companyName = "Company A",
        reportingPeriod = "2024",
        timestamp = 1000L,
        triggeringUserId = "trigger-user-a",
        comment = "first",
        judgementId = judgementId1,
        qaJudgeUserId = judgeId1,
        qaJudgeUserName = "Judge A",
        numberQaReports = 3L,
        priority = 4,
    )

    private val qaReviewEntity1 = testSet1.qaReview
    private val datasetJudgementEntity1 = testSet1.datasetJudgement
    private val expected1 = testSet1.response

    private val testSet2 = buildTestSet(
        dataId = dataId2,
        companyId = "company-b",
        companyName = "Company B",
        reportingPeriod = "2023",
        timestamp = 2000L,
        triggeringUserId = "trigger-user-b",
        comment = "second",
        judgementId = judgementId2,
        qaJudgeUserId = judgeId2,
        qaJudgeUserName = "Judge B",
        numberQaReports = 5L,
        priority = 9,
    )
    private val qaReviewEntity2 = testSet2.qaReview
    private val datasetJudgementEntity2 = testSet2.datasetJudgement
    private val expected2 = testSet2.response

    private val expected = listOf(expected2, expected1)

    private val dataSourcingPriorities = listOf(testSet2.dataSourcingPriority, testSet1.dataSourcingPriority)

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

        doReturn(emptyMap<String, String>())
            .whenever(mockMetaDataControllerApi)
            .getContainedDataPoints(any())

        doReturn(emptyMap<String, Long>())
            .whenever(mockDataPointQaReportManager)
            .countQaReportsForDataPointIdsBulk(any())

        doReturn(listOf(datasetJudgementEntity))
            .whenever(mockDatasetJudgementRepository)
            .findAllWithDataPointsByDatasetIdIn(any())
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

        doReturn(dataSourcingPriorities).whenever(mockDataSourcingService).getDataSourcingPriorities(any())

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

    @Test
    fun `fallback to findAllByDatasetIdIn when fetch-join throws PersistenceException or DataAccessException`() {

        val exceptions: List<Throwable> = listOf(
            PersistenceException(),
            object : DataAccessException("db failure") {},
        )

        for (ex in exceptions) {
            doReturn(null)
                .whenever(mockDataSourcingService)
                .getDataSourcingPriorities(any())

            doReturn(listOf(qaReviewEntity))
                .whenever(mockQaReviewRepository)
                .getPendingQaReviewMetadatasetsByCompany(any())

            doThrow(ex)
                .whenever(mockDatasetJudgementRepository)
                .findAllWithDataPointsByDatasetIdIn(any())

            doReturn(listOf(datasetJudgementEntity))
                .whenever(mockDatasetJudgementRepository)
                .findAllByDatasetIdIn(any())

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
            Assertions.assertEquals(dummyUserId.toString(), responses.first().qaJudgeUserId)
            Assertions.assertEquals(dummyUserName, responses.first().qaJudgeUserName)

            verify(mockDatasetJudgementRepository).findAllWithDataPointsByDatasetIdIn(any())
            verify(mockDatasetJudgementRepository).findAllByDatasetIdIn(any())

            reset(mockDatasetJudgementRepository)
        }
    }

    @Test
    fun `dataset without any judgement still returned with null judgement fields`() {

        doReturn(
            listOf(
                DataSourcingPriorityByDataDimensions(
                    dataType = framework,
                    reportingPeriod = reportingPeriod,
                    companyId = companyId,
                    priority = 7,
                ),
            ),
        ).whenever(mockDataSourcingService).getDataSourcingPriorities(any())

        doReturn(listOf(qaReviewEntity))
            .whenever(mockQaReviewRepository)
            .getPendingQaReviewMetadatasetsByCompany(any())

        doReturn(emptyList<DatasetJudgementEntity>())
            .whenever(mockDatasetJudgementRepository)
            .findAllWithDataPointsByDatasetIdIn(any())

        doReturn(mapOf("x" to "dp-1", "y" to "dp-2"))
            .whenever(mockMetaDataControllerApi)
            .getContainedDataPoints(eq(dataId))

        doReturn(mapOf("dp-1" to 3L, "dp-2" to 2L))
            .whenever(mockDataPointQaReportManager)
            .countQaReportsForDataPointIdsBulk(any())

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
        val resp = responses.first()
        Assertions.assertNull(resp.qaJudgeUserId)
        Assertions.assertNull(resp.qaJudgeUserName)
        Assertions.assertNull(resp.datasetReviewId)
        Assertions.assertEquals(5L, resp.numberQaReports)
        Assertions.assertEquals(7, resp.priorityOfAssociatedDataSourcing)
    }
}
