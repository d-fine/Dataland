package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.frameworks.sfdr.model.SfdrData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.DataMetaInformationRequest
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

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

    @Test
    fun `ensure that meta info about a pending dataset can only be retrieved by authorized users`() {
        val testCompanyInformation = testDataProvider.getCompanyInformationWithoutIdentifiers(1).last()
        val storedCompany = companyManager.addCompany(testCompanyInformation)
        val metaInfo =
            dataMetaInformationManager.storeDataMetaInformation(
                DataMetaInformationEntity(
                    dataId = "data-id-for-testing-user-access", company = storedCompany,
                    dataType = DataType.of(LksgData::class.java).toString(), uploaderUserId = "uploader-user-id",
                    uploadTime = 0, reportingPeriod = "reporting-period", currentlyActive = null,
                    qaStatus = QaStatus.Pending,
                ),
            )
        mockSecurityContext(userId = "reader-user-id", roles = expectedSetOfRolesForReader)
        assertMetaDataNotVisible(metaInfo)
        mockSecurityContext(userId = "uploader-user-id", roles = expectedSetOfRolesForUploader)
        assertMetaDataVisible(metaInfo)
        mockSecurityContext(userId = "admin-user-id", roles = expectedSetOfRolesForAdmin)
        assertMetaDataVisible(metaInfo)
    }

    @Test
    fun `ensure that meta info about a rejected dataset can only be retrieved by authorized users`() {
        val testCompanyInformation = testDataProvider.getCompanyInformationWithoutIdentifiers(1).last()
        val storedCompany = companyManager.addCompany(testCompanyInformation)
        val metaInfo =
            dataMetaInformationManager.storeDataMetaInformation(
                DataMetaInformationEntity(
                    dataId = "data-id-for-testing-user-access-to-rejected-datasets", company = storedCompany,
                    dataType = DataType.of(SfdrData::class.java).toString(),
                    uploaderUserId = "uploader-user-id-of-rejected-dataset",
                    uploadTime = 0, reportingPeriod = "reporting-period", currentlyActive = null,
                    qaStatus = QaStatus.Rejected,
                ),
            )
        mockSecurityContext(userId = "reader-user-id", roles = expectedSetOfRolesForReader)
        assertMetaDataNotVisible(metaInfo)
        mockSecurityContext(userId = "uploader-user-id-of-rejected-dataset", roles = expectedSetOfRolesForUploader)
        assertMetaDataVisible(metaInfo)
        mockSecurityContext(userId = "different-uploader-user-id", roles = expectedSetOfRolesForUploader)
        assertMetaDataNotVisible(metaInfo)
        mockSecurityContext(userId = "admin-user-id", roles = expectedSetOfRolesForAdmin)
        assertMetaDataVisible(metaInfo)
        mockSecurityContext(userId = "reviewer-user-id", roles = expectedSetOfRolesForAdmin)
        assertMetaDataVisible(metaInfo)
    }

    @Test
    fun `check if DataMetaInformationRequest is correctly transformed into DataMetaInformation`() {
        val dataId = "data-id-for-testing-postListOfDataMetaInfoFilters"
        val dataType = DataType.of(SfdrData::class.java)
        val reportingPeriod = "2022"
        val uploaderUserId = UUID.randomUUID()
        val qaStatus = QaStatus.Accepted
        val amountStoredCompanies = 2
        val testCompanyInformation = testDataProvider.getCompanyInformationWithoutIdentifiers(amountStoredCompanies)
        val storedCompany1 = companyManager.addCompany(testCompanyInformation[0])
        val storedCompany2 = companyManager.addCompany(testCompanyInformation[1])
        val companyId1 = storedCompany1.companyId
        val companyId2 = storedCompany2.companyId
        val url = "https://$proxyPrimaryUrl/companies/$companyId1/frameworks/$dataType/$dataId"
        val dataMetaInformationRequest =
            listOf(
                DataMetaInformationRequest(
                    companyId1, dataType, true, reportingPeriod, setOf(uploaderUserId), qaStatus,
                ),
                DataMetaInformationRequest(
                    companyId2, dataType, true, reportingPeriod, setOf(uploaderUserId), qaStatus,
                ),
            )
        dataMetaInformationManager.storeDataMetaInformation(
            DataMetaInformationEntity(
                dataId, company = storedCompany1, dataType.toString(), uploaderUserId.toString(),
                uploadTime = 0, reportingPeriod, currentlyActive = true, qaStatus,
            ),
        )
        dataMetaInformationManager.storeDataMetaInformation(
            DataMetaInformationEntity(
                "dataId", company = storedCompany2, dataType.toString(), uploaderUserId.toString(),
                uploadTime = 0, reportingPeriod, currentlyActive = true, qaStatus,
            ),
        )
        mockSecurityContext(userId = "admin-user-id", roles = expectedSetOfRolesForAdmin)
        val listDataMetaInfos = metaDataController.postListOfDataMetaInfoFilters(dataMetaInformationRequest).body

        assertEquals(amountStoredCompanies, listDataMetaInfos?.size)
        val dataMetaInfo = listDataMetaInfos?.get(0)
        if (dataMetaInfo != null) {
            assertEquals(companyId1, dataMetaInfo.companyId)
            assertEquals(dataType, dataMetaInfo.dataType)
            assertEquals(qaStatus, dataMetaInfo.qaStatus)
            assertTrue(dataMetaInfo.currentlyActive)
            assertEquals(uploaderUserId.toString(), dataMetaInfo.uploaderUserId)
            assertEquals(url, dataMetaInfo.url)
        }
    }

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
