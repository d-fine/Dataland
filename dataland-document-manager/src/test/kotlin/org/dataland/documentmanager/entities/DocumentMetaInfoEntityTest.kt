package org.dataland.documentmanager.entities

import org.dataland.datalandbackendutils.model.DocumentCategory
import org.dataland.datalandbackendutils.model.DocumentType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

class DocumentMetaInfoEntityTest {
    private val mockSecurityContext = mock<SecurityContext>()
    private lateinit var documentMetaInfoEntity: DocumentMetaInfoEntity

    @BeforeEach
    fun setUp() {
        reset(mockSecurityContext)
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = ';',
        value = [
            "Accepted;dummy-user-id;[ROLE_ADMIN,ROLE_REVIEWER]",
        ],
    )
    fun `check that the method isViewableByUser shows the correct output behavior`(
        qaStatus: QaStatus,
        uploaderId: String,
        userRoles: Set<DatalandRealmRole>,
    ) {
        val mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                username = "data_uploader",
                userId = "dummy-user-id",
                roles = userRoles,
            )

        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)

        documentMetaInfoEntity =
            DocumentMetaInfoEntity(
                documentId = "dummy-document-id",
                documentType = DocumentType.Pdf,
                documentName = "dummy-document",
                documentCategory = DocumentCategory.AnnualReport,
                companyIds = mutableSetOf("dummy-company-id"),
                uploaderId = uploaderId,
                uploadTime = 0L,
                publicationDate = null,
                reportingPeriod = null,
                qaStatus = qaStatus,
            )
    }
}
