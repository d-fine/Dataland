package org.dataland.e2etests.tests

import org.dataland.dataSourcingService.openApiClient.model.RequestSearchFilterString
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.e2etests.PREMIUM_USER_ID
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.api.UserService
import org.dataland.e2etests.utils.testDataProviders.GeneralTestDataProvider
import org.dataland.userService.openApiClient.infrastructure.ClientException
import org.dataland.userService.openApiClient.model.PortfolioUpload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PortfolioControllerTest {
    private val apiAccessor = ApiAccessor()

    @ParameterizedTest
    @EnumSource(TechnicalUser::class)
    fun `check that only admins can use the endpoint for getting all portfolios of a user by userId`(technicalUser: TechnicalUser) {
        GlobalAuth.withTechnicalUser(technicalUser) {
            if (technicalUser == TechnicalUser.Admin) {
                assertDoesNotThrow { UserService.portfolioControllerApi.getPortfoliosForUser(PREMIUM_USER_ID) }
            } else {
                assertThrows<ClientException> {
                    UserService.portfolioControllerApi.getPortfoliosForUser(PREMIUM_USER_ID)
                }
            }
        }
    }

    @ParameterizedTest
    @EnumSource(TechnicalUser::class)
    fun `check that only admins can use the endpoint for getting all portfolios`(technicalUser: TechnicalUser) {
        GlobalAuth.withTechnicalUser(technicalUser) {
            if (technicalUser == TechnicalUser.Admin) {
                assertDoesNotThrow { UserService.portfolioControllerApi.getAllPortfolios() }
            } else {
                assertThrows<ClientException> {
                    UserService.portfolioControllerApi.getAllPortfolios()
                }
            }
        }
    }

    @Test
    fun `check that eu-taxonomy portfolios create the right requests depending on the company sector`() {
        val generalTestDataProvider = GeneralTestDataProvider()
        val financialCompanyInformation =
            generalTestDataProvider.generateCompanyInformation("FinancialCompany", "Financials")
        val nonFinancialCompanyInformation =
            generalTestDataProvider.generateCompanyInformation("NonFinancialCompany", "Industrials")
        val noSectorCompanyInformation =
            generalTestDataProvider.generateCompanyInformation("FinancialCompany", null)

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            val financialCompanyId = apiAccessor.companyDataControllerApi.postCompany(financialCompanyInformation).companyId
            val nonFinancialCompanyId = apiAccessor.companyDataControllerApi.postCompany(nonFinancialCompanyInformation).companyId
            val noSectorCompanyId = apiAccessor.companyDataControllerApi.postCompany(noSectorCompanyInformation).companyId

            val testPortfolio =
                PortfolioUpload(
                    "Test Portfolio" + Random.nextInt(0, 100),
                    setOf(financialCompanyId, nonFinancialCompanyId, noSectorCompanyId),
                    true,
                    setOf("eutaxonomy"),
                )
            UserService.portfolioControllerApi.createPortfolio(testPortfolio)
            Thread.sleep(3000)

            val financialRequests =
                apiAccessor.dataSourcingRequestControllerApi.postRequestSearch(
                    RequestSearchFilterString(
                        companyId = financialCompanyId,
                        requestStates =
                            listOf(
                                RequestState.Open,
                            ),
                    ),
                )

            val nonFinancialRequests =
                apiAccessor.dataSourcingRequestControllerApi.postRequestSearch(
                    RequestSearchFilterString(
                        companyId = nonFinancialCompanyId,
                        requestStates =
                            listOf(
                                RequestState.Open,
                            ),
                    ),
                )

            val noSectorRequests =
                apiAccessor.dataSourcingRequestControllerApi.postRequestSearch(
                    RequestSearchFilterString(
                        companyId = noSectorCompanyId,
                        requestStates =
                            listOf(
                                RequestState.Open,
                            ),
                    ),
                )

            assertEquals(2, financialRequests.size)
            assertEquals(2, nonFinancialRequests.size)
            assertEquals(3, noSectorRequests.size)
        }
    }
}
