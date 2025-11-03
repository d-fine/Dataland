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
    private val dataReaderUserId = UUID.fromString(TechnicalUser.Reader.technicalUserId)

    private fun createDummyRequest(companyId: String): String {
        val singleRequest = SingleRequest(companyId, "sfdr", "2023", "dummy request")
        return apiAccessor.dataSourcingRequestControllerApi
            .createRequest(
                singleRequest,
            ).requestId
    }

    private fun uploadCompanyAsUploader(): String =
        GlobalAuth.withTechnicalUser(TechnicalUser.Uploader) {
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        }

    @Test
    fun `post a transaction, then check the balance`() {
        val companyId =
            uploadCompanyAsUploader()
        val initialCredit = BigDecimal(10.0)
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.accountingServiceCreditsControllerApi.postTransaction(
                companyId = companyId,
                transactionPost =
                    TransactionPost(
                        valueOfChange = initialCredit,
                        reasonForChange = "Initial credit top-up",
                    ),
            )
            val balance =
                apiAccessor.accountingServiceCreditsControllerApi.getBalance(companyId)
            assertEquals(initialCredit, balance)
        }
    }

    @Test
    fun `post a transaction, then add a request, set it to processing and check the balance`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val billableCompanyId =
            uploadCompanyAsUploader()
        val initialCredit = BigDecimal(10.0)
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            companyRolesTestUtils.assignCompanyRole(CompanyRole.CompanyOwner, UUID.fromString(billableCompanyId), dataReaderUserId)
            apiAccessor.accountingServiceCreditsControllerApi.postTransaction(
                companyId = billableCompanyId,
                transactionPost =
                    TransactionPost(
                        valueOfChange = initialCredit,
                        reasonForChange = "Initial credit top-up",
                    ),
            )
        }

        val requestedCompanyId =
            uploadCompanyAsUploader()
        val requestId = createDummyRequest(requestedCompanyId)
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.dataSourcingRequestControllerApi.patchRequestState(
                dataRequestId = requestId,
                requestState = RequestState.Processing,
            )
        }
        sleep(2000)
        val balance =
            apiAccessor.accountingServiceCreditsControllerApi.getBalance(billableCompanyId)
        assertEquals(initialCredit - BigDecimal(1.0), balance)
    }
}
