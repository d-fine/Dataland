package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequestResponse
import org.dataland.datalandcommunitymanager.services.BulkDataRequestManager
import org.dataland.datalandcommunitymanager.services.CompanyRolesManager
import org.dataland.datalandcommunitymanager.services.DataAccessManager
import org.dataland.datalandcommunitymanager.services.DataRequestQueryManager
import org.dataland.datalandcommunitymanager.services.DataRequestUpdateManager
import org.dataland.datalandcommunitymanager.services.SingleDataRequestManager
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class RequestControllerTest {
    private val mockBulkDataRequestManager = mock<BulkDataRequestManager>()
    private val mockSingleDataRequestManager = mock<SingleDataRequestManager>()
    private val mockDataRequestQueryManager = mock<DataRequestQueryManager>()
    private val mockDataRequestUpdateManager = mock<DataRequestUpdateManager>()
    private val mockDataAccessManager = mock<DataAccessManager>()
    private val mockCompanyRolesManager = mock<CompanyRolesManager>()
    private val requestController =
        RequestController(
            mockBulkDataRequestManager,
            mockSingleDataRequestManager,
            mockDataRequestQueryManager,
            mockDataRequestUpdateManager,
            mockDataAccessManager,
            mockCompanyRolesManager,
        )

    @BeforeEach
    fun setup() {
        Mockito.reset(
            mockBulkDataRequestManager,
            mockSingleDataRequestManager,
            mockDataRequestQueryManager,
            mockDataRequestUpdateManager,
            mockDataAccessManager,
            mockCompanyRolesManager,
        )
    }

    @Test
    fun `check that a non admin user cannot impersonate another user when posting a single data request`() {
        val impersonatedUserId = UUID.randomUUID().toString()
        val nonAdminUserRoles = DatalandRealmRole.entries.toMutableSet()
        nonAdminUserRoles.remove(DatalandRealmRole.ROLE_ADMIN)
        TestUtils.mockSecurityContext("username", "userId", nonAdminUserRoles)
        val mockSingleDataRequest = mock<SingleDataRequest>()
        assertThrows<InsufficientRightsApiException> {
            requestController.postSingleDataRequest(
                mockSingleDataRequest,
                impersonatedUserId,
            )
        }
    }

    @Test
    fun `check that an admin user can impersonate another user when posting a single data request`() {
        val impersonatedUserId = UUID.randomUUID().toString()
        TestUtils.mockSecurityContext("username", "userId", DatalandRealmRole.ROLE_ADMIN)
        val mockSingleDataRequestResponse = mock<SingleDataRequestResponse>()
        doReturn(mockSingleDataRequestResponse).whenever(mockSingleDataRequestManager).processSingleDataRequest(
            any(),
            any(),
        )
        val mockSingleDataRequest = mock<SingleDataRequest>()
        requestController.postSingleDataRequest(
            mockSingleDataRequest,
            impersonatedUserId,
        )
        verify(mockSingleDataRequestManager).processSingleDataRequest(
            mockSingleDataRequest,
            impersonatedUserId,
        )
    }
}
