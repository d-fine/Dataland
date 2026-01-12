package org.dataland.e2etests.tests

import org.awaitility.Awaitility.await
import org.dataland.accountingService.openApiClient.model.TransactionPost
import org.dataland.communitymanager.openApiClient.model.CompanyRightAssignmentString
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.dataSourcingService.openApiClient.model.SingleRequest
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.GlobalAuth.jwtHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.CompanyRightsTestUtils
import org.dataland.e2etests.utils.CompanyRolesTestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.time.Duration
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountingServiceTest {
    private val apiAccessor = ApiAccessor()
    private val companyRightsTestUtils = CompanyRightsTestUtils()
    private val companyRolesTestUtils = CompanyRolesTestUtils()

    private fun uploadCompanyAsUploader(): String =
        GlobalAuth.withTechnicalUser(TechnicalUser.Uploader) {
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        }

    private fun assignCompanyOwnerRole(
        companyId: String,
        userId: UUID,
    ) = GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
        companyRolesTestUtils.assignCompanyRole(CompanyRole.CompanyOwner, UUID.fromString(companyId), userId)
    }

    private fun removeCompanyOwnerRole(
        companyId: String,
        userId: UUID,
    ) = GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
        companyRolesTestUtils.removeCompanyRole(CompanyRole.CompanyOwner, UUID.fromString(companyId), userId)
    }

    private fun makeCompanyMember(companyId: String) =
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            companyRightsTestUtils.assignCompanyRight(
                CompanyRightAssignmentString.CompanyRight.Member,
                UUID.fromString(companyId),
            )
        }

    private fun removeCompanyMemberRights(companyId: String) =
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            companyRightsTestUtils.removeCompanyRight(
                CompanyRightAssignmentString.CompanyRight.Member,
                UUID.fromString(companyId),
            )
        }

    private val billableCompanyIdReaderA = uploadCompanyAsUploader()
    private val billableCompanyIdReaderB = uploadCompanyAsUploader()
    private val billableCompanyIdReaderC = uploadCompanyAsUploader()
    private val billableCompanyIdUploader = uploadCompanyAsUploader()
    private val billableCompanyIdWithdraw = uploadCompanyAsUploader()

    private val dataReaderUserId = TechnicalUser.Reader.technicalUserId
    private val dataUploaderUserId = TechnicalUser.Uploader.technicalUserId

    private val initialCredit = BigDecimal("10.0")

    private fun createDummyRequestForCompany(
        companyId: String,
        comment: String = "dummy request",
    ): String =
        apiAccessor.dataSourcingRequestControllerApi
            .createRequest(SingleRequest(companyId, "sfdr", "2023", comment))
            .requestId

    private fun postTransaction(
        companyId: String,
        value: BigDecimal = initialCredit,
        reason: String = "Initial credit top-up",
    ) = GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
        apiAccessor.accountingServiceCreditsControllerApi.postTransaction(
            companyId = companyId,
            transactionPost = TransactionPost(valueOfChange = value, reasonForChange = reason),
        )
    }

    private fun patchRequestStateToProcessing(requestId: String) = patchRequestState(requestId, RequestState.Processing)

    private fun patchRequestStateToWithdrawn(requestId: String) = patchRequestState(requestId, RequestState.Withdrawn)

    private fun patchRequestState(
        requestId: String,
        state: RequestState,
    ) = GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(
            dataRequestId = requestId,
            requestState = state,
        )
    }

    private fun getBalance(companyId: String): BigDecimal = apiAccessor.accountingServiceCreditsControllerApi.getBalance(companyId)

    @Test
    fun `post a transaction then check the balance`() {
        assignCompanyOwnerRole(billableCompanyIdReaderA, UUID.fromString(dataReaderUserId))
        makeCompanyMember(billableCompanyIdReaderA)
        try {
            postTransaction(billableCompanyIdReaderA)
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            val balance = getBalance(billableCompanyIdReaderA)
            assertEquals(initialCredit, balance)
        } finally {
            removeCompanyOwnerRole(billableCompanyIdReaderA, UUID.fromString(dataReaderUserId))
            removeCompanyMemberRights(billableCompanyIdReaderA)
        }
    }

    @Test
    fun `post a transaction then add a request and set it to processing and check the balance`() {
        assignCompanyOwnerRole(billableCompanyIdReaderB, UUID.fromString(dataReaderUserId))
        makeCompanyMember(billableCompanyIdReaderB)
        try {
            postTransaction(billableCompanyIdReaderB)

            val requestedCompanyId = uploadCompanyAsUploader()
            val requestId = createDummyRequestForCompany(requestedCompanyId)
            patchRequestStateToProcessing(requestId)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            await().atMost(Duration.ofSeconds(10)).untilAsserted {
                assertEquals(initialCredit - BigDecimal(1.0), getBalance(billableCompanyIdReaderB))
            }
        } finally {
            removeCompanyOwnerRole(billableCompanyIdReaderB, UUID.fromString(dataReaderUserId))
            removeCompanyMemberRights(billableCompanyIdReaderB)
        }
    }

    @Test
    fun `post a transaction then add two requests from different users for the same requestedCompanyId and check the balance`() {
        assignCompanyOwnerRole(billableCompanyIdReaderC, UUID.fromString(dataReaderUserId))
        makeCompanyMember(billableCompanyIdReaderC)
        assignCompanyOwnerRole(billableCompanyIdUploader, UUID.fromString(dataUploaderUserId))
        makeCompanyMember(billableCompanyIdUploader)
        try {
            postTransaction(billableCompanyIdReaderC)

            val requestedCompanyId = uploadCompanyAsUploader()

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            val requestIdReader = createDummyRequestForCompany(requestedCompanyId)

            patchRequestStateToProcessing(requestIdReader)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
            val requestIdUploader = createDummyRequestForCompany(requestedCompanyId)

            patchRequestStateToProcessing(requestIdUploader)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            await().atMost(Duration.ofSeconds(10)).untilAsserted {
                assertEquals(initialCredit - BigDecimal(0.5), getBalance(billableCompanyIdReaderC))
            }
        } finally {
            removeCompanyOwnerRole(billableCompanyIdReaderC, UUID.fromString(dataReaderUserId))
            removeCompanyMemberRights(billableCompanyIdReaderC)
            removeCompanyOwnerRole(billableCompanyIdUploader, UUID.fromString(dataUploaderUserId))
            removeCompanyMemberRights(billableCompanyIdUploader)
        }
    }

    @Test
    fun `post a transaction then add a request set it to processing then withdraw and check the balance is restored`() {
        assignCompanyOwnerRole(billableCompanyIdWithdraw, UUID.fromString(dataReaderUserId))
        makeCompanyMember(billableCompanyIdWithdraw)
        try {
            postTransaction(billableCompanyIdWithdraw)

            val requestedCompanyId = uploadCompanyAsUploader()
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            val requestId = createDummyRequestForCompany(requestedCompanyId)
            patchRequestStateToProcessing(requestId)

            await().atMost(Duration.ofSeconds(10)).untilAsserted {
                assertEquals(initialCredit - BigDecimal("1.0"), getBalance(billableCompanyIdWithdraw))
            }

            patchRequestStateToWithdrawn(requestId)

            await().atMost(Duration.ofSeconds(10)).untilAsserted {
                assertEquals(initialCredit, getBalance(billableCompanyIdWithdraw))
            }
        } finally {
            removeCompanyOwnerRole(billableCompanyIdWithdraw, UUID.fromString(dataReaderUserId))
            removeCompanyMemberRights(billableCompanyIdWithdraw)
        }
    }
}
