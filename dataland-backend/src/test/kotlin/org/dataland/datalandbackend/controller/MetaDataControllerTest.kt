package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
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
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.services.utils.TestPostgresContainer
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.random.Random

@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.rabbitmq.listener.simple.auto-startup=false"],
)
@Transactional
@Rollback
internal class MetaDataControllerTest
    @Suppress("LongParameterList")
    @Autowired
    constructor(
        private val objectMapper: ObjectMapper,
        private val companyManager: CompanyAlterationManager,
        private val dataMetaInformationManager: DataMetaInformationManager,
        private val metaDataController: MetaDataController,
        @Value("\${dataland.backend.proxy-primary-url}") private val proxyPrimaryUrl: String,
    ) {
        companion object {
            @DynamicPropertySource
            @JvmStatic
            fun configureProperties(registry: DynamicPropertyRegistry) {
                TestPostgresContainer.configureProperties(registry)
            }
        }

        private lateinit var testCompanyInformation: CompanyInformation
        private lateinit var storedCompany: StoredCompanyEntity
        private val adminUserId = "admin-user-id"
        private val readerUserId = "reader-user-id"
        private val uploaderUserId = "uploader-user-id"
        private val defaultReportingPeriod = "2023"
        private val defaultDataType = DataType.of(SfdrData::class.java)

        val testDataProvider = TestDataProvider(objectMapper)
        private final val expectedSetOfRolesForReader = setOf(DatalandRealmRole.ROLE_USER)
        private final val expectedSetOfRolesForUploader = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER)
        private final val expectedSetOfRolesForReviewer = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_REVIEWER)
        private final val expectedSetOfRolesForAdmin = DatalandRealmRole.entries.toSet()

        @MockitoBean
        private var specificationClient = mock<SpecificationControllerApi>()

        @BeforeEach
        fun setup() {
            whenever(specificationClient.listFrameworkSpecifications()).thenReturn(emptyList())
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
                    .getActiveDataMetaInformationList(
                        DataDimensionFilter(
                            companyIds = listOf(storedCompanies[0].companyId),
                            dataTypes = listOf(defaultDataType.toString()),
                            reportingPeriods = listOf(singleReportingPeriod),
                        ),
                    ).map { it.toBasicDataDimensions() }
            assertTrue(combinedSingleFilters.first() == expectedDimensions.first())

            val combinedMultipleFilters =
                dataMetaInformationManager
                    .getActiveDataMetaInformationList(
                        DataDimensionFilter(
                            companyIds = listOf(storedCompanies[0].companyId, storedCompanies[1].companyId),
                            dataTypes = listOf(defaultDataType.toString(), singleDataType),
                            reportingPeriods = listOf(singleReportingPeriod, defaultReportingPeriod),
                        ),
                    ).map { it.toBasicDataDimensions() }
            assertTrue(combinedMultipleFilters == expectedDimensions)
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
