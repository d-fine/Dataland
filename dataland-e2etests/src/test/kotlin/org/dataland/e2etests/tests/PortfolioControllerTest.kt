package org.dataland.e2etests.tests

import org.dataland.e2etests.PREMIUM_USER_ID
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.api.UserService
import org.dataland.userService.openApiClient.infrastructure.ClientException
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PortfolioControllerTest {
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
}
