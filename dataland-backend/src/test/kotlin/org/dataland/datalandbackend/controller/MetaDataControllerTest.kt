package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.frameworks.sfdr.model.SfdrData
import org.dataland.datalandbackend.frameworks.vsme.model.VsmeData
import org.dataland.datalandbackend.model.DataDimensionFilter
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformationPatch
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.dataland.specificationservice.openApiClient.model.SimpleFrameworkSpecification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.random.Random

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
internal class MetaDataControllerTest
    @Autowired
    constructor(
        private val objectMapper: ObjectMapper,
        private val companyManager: CompanyAlterationManager,
        private val dataMetaInformationManager: DataMetaInformationManager,
        private val dataPointMetaInformationManager: DataPointMetaInformationManager,
        private val metaDataController: MetaDataController,
        @Value("\${dataland.backend.proxy-primary-url}") private val proxyPrimaryUrl: String,
    ) {
        private lateinit var testCompanyInformation: CompanyInformation
        private lateinit var storedCompany: StoredCompanyEntity
        private val adminUserId = "admin-user-id"
        private val readerUserId = "reader-user-id"
        private val uploaderUserId = "uploader-user-id"
        private val defaultReportingPeriod = "2023"
        private val defaultDataType = DataType.of(SfdrData::class.java)
        private val defaultDataPointType = "default-data-point-type"

        val testDataProvider = TestDataProvider(objectMapper)
        private final val expectedSetOfRolesForReader = setOf(DatalandRealmRole.ROLE_USER)
        private final val expectedSetOfRolesForUploader = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER)
        private final val expectedSetOfRolesForReviewer = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_REVIEWER)
        private final val expectedSetOfRolesForAdmin = DatalandRealmRole.entries.toSet()

        @MockitoBean
        private var specificationClient = mock<SpecificationControllerApi>()

        @BeforeEach
        fun setup() {
            testCompanyInformation = testDataProvider.getCompanyInformationWithoutIdentifiers(1).last()
            storedCompany = companyManager.addCompany(testCompanyInformation)
            mockSecurityContext(userId = adminUserId, roles = expectedSetOfRolesForAdmin)
        }

        @Test
        fun `ensure that meta info about a pending dataset can only be retrieved by authorized users`() {
            val metaInfo =
                dataMetaInformationManager.storeDataMetaInformation(
                    DataMetaInformationEntity(
                        dataId = "data-id-for-testing-user-access", company = storedCompany,
                        dataType = DataType.of(LksgData::class.java).toString(), uploaderUserId = uploaderUserId,
                        uploadTime = 0, reportingPeriod = defaultReportingPeriod, currentlyActive = null,
                        qaStatus = QaStatus.Pending,
                    ),
                )
            mockSecurityContext(userId = readerUserId, roles = expectedSetOfRolesForReader)
            assertMetaDataNotVisible(metaInfo)
            mockSecurityContext(userId = uploaderUserId, roles = expectedSetOfRolesForUploader)
            assertMetaDataVisible(metaInfo)
            mockSecurityContext(userId = adminUserId, roles = expectedSetOfRolesForAdmin)
            assertMetaDataVisible(metaInfo)
        }

        @Test
        fun `ensure that meta info about a rejected dataset can only be retrieved by authorized users`() {
            val metaInfo =
                dataMetaInformationManager.storeDataMetaInformation(
                    DataMetaInformationEntity(
                        dataId = "data-id-for-testing-user-access-to-rejected-datasets", company = storedCompany,
                        dataType = DataType.of(SfdrData::class.java).toString(),
                        uploaderUserId = "uploader-user-id-of-rejected-dataset",
                        uploadTime = 0, reportingPeriod = defaultReportingPeriod, currentlyActive = null,
                        qaStatus = QaStatus.Rejected,
                    ),
                )
            mockSecurityContext(userId = readerUserId, roles = expectedSetOfRolesForReader)
            assertMetaDataNotVisible(metaInfo)
            mockSecurityContext(userId = "uploader-user-id-of-rejected-dataset", roles = expectedSetOfRolesForUploader)
            assertMetaDataVisible(metaInfo)
            mockSecurityContext(userId = "different-uploader-user-id", roles = expectedSetOfRolesForUploader)
            assertMetaDataNotVisible(metaInfo)
            mockSecurityContext(userId = adminUserId, roles = expectedSetOfRolesForAdmin)
            assertMetaDataVisible(metaInfo)
            mockSecurityContext(userId = "reviewer-user-id", roles = expectedSetOfRolesForAdmin)
            assertMetaDataVisible(metaInfo)
        }

        @Test
        fun `check if DataMetaInformationSearchFilter is correctly transformed into DataMetaInformation`() {
            val dataId = listOf("first-data-id", "second-data-id")
            val dataType = defaultDataType
            val uploaderUserId = UUID.randomUUID()
            val qaStatus = QaStatus.Accepted
            val amountStoredCompanies = 2
            val storedCompanies = addCompanyToDatabase(amountStoredCompanies)
            val url = "https://$proxyPrimaryUrl/companies/${storedCompanies[0].companyId}/frameworks/$dataType/${dataId[0]}"
            val dataMetaInformationSearchFilters = mutableListOf<DataMetaInformationSearchFilter>()
            for (i in 0 until amountStoredCompanies) {
                dataMetaInformationSearchFilters.add(
                    DataMetaInformationSearchFilter(
                        companyId = storedCompanies[i].companyId, dataType, defaultReportingPeriod, true, setOf(uploaderUserId),
                        qaStatus,
                    ),
                )
                addMetainformation(dataId = dataId[i], company = storedCompanies[i], userId = uploaderUserId.toString())
            }
            val listDataMetaInfos = metaDataController.postListOfDataMetaInfoFilters(dataMetaInformationSearchFilters).body

            assertEquals(amountStoredCompanies, listDataMetaInfos?.size)
            val dataMetaInfo = listDataMetaInfos?.get(0)
            if (dataMetaInfo != null) {
                assertEquals(storedCompanies[0].companyId, dataMetaInfo.companyId)
                assertEquals(dataType, dataMetaInfo.dataType)
                assertEquals(qaStatus, dataMetaInfo.qaStatus)
                assertTrue(dataMetaInfo.currentlyActive)
                assertEquals(uploaderUserId.toString(), dataMetaInfo.uploaderUserId)
                assertEquals(url, dataMetaInfo.ref)
            }
        }

        @Test
        fun `ensure that meta info patch endpoint cannot be accessed by non admins`() {
            val metaInfo = addMetainformation()
            val dataMetaInformationPatch =
                DataMetaInformationPatch(
                    uploaderUserId = readerUserId,
                )
            mockSecurityContext(userId = readerUserId, roles = expectedSetOfRolesForReader)
            assertMetaDataNotPatchableWithException<AccessDeniedException>(metaInfo, dataMetaInformationPatch)
            mockSecurityContext(userId = uploaderUserId, roles = expectedSetOfRolesForUploader)
            assertMetaDataNotPatchableWithException<AccessDeniedException>(metaInfo, dataMetaInformationPatch)
            mockSecurityContext(userId = "reviewer-user-id", roles = expectedSetOfRolesForReviewer)
            assertMetaDataNotPatchableWithException<AccessDeniedException>(metaInfo, dataMetaInformationPatch)
        }

        @Test
        fun `ensure that meta info patch endpoint rejects empty patches`() {
            val metaInfo = addMetainformation()
            val emptyDataMetaInformationPatch = DataMetaInformationPatch(uploaderUserId = "")
            assertMetaDataNotPatchableWithException<InvalidInputApiException>(metaInfo, emptyDataMetaInformationPatch)
        }

        @Test
        fun `ensure that meta info patch endpoint rejects vsme data`() {
            val metaInfo = addMetainformation(dataType = DataType.of(VsmeData::class.java).toString())
            val mockDataMetaInformationPatch = mock<DataMetaInformationPatch> { on { uploaderUserId } doReturn uploaderUserId }
            assertMetaDataNotPatchableWithException<InvalidInputApiException>(metaInfo, mockDataMetaInformationPatch)
        }

        @Test
        fun `check that the active data endpoint works as expected for basic dataset related searches`() {
            doReturn(emptyList<String>()).whenever(specificationClient).listFrameworkSpecifications()
            val nonDefaultReportingPeriod = "2022"
            val nonDefaultDataType = "lksg"
            addMetainformation(reportingPeriod = nonDefaultReportingPeriod)
            addMetainformation(dataType = nonDefaultDataType)
            val allDimensions =
                listOf(
                    BasicDataDimensions(
                        companyId = storedCompany.companyId,
                        dataType = defaultDataType.toString(),
                        reportingPeriod = nonDefaultReportingPeriod,
                    ),
                    BasicDataDimensions(
                        companyId = storedCompany.companyId,
                        dataType = nonDefaultDataType,
                        reportingPeriod = defaultReportingPeriod,
                    ),
                )

            assertThrows<InvalidInputApiException> { metaDataController.getAvailableDataDimensions() }

            val noMatchesExpected = metaDataController.getAvailableDataDimensions(listOf("dummy"), listOf("dummy"), listOf("dummy")).body
            assertTrue(noMatchesExpected.isNullOrEmpty())

            val allMatchesExpected = metaDataController.getAvailableDataDimensions(listOf(storedCompany.companyId), null, null).body
            assertTrue(allMatchesExpected == allDimensions)

            val filterForYear =
                dataMetaInformationManager
                    .getActiveDataDimensionsFromDatasets(DataDimensionFilter(reportingPeriods = listOf(nonDefaultReportingPeriod)))
            assertTrue(filterForYear.first() == allDimensions.first())

            val filterForCompanyId =
                dataMetaInformationManager
                    .getActiveDataDimensionsFromDatasets(DataDimensionFilter(companyIds = listOf(storedCompany.companyId)))
            assertTrue(filterForCompanyId == allDimensions)

            val filterForType =
                dataMetaInformationManager
                    .getActiveDataDimensionsFromDatasets(DataDimensionFilter(dataTypes = listOf(nonDefaultDataType)))
            assertTrue(filterForType.first() == allDimensions.last())
        }

        @Test
        fun `check that the resulting data dimensions are as expected when retrieving active datasets`() {
            val singleReportingPeriod = "2020"
            val singleDataType = "lksg"
            val storedCompanies = addCompanyToDatabase(3)
            addMetainformation(company = storedCompanies[0], reportingPeriod = singleReportingPeriod)
            addMetainformation(company = storedCompanies[1])
            addMetainformation(company = storedCompanies[2])
            addMetainformation(company = storedCompanies[0], currentlyActive = null, qaStatus = QaStatus.Rejected)
            addMetainformation(company = storedCompanies[0], dataType = singleDataType)
            val expectedDimensions =
                listOf(
                    BasicDataDimensions(
                        companyId = storedCompanies[0].companyId,
                        dataType = defaultDataType.toString(),
                        reportingPeriod = singleReportingPeriod,
                    ),
                    BasicDataDimensions(
                        companyId = storedCompanies[1].companyId,
                        dataType = defaultDataType.toString(),
                        reportingPeriod = defaultReportingPeriod,
                    ),
                    BasicDataDimensions(
                        companyId = storedCompanies[0].companyId,
                        dataType = singleDataType,
                        reportingPeriod = defaultReportingPeriod,
                    ),
                )
            val combinedSingleFilters =
                dataMetaInformationManager
                    .getActiveDataDimensionsFromDatasets(
                        DataDimensionFilter(
                            companyIds = listOf(storedCompanies[0].companyId),
                            dataTypes = listOf(defaultDataType.toString()),
                            reportingPeriods = listOf(singleReportingPeriod),
                        ),
                    )
            assertTrue(combinedSingleFilters.first() == expectedDimensions.first())

            val combinedMultipleFilters =
                dataMetaInformationManager
                    .getActiveDataDimensionsFromDatasets(
                        DataDimensionFilter(
                            companyIds = listOf(storedCompanies[0].companyId, storedCompanies[1].companyId),
                            dataTypes = listOf(defaultDataType.toString(), singleDataType),
                            reportingPeriods = listOf(singleReportingPeriod, defaultReportingPeriod),
                        ),
                    )
            assertTrue(combinedMultipleFilters == expectedDimensions)
        }

        @Test
        fun `check that data dimensions with datasets and datapoints are retrieved correctly`() {
            val testFramework = "test-framework"
            val testSpecification = SimpleFrameworkSpecification(framework = IdWithRef(id = testFramework, ref = "dummy"), name = "Test")
            doReturn(listOf(testSpecification)).whenever(specificationClient).listFrameworkSpecifications()
            doReturn(
                mock<FrameworkSpecification> {
                    on { schema } doReturn
                        "{\"category\":{\"subcategory\":{\"fieldName\":{\"id\":\"$defaultDataPointType\",\"ref\":\"dummy\"}}}}"
                },
            ).whenever(specificationClient).getFrameworkSpecification(testFramework)
            addMetainformation()
            addDataPointMetainformation()
            val allDimensions =
                listOf(
                    BasicDataDimensions(
                        companyId = storedCompany.companyId,
                        dataType = defaultDataType.toString(),
                        reportingPeriod = defaultReportingPeriod,
                    ),
                    BasicDataDimensions(
                        companyId = storedCompany.companyId,
                        dataType = defaultDataPointType,
                        reportingPeriod = defaultReportingPeriod,
                    ),
                    BasicDataDimensions(
                        companyId = storedCompany.companyId,
                        dataType = testFramework,
                        reportingPeriod = defaultReportingPeriod,
                    ),
                )

            val allMatchesExpected = metaDataController.getAvailableDataDimensions(listOf(storedCompany.companyId), null, null).body
            assertTrue(allMatchesExpected == allDimensions.subList(0, 2))

            val testFrameworkExpected = metaDataController.getAvailableDataDimensions(null, listOf(testFramework), null).body
            assertTrue(testFrameworkExpected == listOf(allDimensions.last()))
        }

        private fun addCompanyToDatabase(numberOfCompanies: Int): List<StoredCompanyEntity> {
            val storedCompanies = mutableListOf<StoredCompanyEntity>()
            testDataProvider.getCompanyInformationWithoutIdentifiers(numberOfCompanies).forEach {
                storedCompanies
                    .add(companyManager.addCompany(it))
            }
            return storedCompanies
        }

        @Suppress("LongParameterList")
        private fun addMetainformation(
            dataId: String? = null,
            company: StoredCompanyEntity? = null,
            userId: String? = null,
            uploadTime: Long? = null,
            dataType: String? = null,
            reportingPeriod: String? = null,
            currentlyActive: Boolean? = true,
            qaStatus: QaStatus? = null,
        ): DataMetaInformationEntity =
            dataMetaInformationManager.storeDataMetaInformation(
                DataMetaInformationEntity(
                    dataId = dataId ?: UUID.randomUUID().toString(),
                    company = company ?: storedCompany,
                    dataType = dataType ?: defaultDataType.toString(),
                    uploaderUserId = userId ?: uploaderUserId,
                    uploadTime = uploadTime ?: Random.nextLong(),
                    reportingPeriod = reportingPeriod ?: defaultReportingPeriod,
                    currentlyActive = currentlyActive,
                    qaStatus = qaStatus ?: QaStatus.Accepted,
                ),
            )

        private fun addDataPointMetainformation(): DataPointMetaInformationEntity =
            dataPointMetaInformationManager.storeDataPointMetaInformation(
                DataPointMetaInformationEntity(
                    dataPointId = UUID.randomUUID().toString(),
                    companyId = storedCompany.companyId,
                    dataPointType = defaultDataPointType,
                    reportingPeriod = defaultReportingPeriod,
                    uploaderUserId = uploaderUserId,
                    uploadTime = Random.nextLong(),
                    currentlyActive = true,
                    qaStatus = QaStatus.Accepted,
                ),
            )

        private fun assertMetaDataVisible(metaInfo: DataMetaInformationEntity) {
            val allMetaInformation =
                metaDataController
                    .getListOfDataMetaInfo(
                        companyId = metaInfo.company.companyId,
                        showOnlyActive = false,
                    ).body!!
            val metaInformation = metaDataController.getDataMetaInfo(metaInfo.dataId).body!!
            assertTrue(allMetaInformation.any { it.dataId == metaInfo.dataId })
            assertEquals(metaInformation.dataId, metaInfo.dataId)
        }

        private fun assertMetaDataNotVisible(metaInfo: DataMetaInformationEntity) {
            val allMetaInformation =
                metaDataController
                    .getListOfDataMetaInfo(
                        companyId = metaInfo.company.companyId,
                        showOnlyActive = false,
                    ).body!!
            assertFalse(allMetaInformation.any { it.dataId == metaInfo.dataId })
            assertThrows<AccessDeniedException> {
                metaDataController.getDataMetaInfo(metaInfo.dataId)
            }
        }

        private inline fun <reified T : Throwable> assertMetaDataNotPatchableWithException(
            metaInfo: DataMetaInformationEntity,
            patch: DataMetaInformationPatch,
        ) {
            assertThrows<T> {
                metaDataController.patchDataMetaInfo(metaInfo.dataId, patch)
            }
            val nonUpdatedMetaInfo = metaDataController.getDataMetaInfo(metaInfo.dataId).body!!
            assertEquals(metaInfo.uploaderUserId, nonUpdatedMetaInfo.uploaderUserId)
        }

        private fun mockSecurityContext(
            userId: String,
            roles: Set<DatalandRealmRole>,
        ) {
            val mockAuthentication = AuthenticationMock.mockJwtAuthentication("mocked_uploader", userId, roles)
            val mockSecurityContext = mock<SecurityContext>()
            whenever(mockSecurityContext.authentication).thenReturn(mockAuthentication)
            SecurityContextHolder.setContext(mockSecurityContext)
        }
    }
