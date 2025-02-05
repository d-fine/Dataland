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
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.dataland.datalandqaservice.repositories.QaReviewRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
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
        )
        qaReviewManager =
            QaReviewManager(
                mockQaReviewRepository,
                mockCompanyDataControllerApi,
                mockMetaDataControllerApi,
                mockCloudEventMessageHandler,
                objectMapper,
                mockExceptionForwarder,
            )

        doReturn(mockDataMetaInformation).whenever(mockMetaDataControllerApi).getDataMetaInfo(any())
        doReturn(mockStoredCompany).whenever(mockCompanyDataControllerApi).getCompanyById(any())
        doReturn(mock<QaReviewEntity>()).whenever(mockQaReviewRepository).save(any<QaReviewEntity>())
        doReturn(listOf(mockQaReviewEntity))
            .whenever(mockQaReviewRepository)
            .getSortedAndFilteredQaReviewMetadataSet(any(), any(), any())
    }

    /**
     * Set up the spy object to react in a specific way. Note: We don't need the spy for each test.
     */
    private fun setupSpy() {
        spyQaReviewManager = spy(qaReviewManager)
        doReturn(mockQaReviewEntity)
            .whenever(spyQaReviewManager)
            .saveQaReviewEntity(any(), any(), any(), anyOrNull(), any())
        doNothing().whenever(spyQaReviewManager).sendQaStatusUpdateMessage(any<QaReviewEntity>(), any())
    }

    @Test
    fun `check that adding a new qa review entry works on valid input with bypassQa true`() {
        setupSpy()

        assertDoesNotThrow {
            spyQaReviewManager.addDatasetToQaReviewRepository(
                dataId,
                bypassQa = true,
                correlationId = correlationId,
            )
        }
        verify(spyQaReviewManager, times(1)).saveQaReviewEntity(
            dataId = dataId,
            qaStatus = QaStatus.Accepted,
            triggeringUserId = uploaderId,
            comment = bypassQaComment,
            correlationId = correlationId,
        )
        verify(spyQaReviewManager, times(1)).sendQaStatusUpdateMessage(
            mockQaReviewEntity,
            correlationId,
        )
    }

    @Test
    fun `check that adding a new qa review entry works on valid input with bypassQa false`() {
        setupSpy()

        assertDoesNotThrow {
            spyQaReviewManager.addDatasetToQaReviewRepository(dataId, bypassQa = false, correlationId = correlationId)
        }
        verify(spyQaReviewManager, times(1)).saveQaReviewEntity(
            dataId = dataId,
            qaStatus = QaStatus.Pending,
            triggeringUserId = uploaderId,
            comment = null,
            correlationId = correlationId,
        )
        verify(spyQaReviewManager, times(1)).sendQaStatusUpdateMessage(
            mockQaReviewEntity,
            correlationId,
        )
    }

    @Test
    fun `check that saving QaReviewEntity works as expected`() {
        assertDoesNotThrow {
            qaReviewManager.saveQaReviewEntity(
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
    fun `check that patching the uploaderId throws an error if oldest qa entry does not have status Pending`() {
        val acceptedQaReviewEntityMock = mock<QaReviewEntity> { on { qaStatus } doReturn QaStatus.Accepted }
        doReturn(acceptedQaReviewEntityMock)
            .whenever(mockQaReviewRepository)
            .findFirstByDataIdOrderByTimestampAsc(any())

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
}
