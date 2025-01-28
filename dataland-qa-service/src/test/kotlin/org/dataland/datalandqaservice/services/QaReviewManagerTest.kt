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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.nullable
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock as ktmock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class QaReviewManagerTest {
    private val mockQaReviewRepository: QaReviewRepository = mock(QaReviewRepository::class.java)
    private val mockCompanyDataControllerApi: CompanyDataControllerApi = ktmock<CompanyDataControllerApi>()
    private val mockMetaDataControllerApi: MetaDataControllerApi = ktmock<MetaDataControllerApi>()
    private val mockCloudEventMessageHandler: CloudEventMessageHandler = ktmock<CloudEventMessageHandler>()
    private val mockExceptionForwarder: ExceptionForwarder = ktmock<ExceptionForwarder>()

    private val mockCompanyInformation = ktmock<CompanyInformation> {
        on { companyName } doReturn "dummyCompanyName"
    }
    private val mockStoredCompany = ktmock<StoredCompany> {
        on { companyInformation } doReturn mockCompanyInformation
    }
    private val dummyUploaderId = "dummyUploaderId"
    private val mockDataMetaInformation = ktmock<DataMetaInformation> {
        on { uploaderUserId } doReturn dummyUploaderId
        on { companyId } doReturn "dummyCompanyId"
        on { dataType } doReturn DataTypeEnum.sfdr
        on { reportingPeriod } doReturn "dummyReportingPeriod"
    }
    private val dummyDataId: String = UUID.randomUUID().toString()
    private val correlationId: String = "correlationId"
    private val bypassQaComment = "Automatically QA approved."
    private val objectMapper = jacksonObjectMapper()

    private lateinit var qaReviewManager: QaReviewManager

    @BeforeEach
    fun setup() {
        Mockito.reset(
            mockQaReviewRepository,
            mockCompanyDataControllerApi,
            mockMetaDataControllerApi,
            mockCloudEventMessageHandler,
            mockExceptionForwarder
        )
        qaReviewManager = QaReviewManager(
            mockQaReviewRepository,
            mockCompanyDataControllerApi,
            mockMetaDataControllerApi,
            mockCloudEventMessageHandler,
            objectMapper,
            mockExceptionForwarder,
        )

        `when`(mockMetaDataControllerApi.getDataMetaInfo(any())).thenReturn(mockDataMetaInformation)
        `when`(mockCompanyDataControllerApi.getCompanyById(any())).thenReturn(mockStoredCompany)
        `when`(mockQaReviewRepository.save(any<QaReviewEntity>())).thenReturn(ktmock<QaReviewEntity>())
    }

//    @Test
//    fun `check that adding a new qa review entry works on valid input with bypassQa true`() {
//        val spyQaReviewManager = spy(qaReviewManager)
//        val mockQaReviewEntity = ktmock<QaReviewEntity> {
//            on { dataId } doReturn dummyDataId
//            }
//
//        doReturn(listOf(mockQaReviewEntity)).whenever(mockQaReviewRepository).getSortedAndFilteredQaReviewMetadataSet(any(), any(), any())
//        assertDoesNotThrow {
//            spyQaReviewManager.addDatasetToQaReviewRepository(dummyDataId, bypassQa = true, correlationId = correlationId)
//        }
//
//        verify(qaReviewManager, times(1)).saveQaReviewEntity(
//            dataId = dummyDataId,
//            qaStatus = QaStatus.Accepted,
//            triggeringUserId = dummyUploaderId,
//            comment = bypassQaComment,
//            correlationId = correlationId
//        )
//    }
//
//    @Test
//    fun `check that adding a new qa review entry works on valid input with bypassQa false`() {
//        assertDoesNotThrow {
//            qaReviewManager.addDatasetToQaReviewRepository(dummyDataId, bypassQa = false, correlationId = correlationId)
//        }
//
//        verify(qaReviewManager, times(1)).saveQaReviewEntity(
//            dataId = dummyDataId,
//            qaStatus = QaStatus.Pending,
//            triggeringUserId = dummyUploaderId,
//            comment = null,
//            correlationId = correlationId
//        )
//    }

    @Test
    fun `check that saving QaReviewEntity works as expected`() {
        assertDoesNotThrow {
            qaReviewManager.saveQaReviewEntity(
                dataId = dummyDataId,
                qaStatus = QaStatus.Pending,
                triggeringUserId = dummyUploaderId,
                comment = null,
                correlationId = correlationId,
            )
        }
        verify(mockQaReviewRepository, times(1)).save(any<QaReviewEntity>())
    }

}
