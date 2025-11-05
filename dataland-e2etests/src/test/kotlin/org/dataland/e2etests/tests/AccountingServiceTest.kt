package org.dataland.e2etests.tests

import org.dataland.accountingService.openApiClient.model.TransactionPost
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.dataSourcingService.openApiClient.model.SingleRequest
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.GlobalAuth.jwtHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.CompanyRolesTestUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.lang.Thread.sleep
import java.math.BigDecimal
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountingServiceTest {
    private val apiAccessor = ApiAccessor()
    private val companyRolesTestUtils = CompanyRolesTestUtils()
    private val initialCredit = BigDecimal("10.0")

    private fun createDummyRequestForCompany(
        companyId: String,
        comment: String = "dummy request",
    ): String =
        apiAccessor.dataSourcingRequestControllerApi
            .createRequest(SingleRequest(companyId, "sfdr", "2023", comment))
            .requestId

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

    private fun patchRequestStateToProcessing(
        requestId: String,
        state: RequestState = RequestState.Processing,
    ) = GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
        apiAccessor.dataSourcingRequestControllerApi.patchRequestState(
            dataRequestId = requestId,
            requestState = state,
        )
    }

    private fun getBalance(companyId: String): BigDecimal = apiAccessor.accountingServiceCreditsControllerApi.getBalance(companyId)

    @Test
    fun `post a transaction, then check the balance`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val dataReaderUserId = UUID.fromString(TechnicalUser.Reader.technicalUserId)
        val companyId = uploadCompanyAsUploader()
        assignCompanyOwnerRole(companyId, dataReaderUserId)
        postTransaction(companyId)
        val balance = getBalance(companyId)
        assertEquals(initialCredit, balance)
    }

    @Test
    fun `post a transaction, then add a request, set it to processing and check the balance`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        val billableCompanyId = uploadCompanyAsUploader()
        val dataReaderUserId = UUID.fromString(TechnicalUser.Reader.technicalUserId)
        assignCompanyOwnerRole(billableCompanyId, dataReaderUserId)
        postTransaction(billableCompanyId)

        val requestedCompanyId = uploadCompanyAsUploader()
        val requestId = createDummyRequestForCompany(requestedCompanyId)
        patchRequestStateToProcessing(requestId)

        sleep(2000)
        val balance = getBalance(billableCompanyId)
        assertEquals(initialCredit - BigDecimal(1.0), balance)
    }

    @Test
    fun `post a transaction, then add two requests from different users for the same requestedCompanyId and check the balance`() {
        // First user and request
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val billableCompanyIdA = uploadCompanyAsUploader()
        val dataReaderUserId = UUID.fromString(TechnicalUser.Reader.technicalUserId)
        assignCompanyOwnerRole(billableCompanyIdA, dataReaderUserId)
        postTransaction(billableCompanyIdA)

        // The requested company (same for both requests)
        val requestedCompanyId = uploadCompanyAsUploader()

        // Request from Reader
        val requestIdA = createDummyRequestForCompany(requestedCompanyId)
        patchRequestStateToProcessing(requestIdA)

        // Second user and request
        val dataUploaderUserId = UUID.fromString(TechnicalUser.Uploader.technicalUserId)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val billableCompanyIdB = uploadCompanyAsUploader()
        assignCompanyOwnerRole(billableCompanyIdB, dataUploaderUserId)
        val requestIdB = createDummyRequestForCompany(requestedCompanyId)
        patchRequestStateToProcessing(requestIdB)

        sleep(2000)
        // Request balance as first user
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val balance = getBalance(billableCompanyIdA)
        assertEquals(initialCredit - BigDecimal(0.5), balance)
    }
}
