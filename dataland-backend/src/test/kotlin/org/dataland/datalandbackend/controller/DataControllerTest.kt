package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.EutaxonomyNonFinancialsDataController
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackend.services.DataExportService
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
internal class DataControllerTest(
    @Autowired
    private val dataExportService: DataExportService,
) {
    private val objectMapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()
    private val mockSecurityContext: SecurityContext = mock<SecurityContext>()
    private val mockDataManager: DataManager = mock<DataManager>()
    private val mockDataMetaInformationManager: DataMetaInformationManager = mock<DataMetaInformationManager>()

    private final val testDataProvider = TestDataProvider(objectMapper)

    private final val testDataType = DataType.valueOf("eutaxonomy-non-financials")
    private final val storedCompanyEntity = testDataProvider.getEmptyStoredCompanyEntity()
    private final val someEuTaxoData = testDataProvider.getEuTaxonomyNonFinancialsDataset()
    private val someEuTaxoDataAsString = objectMapper.writeValueAsString(someEuTaxoData)!!

    private final val testUserId = "testuser"
    private final val otherUserId = "otheruser"
    private final val testUserPendingDataId = "testuser_pending"
    private final val otherUserPendingDataId = "otheruser_pending"
    private final val otherUserAcceptedDataId = "otheruser_accepted"
    private final val testCompanyId = UUID.randomUUID().toString()
    private final val testReportingPeriod = "2023"

    private final val testUserPendingDataMetaInformationEntity =
        buildDataMetaInformationEntity(testUserPendingDataId, testUserId, QaStatus.Pending)
    private final val otherUserPendingDataMetaInformationEntity =
        buildDataMetaInformationEntity(otherUserPendingDataId, otherUserId, QaStatus.Pending)
    private final val otherUserAcceptedDataMetaInformationEntity =
        buildDataMetaInformationEntity(otherUserAcceptedDataId, otherUserId, QaStatus.Accepted)

    lateinit var dataController: EutaxonomyNonFinancialsDataController

    private val testDataDimensions = BasicDataDimensions(testCompanyId, testDataType.toString(), testReportingPeriod)

    @BeforeEach
    fun setup() {
        reset(mockSecurityContext, mockDataManager, mockDataMetaInformationManager)
        doReturn(testUserPendingDataMetaInformationEntity)
            .whenever(mockDataMetaInformationManager)
            .getDataMetaInformationByDataId(testUserPendingDataId)
        doReturn(otherUserPendingDataMetaInformationEntity)
            .whenever(mockDataMetaInformationManager)
            .getDataMetaInformationByDataId(otherUserPendingDataId)
        doReturn(otherUserAcceptedDataMetaInformationEntity)
            .whenever(mockDataMetaInformationManager)
            .getDataMetaInformationByDataId(otherUserAcceptedDataId)
        doReturn(
            listOf(
                testUserPendingDataMetaInformationEntity,
                otherUserPendingDataMetaInformationEntity,
                otherUserAcceptedDataMetaInformationEntity,
            ),
        ).whenever(mockDataMetaInformationManager).searchDataMetaInfo(
            DataMetaInformationSearchFilter(
                companyId = testCompanyId,
                dataType = testDataType,
                onlyActive = false,
                reportingPeriod = testReportingPeriod,
            ),
        )
        doReturn(someEuTaxoDataAsString)
            .whenever(mockDataManager)
            .getDatasetData(any(), any(), any())

        dataController =
            EutaxonomyNonFinancialsDataController(
                mockDataManager,
                mockDataMetaInformationManager,
                dataExportService,
                objectMapper,
            )
    }

    @Test
    fun `test that the correct datasets are filtered`() {
        mockJwtAuthentication(DatalandRealmRole.ROLE_UPLOADER)
        assertExpectedDatasetForDataId(testUserPendingDataId)
        assertThrows<AccessDeniedException> {
            dataController.getCompanyAssociatedData(otherUserPendingDataId)
        }
        assertExpectedDatasetForDataId(otherUserAcceptedDataId)

        mockJwtAuthentication(DatalandRealmRole.ROLE_ADMIN)
        assertExpectedDatasetForDataId(testUserPendingDataId)
        assertExpectedDatasetForDataId(otherUserPendingDataId)
        assertExpectedDatasetForDataId(otherUserAcceptedDataId)
    }

    @ParameterizedTest
    @EnumSource(ExportFileType::class)
    fun `test that the export functionality does not throw an error`(exportFileType: ExportFileType) {
        doReturn(someEuTaxoDataAsString)
            .whenever(mockDataManager)
            .getDatasetData(any<BasicDataDimensions>(), any())

        this.mockJwtAuthentication(DatalandRealmRole.ROLE_ADMIN)
        assertDoesNotThrow {
            dataController.exportCompanyAssociatedDataByDimensions(
                reportingPeriod = testReportingPeriod,
                companyId = testCompanyId,
                exportFileType = exportFileType,
            )
        }
    }

    @Test
    fun `test that resource not found error is thrown if combination of reportingPeriod and companyId and dataType does not exist`() {
        doReturn(null)
            .whenever(mockDataMetaInformationManager)
            .getActiveDatasetIdByDataDimensions(any<BasicDataDimensions>())

        assertThrows<ResourceNotFoundApiException> {
            dataController.getCompanyAssociatedDataByDimensions(
                reportingPeriod = testReportingPeriod,
                companyId = testCompanyId,
            )
        }
    }

    @Test
    fun `test that the expected dataset is returned for a combination of reporting period company id and data type`() {
        doReturn(someEuTaxoDataAsString).whenever(mockDataManager).getDatasetData(eq(testDataDimensions), any())
        val response =
            dataController.getCompanyAssociatedDataByDimensions(
                reportingPeriod = testReportingPeriod,
                companyId = testCompanyId,
            )
        Assertions.assertEquals(someEuTaxoData, response.body!!.data)
    }

    private fun buildDataMetaInformationEntity(
        dataId: String,
        uploaderId: String,
        qaStatus: QaStatus,
    ): DataMetaInformationEntity =
        DataMetaInformationEntity(
            dataId,
            storedCompanyEntity,
            testDataType.toString(),
            uploaderId,
            0,
            testReportingPeriod,
            null,
            qaStatus,
        )

    private fun mockJwtAuthentication(role: DatalandRealmRole) {
        val mockAuthentication = AuthenticationMock.mockJwtAuthentication("", testUserId, setOf(role))
        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun assertExpectedDatasetForDataId(dataId: String) {
        Assertions.assertEquals(
            dataController.getCompanyAssociatedData(dataId).body!!.data,
            someEuTaxoData,
        )
    }
}
