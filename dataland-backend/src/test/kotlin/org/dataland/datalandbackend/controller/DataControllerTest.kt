package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.frameworks.lksg.LksgDataController
import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.companies.CompanyIdentifierValidationResult
import org.dataland.datalandbackend.model.export.ExportRequestData
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.DataExportService
import org.dataland.datalandbackend.services.DataExportStore
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.DatasetStorageService
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@DefaultMocks
internal class DataControllerTest {
    private val objectMapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules()
    private val mockSecurityContext: SecurityContext = mock<SecurityContext>()
    private val mockDataManager: DataManager = mock<DataManager>()
    private val mockDataMetaInformationManager: DataMetaInformationManager = mock<DataMetaInformationManager>()
    private val mockDataPointUtils = mock<DataPointUtils>()
    private val mockReferencedReportsUtils = mock<ReferencedReportsUtilities>()
    private val mockSpecificationApi = mock<SpecificationControllerApi>()
    private val mockCompanyQueryManager = mock<CompanyQueryManager>()
    private val mockDatasetStorageService = mock<DatasetStorageService>()
    private val dataExportService =
        DataExportService<LksgData>(
            mockDataPointUtils,
            mockReferencedReportsUtils,
            mockSpecificationApi,
            mockCompanyQueryManager,
            mockDatasetStorageService,
        )

    private final val testDataProvider = TestDataProvider(objectMapper)

    private final val testDataType = DataType.valueOf("lksg")
    private final val storedCompanyEntity = testDataProvider.getEmptyStoredCompanyEntity()
    private final val someEuTaxoData = testDataProvider.getLksgDataset()
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

    lateinit var dataController: LksgDataController

    private val testDataDimensions = BasicDatasetDimensions(testCompanyId, testDataType.toString(), testReportingPeriod)

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
        doReturn(someEuTaxoDataAsString).whenever(mockDataManager).getDatasetData(any(), any(), any())

        dataController =
            LksgDataController(
                mockDataManager,
                mockDataMetaInformationManager,
                dataExportService,
                DataExportStore(),
                mockCompanyQueryManager,
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
        val mockCompanyValidationResult =
            mock<CompanyIdentifierValidationResult> {
                on { companyInformation } doReturn mock<BasicCompanyInformation>()
            }
        doReturn(listOf(mockCompanyValidationResult))
            .whenever(mockCompanyQueryManager)
            .validateCompanyIdentifiers(any())
        doAnswer { invocation ->
            val argument = invocation.arguments[0] as Set<*>
            argument.associateWith { someEuTaxoDataAsString }
        }.whenever(mockDataManager).getDatasetData(any(), any())
        doReturn(null).whenever(mockDataPointUtils).getFrameworkSpecificationOrNull(any())

        this.mockJwtAuthentication(DatalandRealmRole.ROLE_ADMIN)
        assertDoesNotThrow {
            dataController.postExportJobCompanyAssociatedDataByDimensions(
                ExportRequestData(
                    reportingPeriods = listOf(testReportingPeriod),
                    companyIds = listOf(testCompanyId),
                    fileFormat = exportFileType,
                ),
            )
        }
    }

    @Test
    fun playground() {
        whenever(mockCompanyQueryManager.validateCompanyIdentifiers(any()))
            .doReturn(
                listOf(
                    CompanyIdentifierValidationResult(
                        identifier = "LEI",
                        companyInformation =
                            BasicCompanyInformation(
                                "comp-1",
                                "company name",
                                "Berlin",
                                "DE",
                                null, null,
                            ),
                    ),
                ),
            )

        dataController.postExportJobCompanyAssociatedDataByDimensions(
            ExportRequestData(
                reportingPeriods = listOf(testReportingPeriod),
                companyIds = listOf(testCompanyId),
                fileFormat = ExportFileType.CSV,
            ),
        )
    }

    @ParameterizedTest
    @EnumSource(ExportFileType::class)
    fun `test that the export functionality returns 404 if all input companyIds are invalid`(exportFileType: ExportFileType) {
        val mockCompanyValidationResult =
            mock<CompanyIdentifierValidationResult> { on { companyInformation } doReturn null }
        doReturn(listOf(mockCompanyValidationResult))
            .whenever(mockCompanyQueryManager)
            .validateCompanyIdentifiers(any())
        this.mockJwtAuthentication(DatalandRealmRole.ROLE_ADMIN)
        assertThrows<ResourceNotFoundApiException> {
            dataController.postExportJobCompanyAssociatedDataByDimensions(
                ExportRequestData(
                    reportingPeriods = listOf(testReportingPeriod),
                    companyIds = listOf(testCompanyId),
                    fileFormat = exportFileType,
                ),
            )
        }
    }

