package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.frameworks.sfdr.model.SfdrData
import org.dataland.datalandbackend.frameworks.vsme.model.VsmeData
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
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.random.Random

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
internal class MetaDataControllerTest(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyManager: CompanyAlterationManager,
    @Autowired private val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired private val metaDataController: MetaDataController,
    @Value("\${dataland.backend.proxy-primary-url}") private val proxyPrimaryUrl: String,
) {
    private lateinit var testCompanyInformation: CompanyInformation
    private lateinit var storedCompany: StoredCompanyEntity
    private val adminUserId = "admin-user-id"
    private val readerUserId = "reader-user-id"
    private val uploaderUserId = "uploader-user-id"
    private val defaultReportingPeriod = "2023"
    private val defaultDataType = DataType.of(SfdrData::class.java)

    val testDataProvider = TestDataProvider(objectMapper)
    private final val expectedSetOfRolesForReader = setOf(DatalandRealmRole.ROLE_USER)
    private final val expectedSetOfRolesForUploader =
        expectedSetOfRolesForReader +
            setOf(DatalandRealmRole.ROLE_UPLOADER)
    private final val expectedSetOfRolesForReviewer =
        expectedSetOfRolesForReader +
            setOf(DatalandRealmRole.ROLE_REVIEWER)
    private final val expectedSetOfRolesForAdmin =
        expectedSetOfRolesForReader + expectedSetOfRolesForUploader +
            expectedSetOfRolesForReviewer + setOf(DatalandRealmRole.ROLE_ADMIN)

    @BeforeEach
    fun setup() {
        testCompanyInformation = testDataProvider.getCompanyInformationWithoutIdentifiers(1).last()
        storedCompany = companyManager.addCompany(testCompanyInformation)
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
        mockSecurityContext(userId = adminUserId, roles = expectedSetOfRolesForAdmin)
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
        val emptyDataMetaInformationPatch =
            DataMetaInformationPatch(
                uploaderUserId = "",
            )
        val nullDataMetaInformationPatch = DataMetaInformationPatch(uploaderUserId = "")

        mockSecurityContext(userId = adminUserId, roles = expectedSetOfRolesForAdmin)
        assertMetaDataNotPatchableWithException<InvalidInputApiException>(metaInfo, emptyDataMetaInformationPatch)
        assertMetaDataNotPatchableWithException<InvalidInputApiException>(metaInfo, nullDataMetaInformationPatch)
    }

    @Test
    fun `ensure that meta info patch endpoint rejects vsme data`() {
        val metaInfo = addMetainformation(dataType = DataType.of(VsmeData::class.java).toString())
        mockSecurityContext(userId = adminUserId, roles = expectedSetOfRolesForAdmin)
        val mockDataMetaInformationPatch =
            mock<DataMetaInformationPatch> { on { uploaderUserId } doReturn uploaderUserId }
        assertMetaDataNotPatchableWithException<InvalidInputApiException>(metaInfo, mockDataMetaInformationPatch)
    }

    @Test
    fun `check that the active data endpoint works as expected for basic dataset related searches`() {
        mockSecurityContext(userId = adminUserId, roles = expectedSetOfRolesForAdmin)
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

        assertThrows<InvalidInputApiException> { metaDataController.getAvailableData() }

        val noMatchesExpected = metaDataController.getAvailableData(listOf("dummy"), listOf("dummy"), listOf("dummy")).body
        assertTrue(noMatchesExpected.isNullOrEmpty())

        val allMatchesExpected = metaDataController.getAvailableData(listOf(storedCompany.companyId), null, null).body
        assertTrue(allMatchesExpected == allDimensions)

        val filterForYear = dataMetaInformationManager.getAllActiveDatasets(null, null, listOf(nonDefaultReportingPeriod))
        assertTrue(filterForYear.first() == allDimensions.first())
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
        addMetainformation(company = storedCompanies[0], currentlyActive = null, qaStatus = QaStatus.Pending)
        addMetainformation(company = storedCompanies[0], dataType = singleDataType)
        val allDimensions =
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
                    companyId = storedCompanies[2].companyId,
                    dataType = defaultDataType.toString(),
                    reportingPeriod = defaultReportingPeriod,
                ),
                BasicDataDimensions(
                    companyId = storedCompanies[0].companyId,
                    dataType = singleDataType,
                    reportingPeriod = defaultReportingPeriod,
                ),
            )

        val filterForCompanyId = dataMetaInformationManager.getAllActiveDatasets(listOf(storedCompanies[0].companyId), null, null)
        assertTrue(filterForCompanyId == listOf(allDimensions.first(), allDimensions.last()))

        val filterForType = dataMetaInformationManager.getAllActiveDatasets(null, listOf(singleDataType), null)
        assertTrue(filterForType.first() == allDimensions.last())

        val combinedSingleFilters =
            dataMetaInformationManager
                .getAllActiveDatasets(
                    listOf(storedCompanies[0].companyId),
                    listOf(defaultDataType.toString()),
                    listOf(singleReportingPeriod),
                )
        assertTrue(combinedSingleFilters.first() == allDimensions.first())

        val combinedMultipleFilters =
            dataMetaInformationManager
                .getAllActiveDatasets(
                    listOf(storedCompanies[0].companyId, storedCompanies[1].companyId),
                    listOf(
                        defaultDataType.toString(),
                        singleDataType,
                    ),
                    listOf(singleReportingPeriod, defaultReportingPeriod),
                )
        assertTrue(combinedMultipleFilters == listOf(allDimensions[0], allDimensions[1], allDimensions[3]))
    }

    private fun addCompanyToDatabase(numberOfCompanies: Int): List<StoredCompanyEntity> {
        val storedCompanies = mutableListOf<StoredCompanyEntity>()
        testDataProvider.getCompanyInformationWithoutIdentifiers(numberOfCompanies).forEach {
            storedCompanies
                .add(companyManager.addCompany(it))
        }
        return storedCompanies
    }

    // Helper function that will crash if one of the optional parameters is explicitly set to null (except for currentlyActive)
    @Suppress("LongParameterList")
    private fun addMetainformation(
        dataId: String? = UUID.randomUUID().toString(),
        company: StoredCompanyEntity? = storedCompany,
        userId: String? = uploaderUserId,
        uploadTime: Long? = Random.nextLong(),
        dataType: String? = defaultDataType.toString(),
        reportingPeriod: String? = defaultReportingPeriod,
        currentlyActive: Boolean? = true,
        qaStatus: QaStatus? = QaStatus.Accepted,
    ): DataMetaInformationEntity =
        dataMetaInformationManager.storeDataMetaInformation(
            DataMetaInformationEntity(
                dataId = dataId!!,
                company = company!!,
                dataType = dataType!!,
                uploaderUserId = userId!!,
                uploadTime = uploadTime!!,
                reportingPeriod = reportingPeriod!!,
                currentlyActive = currentlyActive,
                qaStatus = qaStatus!!,
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
        val mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "mocked_uploader",
                userId,
                roles,
            )
        val mockSecurityContext = Mockito.mock(SecurityContext::class.java)
        Mockito.`when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        SecurityContextHolder.setContext(mockSecurityContext)
    }
}
