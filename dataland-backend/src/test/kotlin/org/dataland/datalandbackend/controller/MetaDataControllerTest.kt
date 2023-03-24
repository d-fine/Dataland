package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.enums.data.QAStatus
import org.dataland.datalandbackend.model.lksg.LksgData
import org.dataland.datalandbackend.services.CompanyManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.utils.AuthenticationMock
import org.dataland.datalandbackend.utils.CompanyUploader
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [DatalandBackend::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles(profiles = ["unprotected"])
internal class MetaDataControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyManager: CompanyManager,
    @Autowired private val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired private val metaDataController: MetaDataController,
) {
    val testDataProvider = TestDataProvider(objectMapper)

    val expectedSetOfRolesForReader = setOf(DatalandRealmRole.ROLE_USER)
    val expectedSetOfRolesForUploader = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER)
    val expectedSetOfRolesForAdmin =
        setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER, DatalandRealmRole.ROLE_ADMIN)

    @Test
    fun `list of meta info about data for specific company can be retrieved`() {
        val testCompanyInformation = testDataProvider.getCompanyInformationWithoutIdentifiers(1).last()
        val storedCompany = CompanyUploader().uploadCompany(mockMvc, objectMapper, testCompanyInformation)
        mockMvc.perform(
            get("/metadata?companyId=${storedCompany.companyId}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
                content().string("[]"),
            )
    }

    @Test
    fun `ensure that meta info about a pending dataset can only be retrieved by authorized users`() {
        val testCompanyInformation = testDataProvider.getCompanyInformationWithoutIdentifiers(1).last()
        val storedCompany = companyManager.addCompany(testCompanyInformation)
        val metaInfo = dataMetaInformationManager.storeDataMetaInformation(
            DataMetaInformationEntity(
                dataId = "data-id-for-testing-user-access", company = storedCompany,
                dataType = DataType.of(LksgData::class.java).toString(), uploaderUserId = "uploader-user-id",
                uploadTime = 0, reportingPeriod = "reporting-period", currentlyActive = null,
                qaStatus = QAStatus.Pending,
            ),
        )
        mockSecurityContext(userId = "reader-user-id", roles = expectedSetOfRolesForReader)
        assertMetaDataNotVisible(metaInfo)
        mockSecurityContext(userId = "uploader-user-id", roles = expectedSetOfRolesForUploader)
        assertMetaDataVisible(metaInfo)
        mockSecurityContext(userId = "admin-user-id", roles = expectedSetOfRolesForAdmin)
    }

    private fun assertMetaDataVisible(metaInfo: DataMetaInformationEntity) {
        val allMetaInformation = metaDataController.getListOfDataMetaInfo(
            companyId = metaInfo.company.companyId,
            showOnlyActive = false,
        ).body!!
        val metaInformation = metaDataController.getDataMetaInfo(metaInfo.dataId).body!!
        assertTrue(allMetaInformation.any { it.dataId == metaInfo.dataId })
        assertEquals(metaInformation.dataId, metaInfo.dataId)
    }

    private fun assertMetaDataNotVisible(metaInfo: DataMetaInformationEntity) {
        val allMetaInformation = metaDataController.getListOfDataMetaInfo(
            companyId = metaInfo.company.companyId,
            showOnlyActive = false,
        ).body!!
        assertFalse(allMetaInformation.any { it.dataId == metaInfo.dataId })
        assertThrows<AccessDeniedException> {
            metaDataController.getDataMetaInfo(metaInfo.dataId)
        }
    }

    private fun mockSecurityContext(userId: String, roles: Set<DatalandRealmRole>) {
        val mockAuthentication = AuthenticationMock.mockJwtAuthentication(
            "mocked_uploader",
            userId,
            roles,
        )
        val mockSecurityContext = Mockito.mock(SecurityContext::class.java)
        Mockito.`when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        SecurityContextHolder.setContext(mockSecurityContext)
    }
}
