package org.dataland.e2etests.tests

import org.dataland.dataSourcingService.openApiClient.model.ExtendedStoredRequest
import org.dataland.dataSourcingService.openApiClient.model.RequestSearchFilterString
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.e2etests.PREMIUM_USER_ID
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.api.UserService
import org.dataland.e2etests.utils.testDataProviders.GeneralTestDataProvider
import org.dataland.e2etests.utils.testDataProviders.awaitUntilAsserted
import org.dataland.userService.openApiClient.infrastructure.ClientException
import org.dataland.userService.openApiClient.model.PortfolioUpload
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.util.UUID

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
    fun `check that eutaxonomy portfolios create the right requests depending on the company sector`() {
        val generalTestDataProvider = GeneralTestDataProvider()
        val financialCompanyInformation =
            generalTestDataProvider.generateCompanyInformation("FinancialCompany", "Financials")
        val nonFinancialCompanyInformation =
            generalTestDataProvider.generateCompanyInformation("NonFinancialCompany", "Industrials")
        val noSectorCompanyInformation =
            generalTestDataProvider.generateCompanyInformation("NoSectorCompany", null)

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            val financialCompanyId = apiAccessor.companyDataControllerApi.postCompany(financialCompanyInformation).companyId
            val nonFinancialCompanyId = apiAccessor.companyDataControllerApi.postCompany(nonFinancialCompanyInformation).companyId
            val noSectorCompanyId = apiAccessor.companyDataControllerApi.postCompany(noSectorCompanyInformation).companyId

            val testPortfolio =
                PortfolioUpload(
                    "Test Portfolio " + UUID.randomUUID().toString(),
                    setOf(financialCompanyId, nonFinancialCompanyId, noSectorCompanyId),
                    true,
                    setOf("eutaxonomy"),
                )
            UserService.portfolioControllerApi.createPortfolio(testPortfolio)
            lateinit var financialRequests: List<ExtendedStoredRequest>
            lateinit var nonFinancialRequests: List<ExtendedStoredRequest>
            lateinit var noSectorRequests: List<ExtendedStoredRequest>
            awaitUntilAsserted {
                financialRequests = getOpenRequests(financialCompanyId)
                nonFinancialRequests = getOpenRequests(nonFinancialCompanyId)
                noSectorRequests = getOpenRequests(noSectorCompanyId)

                assertEquals(2, financialRequests.size)
                assertEquals(2, nonFinancialRequests.size)
                assertEquals(3, noSectorRequests.size)
            }
            assertEquals(
                setOf(
                    DataTypeEnum.eutaxonomyMinusFinancials.toString(),
                    DataTypeEnum.nuclearMinusAndMinusGas.toString(),
                ),
                financialRequests.map { it.dataType }.toSet(),
            )
            assertEquals(
                setOf(
                    DataTypeEnum.eutaxonomyMinusNonMinusFinancials.toString(),
                    DataTypeEnum.nuclearMinusAndMinusGas.toString(),
                ),
                nonFinancialRequests.map { it.dataType }.toSet(),
            )
            assertEquals(
                setOf(
                    DataTypeEnum.eutaxonomyMinusFinancials.toString(),
                    DataTypeEnum.eutaxonomyMinusNonMinusFinancials.toString(),
                    DataTypeEnum.nuclearMinusAndMinusGas.toString(),
                ),
                noSectorRequests.map { it.dataType }.toSet(),
            )
        }
    }

    private fun getOpenRequests(companyId: String) =
        apiAccessor.dataSourcingRequestControllerApi.postRequestSearch(
            RequestSearchFilterString(
                companyId = companyId,
                requestStates = listOf(RequestState.Open),
            ),
        )
}