    @Test
    fun `test that resource not found error is thrown if combination of reportingPeriod and companyId and dataType does not exist`() {
        doReturn(null)
            .whenever(mockDataMetaInformationManager)
            .getActiveDatasetIdByDataDimensions(any<BasicDatasetDimensions>())

        assertThrows<ResourceNotFoundApiException> {
            dataController.getCompanyAssociatedDataByDimensions(
                reportingPeriod = testReportingPeriod,
                companyId = testCompanyId,
            )
        }
    }

    @Test
    fun `test that the expected dataset is returned for a combination of reporting period company id and data type`() {
        doAnswer { invocation ->
            val argument = invocation.getArgument<Set<BasicDatasetDimensions>>(0)
            argument.associateWith { someEuTaxoDataAsString }
        }.whenever(mockDataManager).getDatasetData(eq(setOf(testDataDimensions)), any())
        val response =
            dataController.getCompanyAssociatedDataByDimensions(
                reportingPeriod = testReportingPeriod,
                companyId = testCompanyId,
            )
        Assertions.assertEquals(someEuTaxoData, response.body!!.data)
    }

    @Test
    fun `verify that polling data by company ID and reporting period throws an error if no data is available`() {
        doReturn(emptyMap<BasicDataDimensions, String>()).whenever(mockDataManager).getDatasetData(any(), any())
        assertThrows<ResourceNotFoundApiException> {
            dataController.getCompanyAssociatedDataByDimensions(testReportingPeriod, testCompanyId)
        }
    }

    @Test
    fun `verify that polling latest data fails if the company identifier is ambiguous`() {
        val companyLei = randomLei()

        doReturn(listOf("firstCompanyId", "ambiguousCompanyId").map { createCompanyValidationResultMock(it) })
            .whenever(
                mockCompanyQueryManager,
            ).validateCompanyIdentifiers(listOf(companyLei))

        assertThrows<InvalidInputApiException> {
            dataController.getLatestAvailableCompanyAssociatedData(companyLei)
        }.also { exception ->
            Assertions.assertTrue(exception.summary.startsWith("Multiple companies found"))
        }
    }

    @Test
    fun `verify that polling latest data fails if the company identifier is not known`() {
        val companyLei = randomLei()

        doReturn(emptyList<CompanyIdentifierValidationResult>())
            .whenever(mockCompanyQueryManager)
            .validateCompanyIdentifiers(listOf(companyLei))

        assertThrows<ResourceNotFoundApiException> {
            dataController.getLatestAvailableCompanyAssociatedData(companyLei)
        }.also { exception ->
            Assertions.assertTrue(exception.summary.endsWith("$companyLei not found."))
        }
    }

    @Test
    fun `verify that polling latest data fails if no data is available`() {
        val companyLei = randomLei()
        val companyId = UUID.randomUUID().toString()

        doReturn(listOf(createCompanyValidationResultMock(companyId)))
            .whenever(mockCompanyQueryManager)
            .validateCompanyIdentifiers(listOf(companyLei))
        doReturn(null).whenever(mockDataManager).getLatestAvailableData(eq(companyId), any(), any())

        assertThrows<ResourceNotFoundApiException> {
            dataController.getLatestAvailableCompanyAssociatedData(companyLei)
        }.also { exception ->
            Assertions.assertTrue(exception.summary.startsWith("No available data found"))
        }
    }

    @Test
    fun `verify that polling latest works with an identifier other than the companyID`() {
        val companyLei = randomLei()
        val companyId = UUID.randomUUID().toString()

        doReturn(listOf(createCompanyValidationResultMock(companyId)))
            .whenever(mockCompanyQueryManager)
            .validateCompanyIdentifiers(listOf(companyLei))
        doReturn(null).whenever(mockDataManager).getLatestAvailableData(eq(companyId), any(), any())
        doReturn(Pair(testReportingPeriod, someEuTaxoDataAsString))
            .whenever(mockDataManager)
            .getLatestAvailableData(eq(companyId), any(), any())

        val response = dataController.getLatestAvailableCompanyAssociatedData(companyLei)
        Assertions.assertNotNull(response, "Controller should not return null")
        Assertions.assertNotNull(response.body, "Response body should not be null")

        Assertions.assertEquals(
            CompanyAssociatedData(companyId, testReportingPeriod, someEuTaxoData),
            response.body,
        )
    }

    private fun randomLei(): String = (1..20).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")

    private fun createCompanyValidationResultMock(companyId: String): CompanyIdentifierValidationResult {
        val companyInfo =
            BasicCompanyInformation(
                companyId, "test company name", "test company HQ", "DE", null, null,
            )
        return mock<CompanyIdentifierValidationResult> {
            on { companyInformation } doReturn companyInfo
        }
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
