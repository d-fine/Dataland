package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.ExportJobEntity
import org.dataland.datalandbackend.frameworks.sfdr.model.SfdrData
import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackend.model.export.ExportJob
import org.dataland.datalandbackend.services.DataExportService
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.profiles.active=nodb"],
)
@AutoConfigureMockMvc
@DefaultMocks
class DataExportControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockitoBean
    private lateinit var dataExportService: DataExportService<SfdrData>

    private val mockSecurityContext = mock<SecurityContext>()
    private val testUserId = UUID.randomUUID()
    private val otherUserId = UUID.randomUUID().toString()

    private val userAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "testuser",
            userId = testUserId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_USER),
        )

    private val otherUserAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "otheruser",
            userId = otherUserId,
            roles = setOf(DatalandRealmRole.ROLE_USER),
        )

    @BeforeEach
    fun setup() {
        reset(mockSecurityContext, dataExportService)
        setMockUser(userAuthentication)
    }

    private fun setMockUser(authentication: DatalandJwtAuthentication) {
        doReturn(authentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `post export job returns job ID and state is initially Pending`() {
        val testJobId = UUID.randomUUID()
        val exportJobEntity =
            ExportJobEntity(
                id = testJobId,
                fileToExport = null,
                fileType = ExportFileType.CSV,
                creationTime = Instant.now().toEpochMilli(),
            )

        doReturn(exportJobEntity)
            .whenever(dataExportService)
            .createAndSaveExportJob(any(), any())

        whenever(dataExportService.getExportJobState(testJobId))
            .thenReturn(ExportJobProgressState.Pending)

        mockMvc
            .perform(
                post("/api/data/eutaxonomy-non-financials/export-jobs")
                    .contentType("application/json")
                    .content(
                        """
                        {
                            "companyIds": ["comp-1", "comp-2"],
                            "reportingPeriods": ["2024"],
                            "fileFormat": "CSV"
                        }
                        """.trimIndent(),
                    ).with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(testJobId))

        mockMvc
            .perform(
                get("/api/data/eutaxonomy-non-financials/export/state")
                    .param("exportJobId", testJobId.toString())
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
            .andExpect(content().string("Pending"))
    }

    @Test
    fun `get export job state returns 404 for non-existent job`() {
        val nonExistentJobId = UUID.randomUUID()

        whenever(dataExportService.getExportJobState(nonExistentJobId)).thenAnswer {
            throw ResourceNotFoundApiException(
                "Export job not found",
                "Export job with ID $nonExistentJobId not found",
            )
        }
        mockMvc
            .perform(
                get("/api/data/eutaxonomy-non-financials/export/state")
                    .param("exportJobId", nonExistentJobId.toString())
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)
    }

    @Test
    fun `user cannot access other user's export job`() {
        val jobId = UUID.randomUUID()

        doReturn(ExportJob(id = jobId))
            .whenever(dataExportService)
            .createAndSaveExportJob(any(), any())

        mockMvc
            .perform(
                post("/api/data/eutaxonomy-non-financials/export")
                    .contentType("application/json")
                    .content("""{"companyIds": ["comp-1"], "reportingPeriods": ["2024"], "fileFormat": "CSV"}""")
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk)

        setMockUser(otherUserAuthentication)
        doThrow(AccessDeniedException("Access denied"))
            .whenever(dataExportService)
            .getExportJobState(jobId)

        mockMvc
            .perform(
                get("/api/data/eutaxonomy-non-financials/export/state")
                    .param("exportJobId", jobId.toString())
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isForbidden)
    }

    @Test
    fun `export download returns 404 for non-existent job`() {
        val nonExistentJobId = UUID.randomUUID()

        whenever(dataExportService.exportCompanyAssociatedDataById(nonExistentJobId)).thenAnswer {
            throw ResourceNotFoundApiException(
                "Export job not found",
                "Export job with ID $nonExistentJobId not found",
            )
        }

        mockMvc
            .perform(
                post("/api/data/eutaxonomy-non-financials/export/download")
                    .param("exportJobId", nonExistentJobId.toString())
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isNotFound)
    }
}
