package org.dataland.documentmanager.controller

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.documentmanager.DatalandDocumentManager
import org.dataland.documentmanager.model.DocumentMetaInfoPatch
import org.dataland.documentmanager.model.DocumentMetaInfoResponse
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
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean

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
    private final val dummyDocumentId = "dummyDocumentId"
    private final val mockDocumentMetaInfoPatch = mock<DocumentMetaInfoPatch>()
    private final val cumulatedRolesForReader = setOf(DatalandRealmRole.ROLE_USER)
    private final val cumulatedRolesForUploader = cumulatedRolesForReader.plus(DatalandRealmRole.ROLE_UPLOADER)
    private final val cumulatedRolesForReviewer =
        cumulatedRolesForReader.plus(DatalandRealmRole.ROLE_REVIEWER)
    private final val cumulatedRolesForAdmin =
        cumulatedRolesForReader + cumulatedRolesForUploader + cumulatedRolesForReviewer + DatalandRealmRole.ROLE_ADMIN

    @BeforeEach
    fun setup() {
        reset(mockDocumentManager, mockDocumentMetaInfoPatch)
        doNothing().whenever(mockCompanyDataControllerApi).isCompanyIdValid(any())
        doReturn(mock<DocumentMetaInfoResponse>())
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
        setMockSecurityContext(userName = dummyUserName, userId = dummyUserId, roles = cumulatedRolesForAdmin)
        assertDoesNotThrow {
            documentController.patchDocumentMetaInfo(dummyDocumentId, mockDocumentMetaInfoPatch)
        }
        assertDoesNotThrow {
            documentController.patchDocumentMetaInfoCompanyIds(dummyDocumentId, dummyCompanyId)
        }
    }

    @Test
    fun `test that patch endpoints are not accessible for reader nor reviewers`() {
        for (setOfRoles in listOf(cumulatedRolesForReader, cumulatedRolesForReviewer)) {
            setMockSecurityContext(userName = dummyUserName, userId = dummyUserId, roles = setOfRoles)
            assertThrows<AccessDeniedException> {
                documentController.patchDocumentMetaInfo(dummyDocumentId, mockDocumentMetaInfoPatch)
            }
            assertThrows<AccessDeniedException> {
                documentController.patchDocumentMetaInfoCompanyIds(dummyDocumentId, dummyCompanyId)
            }
        }
    }

    @Test
    fun `test that companyIds patch endpoint is accessible for uploaders`() {
        setMockSecurityContext(userName = dummyUserName, userId = dummyUserId, roles = cumulatedRolesForUploader)
        assertDoesNotThrow {
            documentController.patchDocumentMetaInfoCompanyIds(dummyDocumentId, dummyCompanyId)
        }
    }

    @Test
    fun `test that full patch endpoint is accessible for initial uploader of document`() {
        doReturn(true).whenever(mockUserRolesChecker).isCurrentUserUploaderOfDocumentWithId(dummyDocumentId)
        setMockSecurityContext(
            userName = dummyUserName,
            userId = initialUploaderUserId,
            roles = cumulatedRolesForUploader,
        )
        assertDoesNotThrow {
            documentController.patchDocumentMetaInfo(dummyDocumentId, mockDocumentMetaInfoPatch)
        }
    }

    @Test
    fun `test that full patch endpoint is not accessible for uploaders which are not initial uploader of document`() {
        doReturn(false).whenever(mockUserRolesChecker).isCurrentUserUploaderOfDocumentWithId(dummyDocumentId)
        setMockSecurityContext(
            userName = dummyUserName,
            userId = otherUploaderUserId,
            roles = cumulatedRolesForUploader,
        )
        assertThrows<AccessDeniedException> {
            documentController.patchDocumentMetaInfo(dummyDocumentId, mockDocumentMetaInfoPatch)
        }
    }

    @Test
    fun `test that patching meta info throws error if document does not exist`() {
        mockDocumentMetaInfoPatch.apply { doReturn(true).whenever(this).isNullOrEmpty() }

        setMockSecurityContext(
            userName = dummyUserName,
            userId = dummyUserName,
            roles = cumulatedRolesForAdmin,
        )
        assertThrows<InvalidInputApiException> {
            documentController.patchDocumentMetaInfo(dummyDocumentId, mockDocumentMetaInfoPatch)
        }
    }

    @Test
    fun `test that resource not found exception is thrown if companyId does not exist`() {
        val mockClientException = mock<ClientException> { on { statusCode } doReturn HttpStatus.NOT_FOUND.value() }
        mockCompanyDataControllerApi.apply { doThrow(mockClientException).whenever(this).isCompanyIdValid(any()) }

        setMockSecurityContext(
            userName = dummyUserName,
            userId = dummyUserName,
            roles = cumulatedRolesForUploader,
        )
        assertThrows<ResourceNotFoundApiException> {
            documentController.patchDocumentMetaInfoCompanyIds(dummyDocumentId, dummyCompanyId)
        }
    }
}
