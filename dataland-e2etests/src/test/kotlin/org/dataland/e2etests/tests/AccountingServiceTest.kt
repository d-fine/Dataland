package org.dataland.e2etests.tests

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
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.lang.Thread.sleep
import java.math.BigDecimal
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountingServiceTest {
    companion object {
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

        private val billableCompanyIdA = uploadCompanyAsUploader()
        private val billableCompanyIdB = uploadCompanyAsUploader()

        private val dataReaderUserId = TechnicalUser.Reader.technicalUserId
        private val dataUploaderUserId = TechnicalUser.Uploader.technicalUserId

        @BeforeAll
        @JvmStatic
        fun setup() {
            assignCompanyOwnerRole(billableCompanyIdA, UUID.fromString(dataReaderUserId))
            assignCompanyOwnerRole(billableCompanyIdB, UUID.fromString(dataUploaderUserId))
            makeCompanyMember(billableCompanyIdA)
            makeCompanyMember(billableCompanyIdB)
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            removeCompanyOwnerRole(billableCompanyIdA, UUID.fromString(dataReaderUserId))
            removeCompanyOwnerRole(billableCompanyIdB, UUID.fromString(dataUploaderUserId))
            removeCompanyMemberRights(billableCompanyIdA)
            removeCompanyMemberRights(billableCompanyIdB)
        }
    }

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
    fun `post a transaction then check the balance`() {
        postTransaction(billableCompanyIdA)
        val balance = getBalance(billableCompanyIdA)
        assertEquals(initialCredit, balance)
    }

    @Test
    fun `post a transaction then add a request and set it to processing and check the balance`() {
        postTransaction(billableCompanyIdA)

        val requestedCompanyId = uploadCompanyAsUploader()
        val requestId = createDummyRequestForCompany(requestedCompanyId)
        patchRequestStateToProcessing(requestId)

        sleep(2000)
        assertEquals(initialCredit - BigDecimal(1.0), getBalance(billableCompanyIdA))
    }

    @Test
    fun `post a transaction then add two requests from different users for the same requestedCompanyId and check the balance`() {
        postTransaction(billableCompanyIdA)

        val requestedCompanyId = uploadCompanyAsUploader()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val requestIdA = createDummyRequestForCompany(requestedCompanyId)

        patchRequestStateToProcessing(requestIdA)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val requestIdB = createDummyRequestForCompany(requestedCompanyId)

        patchRequestStateToProcessing(requestIdB)

        sleep(2000)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertEquals(initialCredit - BigDecimal(0.5), getBalance(billableCompanyIdA))
    }
}
