package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.EutaxonomyNonFinancialsDataController
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.services.DataExportService
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.notNull
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
internal class DataControllerTest(
    @Autowired @Spy
    private val objectMapper: ObjectMapper,
    @Autowired
    private val dataExportService: DataExportService,
) {
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

    val testUserPendingDataMetaInformationEntity =
        buildDataMetaInformationEntity(testUserPendingDataId, testUserId, QaStatus.Pending)
    val otherUserPendingDataMetaInformationEntity =
        buildDataMetaInformationEntity(otherUserPendingDataId, otherUserId, QaStatus.Pending)
    val otherUserAcceptedDataMetaInformationEntity =
        buildDataMetaInformationEntity(otherUserAcceptedDataId, otherUserId, QaStatus.Accepted)

    @Mock
    lateinit var mockSecurityContext: SecurityContext

    @Mock
    lateinit var mockDataManager: DataManager

    @Mock
    lateinit var mockDataMetaInformationManager: DataMetaInformationManager
    lateinit var dataController: EutaxonomyNonFinancialsDataController

    private val testDataDimensions = BasicDataDimensions(testCompanyId, testDataType.toString(), testReportingPeriod)

    @BeforeEach
    fun setup() {
        `when`(mockDataMetaInformationManager.getDataMetaInformationByDataId(testUserPendingDataId)).thenReturn(
            testUserPendingDataMetaInformationEntity,
        )
        `when`(mockDataMetaInformationManager.getDataMetaInformationByDataId(otherUserPendingDataId)).thenReturn(
            otherUserPendingDataMetaInformationEntity,
        )
        `when`(mockDataMetaInformationManager.getDataMetaInformationByDataId(otherUserAcceptedDataId)).thenReturn(
            otherUserAcceptedDataMetaInformationEntity,
        )
        `when`(
            mockDataMetaInformationManager
                .searchDataMetaInfo(
                    testCompanyId,
                    testDataType,
                    false,
                    testReportingPeriod,
                    uploaderUserIds = null,
                    qaStatus = null,
                ),
        ).thenReturn(
            listOf(
                testUserPendingDataMetaInformationEntity,
                otherUserPendingDataMetaInformationEntity,
                otherUserAcceptedDataMetaInformationEntity,
            ),
        )
        `when`(mockDataManager.getDatasetData(anyString(), notNull() ?: testDataType.toString(), anyString()))
            .thenReturn(someEuTaxoDataAsString)
        dataController =
            EutaxonomyNonFinancialsDataController(
                mockDataManager,
                mockDataMetaInformationManager,
                dataExportService,
                objectMapper,
            )
    }

    @AfterEach
    fun resetMocks() = Mockito.reset(mockSecurityContext, mockDataManager, mockDataMetaInformationManager)

    @Test
    fun `test that the correct datasets are filtered`() {
        testGetCompanyAssociatedDataEndpoint()
        testGetAllCompanyDataEndpoint()
    }

    @Test
    fun `test that the json export does not throw an error`() {
        this.mockJwtAuthentication(DatalandRealmRole.ROLE_ADMIN)
        assertDoesNotThrow {
            dataController.exportCompanyAssociatedDataToJson(otherUserAcceptedDataId)
        }
    }

    @Test
    fun `test that the csv export does not throw an error`() {
        this.mockJwtAuthentication(DatalandRealmRole.ROLE_ADMIN)
        assertDoesNotThrow {
            dataController.exportCompanyAssociatedDataToCsv(otherUserAcceptedDataId)
        }
    }

    @Test
    fun `test that the excel export does not throw an error`() {
        this.mockJwtAuthentication(DatalandRealmRole.ROLE_ADMIN)
        assertDoesNotThrow {
            dataController.exportCompanyAssociatedDataToExcel(otherUserAcceptedDataId)
        }
    }

    @Test
    fun `test that no dataset is returned for a combination of reporting period company id and data type that does not exist`() {
        `when`(
            mockDataMetaInformationManager
                .getActiveDatasetIdByDataDimensions(testDataDimensions),
        ).thenReturn(null)
        assertThrows<ResourceNotFoundApiException> {
            dataController.getCompanyAssociatedData(reportingPeriod = testReportingPeriod, companyId = testCompanyId)
        }
    }

    @Test
    fun `test that the expected dataset is returned for a combination of reporting period company id and data type`() {
        `when`(
            mockDataMetaInformationManager
                .getActiveDatasetIdByDataDimensions(testDataDimensions),
        ).thenReturn(otherUserAcceptedDataId)
        val response = dataController.getCompanyAssociatedData(reportingPeriod = testReportingPeriod, companyId = testCompanyId)
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

    private fun testGetCompanyAssociatedDataEndpoint() {
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

    private fun testGetAllCompanyDataEndpoint() {
        mockJwtAuthentication(DatalandRealmRole.ROLE_UPLOADER)
        assertDatasetsContainExactly(listOf(testUserPendingDataId, otherUserAcceptedDataId))

        mockJwtAuthentication(DatalandRealmRole.ROLE_ADMIN)
        assertDatasetsContainExactly(listOf(testUserPendingDataId, otherUserPendingDataId, otherUserAcceptedDataId))
    }

    private fun mockJwtAuthentication(role: DatalandRealmRole) {
        val mockAuthentication = AuthenticationMock.mockJwtAuthentication("", testUserId, setOf(role))
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun assertExpectedDatasetForDataId(dataId: String) {
        Assertions.assertEquals(
            dataController.getCompanyAssociatedData(dataId).body!!.data,
            someEuTaxoData,
        )
    }

    private fun assertDatasetsContainExactly(dataIds: List<String>) {
        val datasetsWithMetaInfo = dataController.getFrameworkDatasetsForCompany(testCompanyId, false, testReportingPeriod).body!!
        Assertions.assertEquals(datasetsWithMetaInfo.size, dataIds.size)
        dataIds.forEach { dataId ->
            assert(datasetsWithMetaInfo.any { it.metaInfo.dataId == dataId })
        }
    }
}
