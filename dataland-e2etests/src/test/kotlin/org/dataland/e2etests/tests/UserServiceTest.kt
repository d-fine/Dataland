package org.dataland.e2etests.tests

import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.api.ApiAwait
import org.dataland.e2etests.utils.api.UserService
import org.dataland.userService.openApiClient.model.PortfolioUpload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {
    private val apiAccessor = ApiAccessor()
    private val companyId: String = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId

    @Test
    fun `test that creating and retrieving a portfolio for an existing company works as expected`() {
        val portfolio =
            PortfolioUpload(
                portfolioName = "Test Portfolio ${UUID.randomUUID()}",
                companyIds = setOf(companyId),
                frameworks =
                    setOf(
                        PortfolioUpload.Frameworks.sfdr,
                        PortfolioUpload.Frameworks.additionalMinusCompanyMinusInformation,
                        PortfolioUpload.Frameworks.nuclearMinusAndMinusGas,
                    ),
            )

        GlobalAuth.withTechnicalUser(TechnicalUser.Reader) {
            ApiAwait.waitForSuccess { UserService.portfolioControllerApi.createPortfolio(portfolio) }
            val portfolioResponse = assertDoesNotThrow { UserService.portfolioControllerApi.getAllPortfoliosForCurrentUser() }
            assertEquals(1, portfolioResponse.size)
        }
    }
}
