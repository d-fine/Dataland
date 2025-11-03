package org.dataland.e2etests.tests

import org.dataland.accountingService.openApiClient.model.TransactionPost
import org.dataland.dataSourcingService.openApiClient.model.SingleRequest
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountingServiceTest {
    private val apiAccessor = ApiAccessor()
    private lateinit var dummyRequest: SingleRequest

    @BeforeEach
    fun setup() {
        val companyId =
            uploadCompanyAsUploader()
        dummyRequest = SingleRequest(companyId, "sfdr", "2023", "dummy request")
    }

    private fun uploadCompanyAsUploader(): String =
        GlobalAuth.withTechnicalUser(TechnicalUser.Uploader) {
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        }

    @Test
    fun `post a transaction, then check the balance`() {
        val companyId =
            uploadCompanyAsUploader()
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.accountingServiceCreditsControllerApi.postTransaction(
                companyId = companyId,
                transactionPost =
                    TransactionPost(
                        valueOfChange = BigDecimal(10.0),
                        reasonForChange = "Initial credit top-up",
                    ),
            )
            val balance =
                apiAccessor.accountingServiceCreditsControllerApi.getBalance(companyId)
            assert(balance == BigDecimal(10.0))
        }
    }
}
