package org.dataland.datalanduserservice

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalanduserservice.api.PortfolioApi
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional

/**
 * In this test class, the entire portfolio workflow is tested
 */
@SpringBootTest(
    classes = [DatalandUserService::class], properties = ["spring.profiles.active=nodb"],
)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class DatalandUserServiceSpringbootTest
    @Autowired
    constructor(
        private val portfolioApi: PortfolioApi,
    ) {
        @MockitoBean
        private val mockKeycloakUserService = mock<KeycloakUserService>()

        @MockitoBean
        private val mockCompanyDataController = mock<CompanyDataControllerApi>()

        private val mockSecurityContext = mock<SecurityContext>()
        private lateinit var mockAuthentication: DatalandAuthentication

        private val username = "data_reader"
        private val userId = "user-id"
        private val validCompanyId1 = "valid-company-id-1"
        private val validCompanyId2 = "valid-company-id-2"
        private val invalidCompanyId = "invalid-company-id"

        private val dummyPortfolioUpload1 =
            PortfolioUpload(
                portfolioName = "Test Portfolio",
                companyIds = setOf(validCompanyId1, validCompanyId2),
                frameworks = setOf(DataTypeEnum.eutaxonomyMinusFinancials, DataTypeEnum.lksg),
            )

        private val dummyPortfolioUpload2 =
            PortfolioUpload(
                portfolioName = "Second Test Portfolio",
                companyIds = setOf(validCompanyId1),
                frameworks = setOf(DataTypeEnum.eutaxonomyMinusNonMinusFinancials, DataTypeEnum.sfdr),
            )

        @BeforeEach
        fun setup() {
            reset(mockCompanyDataController, mockSecurityContext, mockKeycloakUserService)
            this.resetSecurityContext()

            doReturn(listOf("ROLE_USER")).whenever(mockKeycloakUserService).getUserRoleNames(userId)
            doNothing().whenever(mockCompanyDataController).isCompanyIdValid(validCompanyId1)
            doNothing().whenever(mockCompanyDataController).isCompanyIdValid(validCompanyId2)
            doThrow(ClientException(statusCode = HttpStatus.NOT_FOUND.value()))
                .whenever(mockCompanyDataController)
                .isCompanyIdValid(invalidCompanyId)
        }

        /**
         * Setting the security context to use dataland dummy user with role ROLE_USER
         */
        private fun resetSecurityContext() {
            mockAuthentication =
                AuthenticationMock.mockJwtAuthentication(username, userId, roles = setOf(DatalandRealmRole.ROLE_USER))
            doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
            SecurityContextHolder.setContext(mockSecurityContext)
        }

        @Nested
        @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
        inner class HappyPathTests {
            @Test
            fun `test that posting and retrieving valid portfolios with valid companyIds and valid dataTypes works`() {
                assertDoesNotThrow {
                    portfolioApi.createPortfolio(dummyPortfolioUpload1)
                    portfolioApi.createPortfolio(dummyPortfolioUpload2)
                }

                val portfolios = assertDoesNotThrow { portfolioApi.getAllPortfoliosForCurrentUser() }
                assertEquals(2, portfolios.body?.size)
            }

            @Test
            fun `test that replacing an existing portfolio works as expected`() {
                val originalPortfolioResponse =
                    assertDoesNotThrow { portfolioApi.createPortfolio(dummyPortfolioUpload1) }.body!!
                assertDoesNotThrow {
                    portfolioApi.replacePortfolio(
                        originalPortfolioResponse.portfolioId,
                        dummyPortfolioUpload2,
                    )
                }

                val portfolioResponse =
                    assertDoesNotThrow { portfolioApi.getPortfolio(originalPortfolioResponse.portfolioId) }.body!!

                assertEquals(originalPortfolioResponse.portfolioId, portfolioResponse.portfolioId)
                assertEquals(dummyPortfolioUpload2.portfolioName, portfolioResponse.portfolioName)
                assertEquals(originalPortfolioResponse.creationTimestamp, portfolioResponse.creationTimestamp)
                assertTrue(originalPortfolioResponse.lastUpdateTimestamp < portfolioResponse.lastUpdateTimestamp)
                assertEquals(dummyPortfolioUpload2.companyIds, portfolioResponse.companyIds)
                assertEquals(dummyPortfolioUpload2.frameworks, portfolioResponse.frameworks)
            }

            @Test
            fun `test that deleting a portfolio works`() {
                val portfolioResponse =
                    assertDoesNotThrow { portfolioApi.createPortfolio(dummyPortfolioUpload1) }.body!!
                assertDoesNotThrow { portfolioApi.deletePortfolio(portfolioResponse.portfolioId) }
                assertEquals(0, portfolioApi.getAllPortfoliosForCurrentUser().body?.size)
            }
        }

        @Nested
        @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
        inner class ExceptionHandlingTests {
            @Test
            fun `test that posting portfolio with an existing name throws ConflictException`() {
                val portfolioWithExistingName =
                    dummyPortfolioUpload2.copy(portfolioName = dummyPortfolioUpload1.portfolioName)
                assertDoesNotThrow { portfolioApi.createPortfolio(dummyPortfolioUpload1) }
                assertThrows<ConflictApiException> { portfolioApi.createPortfolio(portfolioWithExistingName) }
            }

            @Test
            fun `test that posting portfolio with invalid companyId throws ClientException`() {
                val portfolio = dummyPortfolioUpload1.copy(companyIds = setOf(validCompanyId1, invalidCompanyId))

                assertThrows<ResourceNotFoundApiException> { portfolioApi.createPortfolio(portfolio) }
            }
        }
    }
