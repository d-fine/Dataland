package org.dataland.documentmanager.controller

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.documentmanager.DatalandDocumentManager
import org.dataland.documentmanager.model.DocumentMetaInfoPatch
import org.dataland.documentmanager.model.DocumentUploadResponse
import org.dataland.documentmanager.services.DocumentManager
import org.dataland.documentmanager.services.UserRolesChecker
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = [DatalandDocumentManager::class], properties = ["spring.profiles.active=nodb"])
class DocumentControllerTest(
    @Autowired private val documentController: DocumentController,
) {
    @MockitoBean
    private val mockDocumentManager = mock<DocumentManager>()

    @MockitoBean
    private val mockCompanyDataControllerApi = mock<CompanyDataControllerApi>()

    @MockitoBean
    private val mockUserRolesChecker = mock<UserRolesChecker>()

    private final val dummyUserName = "dummyUserName"
    private final val dummyUserId = "dummyUserId"
    private final val initialUploaderUserId = "initialUploaderUserId"
    private final val otherUploaderUserId = "otherUploaderUserId"
    private final val dummyCompanyId = "dummyCompanyId"
    private final val cumulatedRolesForReader = setOf(DatalandRealmRole.ROLE_USER)
    private final val cumulatedRolesForUploader = cumulatedRolesForReader.plus(DatalandRealmRole.ROLE_UPLOADER)
    private final val cumulatedRolesForReviewer =
        cumulatedRolesForReader.plus(DatalandRealmRole.ROLE_REVIEWER)
    private final val cumulatedRolesForAdmin =
        cumulatedRolesForReader + cumulatedRolesForUploader + cumulatedRolesForReviewer + DatalandRealmRole.ROLE_ADMIN

    @BeforeEach
    fun setup() {
        reset(mockDocumentManager)
        doNothing().whenever(mockCompanyDataControllerApi).isCompanyIdValid(any())
        doReturn(mock<DocumentUploadResponse>())
            .whenever(mockDocumentManager)
            .patchDocumentMetaInformationCompanyIds(any(), any())
    }

    private fun setMockSecurityContext(
        userName: String,
        userId: String,
        roles: Set<DatalandRealmRole>,
    ) {
        val mockAuthentication = AuthenticationMock.mockJwtAuthentication(userName, userId, roles)
        val mockSecurityContext = mock<SecurityContext>()
        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `test that patch endpoints are accessible for admins`() {
        val dummyDocumentID = UUID.randomUUID().toString()
        val mockDocumentMetaInfoPatch = mock<DocumentMetaInfoPatch>()

        setMockSecurityContext(userName = dummyUserName, userId = dummyUserId, roles = cumulatedRolesForAdmin)
        assertDoesNotThrow {
            documentController.patchDocumentMetaInfo(dummyDocumentID, mockDocumentMetaInfoPatch)
        }
        assertDoesNotThrow {
            documentController.patchDocumentMetaInfoCompanyIds(dummyDocumentID, dummyCompanyId)
        }
    }

    @Test
    fun `test that patch endpoints are not accessible for reader nor reviewers`() {
        val dummyDocumentID = UUID.randomUUID().toString()
        val mockDocumentMetaInfoPatch = mock<DocumentMetaInfoPatch>()

        for (setOfRoles in listOf(cumulatedRolesForReader, cumulatedRolesForReviewer)) {
            setMockSecurityContext(userName = dummyUserName, userId = dummyUserId, roles = setOfRoles)
            assertThrows<AccessDeniedException> {
                documentController.patchDocumentMetaInfo(dummyDocumentID, mockDocumentMetaInfoPatch)
            }
            assertThrows<AccessDeniedException> {
                documentController.patchDocumentMetaInfoCompanyIds(dummyDocumentID, dummyCompanyId)
            }
        }
    }

    @Test
    fun `test that companyIds patch endpoint is accessible for uploaders`() {
        val dummyDocumentID = UUID.randomUUID().toString()

        setMockSecurityContext(userName = dummyUserName, userId = dummyUserId, roles = cumulatedRolesForUploader)
        assertDoesNotThrow {
            documentController.patchDocumentMetaInfoCompanyIds(dummyDocumentID, dummyCompanyId)
        }
    }

    @Test
    fun `test that full patch endpoint is accessible for initial uploader of document`() {
        val dummyDocumentID = UUID.randomUUID().toString()
        val mockDocumentMetaInfoPatch = mock<DocumentMetaInfoPatch>()

        doReturn(true).whenever(mockUserRolesChecker).isCurrentUserUploaderOfDocumentWithId(dummyDocumentID)
        setMockSecurityContext(userName = dummyUserName, userId = initialUploaderUserId, roles = cumulatedRolesForUploader)
        assertDoesNotThrow {
            documentController.patchDocumentMetaInfo(dummyDocumentID, mockDocumentMetaInfoPatch)
        }
    }

    @Test
    fun `test that full patch endpoint is not accessible for uploaders which are not initial uploader of document`() {
        val dummyDocumentID = UUID.randomUUID().toString()
        val mockDocumentMetaInfoPatch = mock<DocumentMetaInfoPatch>()

        doReturn(false).whenever(mockUserRolesChecker).isCurrentUserUploaderOfDocumentWithId(dummyDocumentID)
        setMockSecurityContext(
            userName = dummyUserName,
            userId = otherUploaderUserId,
            roles = cumulatedRolesForUploader,
        )
        assertThrows<AccessDeniedException> {
            documentController.patchDocumentMetaInfo(dummyDocumentID, mockDocumentMetaInfoPatch)
        }
    }
}
