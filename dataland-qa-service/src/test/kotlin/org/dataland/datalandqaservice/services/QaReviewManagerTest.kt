package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackendutils.exceptions.ExceptionForwarder
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReportManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.dataland.datalandqaservice.repositories.QaReviewRepository
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QaReviewManagerTest {
    private val objectMapper = jacksonObjectMapper()

    private val mockQaReviewRepository: QaReviewRepository = mock<QaReviewRepository>()
    private val mockCompanyDataControllerApi: CompanyDataControllerApi = mock<CompanyDataControllerApi>()
    private val mockMetaDataControllerApi: MetaDataControllerApi = mock<MetaDataControllerApi>()
    private val mockCloudEventMessageHandler: CloudEventMessageHandler = mock<CloudEventMessageHandler>()
    private val mockExceptionForwarder: ExceptionForwarder = mock<ExceptionForwarder>()
    private val mockDataPointQaReportManager: DataPointQaReportManager = mock<DataPointQaReportManager>()
    private val mockDatasetReviewService: DatasetReviewService = mock<DatasetReviewService>()

    private val bypassQaComment = "Automatically QA approved."
    private val companyId: String = "dummyCompanyId"
    private val correlationId: String = "dummmyCorrelationId"
    private val dataId: String = UUID.randomUUID().toString()
    private val reportingPeriod: String = "dummyReportingPeriod"
    private val uploaderId = "dummyUploaderId"

    private val mockQaReviewEntity = mock<QaReviewEntity> { on { dataId } doReturn dataId }
    private val mockCompanyInformation = mock<CompanyInformation> { on { companyName } doReturn "dummyCompanyName" }
    private val mockStoredCompany = mock<StoredCompany> { on { companyInformation } doReturn mockCompanyInformation }
    private val mockDataMetaInformation =
        mock<DataMetaInformation> {
            on { uploaderUserId } doReturn uploaderId
            on { companyId } doReturn companyId
            on { dataType } doReturn DataTypeEnum.sfdr
            on { reportingPeriod } doReturn reportingPeriod
        }

    private lateinit var qaReviewManager: QaReviewManager
    private lateinit var spyQaReviewManager: QaReviewManager

    @BeforeEach
    fun setup() {
        reset(
            mockQaReviewRepository,
            mockCompanyDataControllerApi,
            mockMetaDataControllerApi,
            mockCloudEventMessageHandler,
            mockExceptionForwarder,
            mockDataPointQaReportManager,
        )
        qaReviewManager =
            QaReviewManager(
                mockQaReviewRepository,
                mockCompanyDataControllerApi,
                mockMetaDataControllerApi,
                mockCloudEventMessageHandler,
                objectMapper,
                mockExceptionForwarder,
                mockDataPointQaReportManager,
                mockDatasetReviewService,
            )

        doReturn(mockDataMetaInformation).whenever(mockMetaDataControllerApi).getDataMetaInfo(any())
        doReturn(mockStoredCompany).whenever(mockCompanyDataControllerApi).getCompanyById(any())
        doReturn(mock<QaReviewEntity>()).whenever(mockQaReviewRepository).save(any<QaReviewEntity>())
        doReturn(listOf(mockQaReviewEntity))
            .whenever(mockQaReviewRepository)
            .getSortedAndFilteredQaReviewMetadataset(any(), any(), any())
    }

    @ParameterizedTest
    @CsvSource(
        "true, Accepted",
        "false, Pending",
    )
    fun `check that adding a new qa review entry works on valid input`(
        bypassQa: Boolean,
        expectedStatus: QaStatus,
    ) {
        spyQaReviewManager = spy(qaReviewManager)
        doNothing().whenever(spyQaReviewManager).sendQaStatusUpdateMessage(any<QaReviewEntity>(), any())

        assertDoesNotThrow {
            spyQaReviewManager.addDatasetToQaReviewRepository(
                dataId,
                bypassQa = bypassQa,
                correlationId = correlationId,
            )
        }

        verify(spyQaReviewManager, times(1)).handleQaChange(
            dataId = dataId,
            qaStatus = expectedStatus,
            triggeringUserId = uploaderId,
            comment = if (bypassQa) bypassQaComment else null,
            correlationId = correlationId,
        )
        val argCaptor = argumentCaptor<QaReviewEntity>()
        verify(spyQaReviewManager, times(1)).sendQaStatusUpdateMessage(
            argCaptor.capture(),
            eq(correlationId),
        )
        Assertions.assertEquals(dataId, argCaptor.firstValue.dataId)
        Assertions.assertEquals(companyId, argCaptor.firstValue.companyId)
        Assertions.assertEquals(mockDataMetaInformation.dataType.value, argCaptor.firstValue.framework)
    }

    @Test
    fun `check that saving QaReviewEntity works as expected`() {
        assertDoesNotThrow {
            qaReviewManager.handleQaChange(
                dataId = dataId,
                qaStatus = QaStatus.Pending,
                triggeringUserId = uploaderId,
                comment = null,
                correlationId = correlationId,
            )
        }
        verify(mockQaReviewRepository, times(1)).save(any<QaReviewEntity>())
    }

    @Test
    fun `check that patching the uploaderId throws an error if finding oldest qa entry returns null`() {
        doReturn(null).whenever(mockQaReviewRepository).findFirstByDataIdOrderByTimestampAsc(any())

        assertThrows<IllegalArgumentException> {
            qaReviewManager.patchUploaderUserIdInQaReviewEntry(
                dataId = dataId,
                uploaderUserId = uploaderId,
                correlationId = correlationId,
            )
        }
    }

    @Test
    fun `check that patching the uploaderId works as expected`() {
        val dummyUploadQaReviewEntity =
            QaReviewEntity(
                dataId = dataId,
                companyId = companyId,
                companyName = "dummyCompanyName",
                framework = "dummyFramework",
                reportingPeriod = reportingPeriod,
                timestamp = 0L,
                qaStatus = QaStatus.Pending,
                triggeringUserId = "oldUploaderId",
                comment = null,
            )

        doReturn(dummyUploadQaReviewEntity).whenever(mockQaReviewRepository).findFirstByDataIdOrderByTimestampAsc(any())

        assertDoesNotThrow {
            qaReviewManager.patchUploaderUserIdInQaReviewEntry(
                dataId = dataId,
                uploaderUserId = uploaderId,
                correlationId = correlationId,
            )
        }
        Assertions.assertEquals(uploaderId, dummyUploadQaReviewEntity.triggeringUserId)
    }

    @Test
    fun `check that QaReviewResponse includes qa report count and reviewer user name`() {
        val qaReviewEntity =
            QaReviewEntity(
                dataId = dataId,
                companyId = companyId,
                companyName = "dummyCompanyName",
                framework = "dummyFramework",
                reportingPeriod = reportingPeriod,
                timestamp = 0L,
                qaStatus = QaStatus.Pending,
                triggeringUserId = uploaderId,
                comment = null,
            )

        doReturn(listOf(qaReviewEntity))
            .whenever(mockQaReviewRepository)
            .getSortedAndFilteredQaReviewMetadataset(any(), any(), any())
        doReturn(mapOf("first" to "dp1", "second" to "dp2"))
            .whenever(mockMetaDataControllerApi)
            .getContainedDataPoints(eq(dataId))
        doReturn(2L)
            .whenever(mockDataPointQaReportManager)
            .countQaReportsForDataPointIds(any())

        val responses =
            AuthenticationMock.withAuthenticationMock(
                username = "user",
                userId = uploaderId,
                roles = setOf(DatalandRealmRole.ROLE_USER),
            ) {
                qaReviewManager.getInfoOnDatasets(
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
        Assertions.assertNull(responses.first().reviewerUserName) // for now, change later
    }
}
