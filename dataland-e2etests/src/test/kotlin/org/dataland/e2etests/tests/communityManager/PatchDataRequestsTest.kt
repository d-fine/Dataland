package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.RequestPriority
import org.dataland.communitymanager.openApiClient.model.RequestStatus
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.communityManager.assertAdminCommentForDataRequestId
import org.dataland.e2etests.utils.communityManager.assertPriorityForDataRequestId
import org.dataland.e2etests.utils.communityManager.assertStatusForDataRequestId
import org.dataland.e2etests.utils.communityManager.getIdForUploadedCompanyWithIdentifiers
import org.dataland.e2etests.utils.communityManager.getNewlyStoredRequestsAfterTimestamp
import org.dataland.e2etests.utils.communityManager.patchDataRequestAdminCommentAndAssertLastModifiedNotUpdated
import org.dataland.e2etests.utils.communityManager.patchDataRequestAndAssertNewStatusAndLastModifiedUpdated
import org.dataland.e2etests.utils.communityManager.patchDataRequestPriorityAndAssertLastModifiedUpdated
import org.dataland.e2etests.utils.communityManager.postStandardSingleDataRequest
import org.dataland.e2etests.utils.communityManager.retrieveTimeAndWaitOneMillisecond
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PatchDataRequestsTest {
    val apiAccessor = ApiAccessor()
    val jwtHelper = JwtAuthenticationHelper()
    private val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    private val clientErrorMessage403 = "Client error : 403 "
    private var timestampBeforeSingleRequest: Long = 0
    private var dataRequestId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        val companyId = getIdForUploadedCompanyWithIdentifiers(permId = System.currentTimeMillis().toString())
        this.timestampBeforeSingleRequest = retrieveTimeAndWaitOneMillisecond()
        postStandardSingleDataRequest(companyId)
        this.dataRequestId =
            UUID.fromString(
                getNewlyStoredRequestsAfterTimestamp(timestampBeforeSingleRequest)[0].dataRequestId,
            )
    }

    @Test
    fun `post a single data request and check if patching it changes its status accordingly`() {
        assertStatusForDataRequestId(dataRequestId, RequestStatus.Open)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.Answered)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.Resolved)
        patchDataRequestAndAssertNewStatusAndLastModifiedUpdated(dataRequestId, RequestStatus.Withdrawn)
    }

    @Test
    fun `post a single data request and validate that patching the admin comment does not update the last modified date`() {
        val testAdminComment = "test"
        assertPriorityForDataRequestId(dataRequestId, RequestPriority.Normal)
        assertAdminCommentForDataRequestId(dataRequestId, null)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestAdminCommentAndAssertLastModifiedNotUpdated(dataRequestId, testAdminComment)
    }

    @Test
    fun `post a single data request and validate that patching the request priority updates the last modified date`() {
        val testRequestPriority = RequestPriority.High
        assertPriorityForDataRequestId(dataRequestId, RequestPriority.Normal)
        assertAdminCommentForDataRequestId(dataRequestId, null)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        patchDataRequestPriorityAndAssertLastModifiedUpdated(dataRequestId, testRequestPriority)
    }

    @Test
    fun `validate that patching the admin comment as a normal user is forbidden`() {
        val testAdminComment = "test"
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val clientException =
            assertThrows<ClientException> {
                requestControllerApi.patchDataRequest(dataRequestId, adminComment = testAdminComment)
            }
        assertEquals(clientErrorMessage403, clientException.message)
    }

    @Test
    fun `validate that patching the request priority as a normal user is forbidden`() {
        val testRequestPriority = RequestPriority.High
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.PremiumUser)
        val clientException =
            assertThrows<ClientException> {
                requestControllerApi.patchDataRequest(dataRequestId, requestPriority = testRequestPriority)
            }
        assertEquals(clientErrorMessage403, clientException.message)
    }
}
