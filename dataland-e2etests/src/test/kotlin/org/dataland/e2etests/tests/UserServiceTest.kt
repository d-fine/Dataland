package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
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

    private fun createDummyPortfolioUpload(companyIds: Set<String>): PortfolioUpload =
        PortfolioUpload(
            portfolioName = "Test Portfolio ${UUID.randomUUID()}",
            companyIds = companyIds,
            frameworks =
                setOf(
                    PortfolioUpload.Frameworks.sfdr,
                    PortfolioUpload.Frameworks.additionalMinusCompanyMinusInformation,
                    PortfolioUpload.Frameworks.nuclearMinusAndMinusGas,
                ),
        )

    @Test
    fun `test that creating and retrieving a portfolio for an existing company works as expected`() {
        val portfolio = createDummyPortfolioUpload(setOf(companyId))

        GlobalAuth.withTechnicalUser(TechnicalUser.Reader) {
            ApiAwait.waitForSuccess { UserService.portfolioControllerApi.createPortfolio(portfolio) }
            val portfolioResponse = assertDoesNotThrow { UserService.portfolioControllerApi.getAllPortfoliosForCurrentUser() }
            assertEquals(1, portfolioResponse.size)
        }
    }

    private fun uploadDummyCompaniesAndDatasets(): List<StoredCompany> {
        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            val storedCompanyInformation =
                (1..3).map { apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany }

            apiAccessor.uploadDummyFrameworkDataset(storedCompanyInformation[0].companyId, DataTypeEnum.sfdr, "2023")
            apiAccessor.uploadDummyFrameworkDataset(storedCompanyInformation[1].companyId, DataTypeEnum.sfdr, "2023")
            apiAccessor.uploadDummyFrameworkDataset(storedCompanyInformation[1].companyId, DataTypeEnum.eutaxonomyMinusFinancials, "2023")
            apiAccessor.uploadDummyFrameworkDataset(storedCompanyInformation[0].companyId, DataTypeEnum.sfdr, "2024")

            return storedCompanyInformation
        }
    }

    @Test
    fun `test that enriched portfolios are build as expected`() {
        val storedCompanies = uploadDummyCompaniesAndDatasets()
        val portfolioUpload = createDummyPortfolioUpload(storedCompanies.map { it.companyId }.toSet())

        GlobalAuth.withTechnicalUser(TechnicalUser.Reader) {
            // ApiAwait.waitForSuccess { UserService.portfolioControllerApi.createPortfolio(portfolioUpload) }
            UserService.portfolioControllerApi.createPortfolio(portfolioUpload)
            val portfolioResponse = assertDoesNotThrow { UserService.portfolioControllerApi.getAllPortfoliosForCurrentUser() }
            val enrichedPortfolio = UserService.portfolioControllerApi.getEnrichedPortfolio(portfolioResponse.first().portfolioId)

            assertEquals(enrichedPortfolio.portfolioName, portfolioUpload.portfolioName)
            assertEquals(6, enrichedPortfolio.propertyEntries.size)
        }
    }
}
