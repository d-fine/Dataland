package org.dataland.datalandqaservice.services

import org.dataland.dataSourcingService.openApiClient.api.DataSourcingControllerApi
import org.dataland.dataSourcingService.openApiClient.model.DataSourcingPriorityByDataDimensions
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState
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
}
