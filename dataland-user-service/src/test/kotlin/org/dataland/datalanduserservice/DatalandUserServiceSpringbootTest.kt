package org.dataland.datalanduserservice

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalanduserservice.api.PortfolioApi
import org.dataland.datalanduserservice.model.PortfolioPayload
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

        private val dummyPortfolioPayload1 =
            PortfolioPayload(
                portfolioName = "Test Portfolio",
                companyIds = setOf(validCompanyId1, validCompanyId2),
                dataTypes = setOf(DataTypeEnum.eutaxonomyMinusFinancials, DataTypeEnum.lksg),
            )

        private val dummyPortfolioPayload2 =
            PortfolioPayload(
                portfolioName = "Second Test Portfolio",
                companyIds = setOf(validCompanyId1),
                dataTypes = setOf(DataTypeEnum.eutaxonomyMinusNonMinusFinancials, DataTypeEnum.sfdr),
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
        inner class TestsForUserRole {
            @Test
            fun `test that posting and retrieving valid portfolios with valid companyIds and valid dataTypes works`() {
                assertDoesNotThrow {
                    portfolioApi.createPortfolio(dummyPortfolioPayload1)
                    portfolioApi.createPortfolio(dummyPortfolioPayload2)
                }

                val portfolios = assertDoesNotThrow { portfolioApi.getAllPortfoliosForCurrentUser() }
                assertEquals(2, portfolios.body?.size)
            }

            @Test
            fun `test that posting and retrieving portfolio by portfolioId works as expected`() {
                assertDoesNotThrow {
                    val portfolioId = portfolioApi.createPortfolio(dummyPortfolioPayload1).body!!.portfolioId
                    portfolioApi.getPortfolio(portfolioId)
                }
            }

            @Test
            fun `test that posting portfolio with invalid companyId throws ClientException`() {
                val portfolio = dummyPortfolioPayload1.copy(companyIds = setOf(validCompanyId1, invalidCompanyId))

                assertThrows<ResourceNotFoundApiException> { portfolioApi.createPortfolio(portfolio) }
            }

            @Test
            fun `test that posting portfolio with an existing name throws ConflictException`() {
                val portfolioWithExistingName =
                    dummyPortfolioPayload2.copy(portfolioName = dummyPortfolioPayload1.portfolioName)
                assertDoesNotThrow { portfolioApi.createPortfolio(dummyPortfolioPayload1) }
                assertThrows<ConflictApiException> { portfolioApi.createPortfolio(portfolioWithExistingName) }
            }

            @Test
            fun `test that patching an existing portfolio with a valid companyId works as expected`() {
                assertDoesNotThrow {
                    val portfolioId = portfolioApi.createPortfolio(dummyPortfolioPayload2).body!!.portfolioId
                    portfolioApi.patchPortfolio(portfolioId, validCompanyId2)
                }
            }

            @Test
            fun `test that patching an existing portfolio with an invalid companyId throws ResourceNotFoundException`() {
                assertThrows<ResourceNotFoundApiException> {
                    val portfolioId = portfolioApi.createPortfolio(dummyPortfolioPayload2).body!!.portfolioId
                    portfolioApi.patchPortfolio(portfolioId, invalidCompanyId)
                }
            }

            @Test
            fun `test that replacing an existing portfolio works as expected`() {
                val originalPortfolioResponse =
                    assertDoesNotThrow { portfolioApi.createPortfolio(dummyPortfolioPayload1) }.body!!
                assertDoesNotThrow {
                    portfolioApi.replacePortfolio(
                        originalPortfolioResponse.portfolioId,
                        dummyPortfolioPayload2,
                    )
                }

                val portfolioResponse =
                    assertDoesNotThrow { portfolioApi.getPortfolio(originalPortfolioResponse.portfolioId) }.body!!

                assertEquals(originalPortfolioResponse.portfolioId, portfolioResponse.portfolioId)
                assertEquals(dummyPortfolioPayload2.portfolioName, portfolioResponse.portfolioName)
                assertEquals(originalPortfolioResponse.creationTimestamp, portfolioResponse.creationTimestamp)
                assertTrue(originalPortfolioResponse.lastUpdateTimestamp < portfolioResponse.lastUpdateTimestamp)
                assertEquals(dummyPortfolioPayload2.companyIds, portfolioResponse.companyIds)
                assertEquals(dummyPortfolioPayload2.dataTypes, portfolioResponse.dataTypes)
            }

            @Test
            fun `test that deleting a portfolio works`() {
                val portfolioResponse = assertDoesNotThrow { portfolioApi.createPortfolio(dummyPortfolioPayload1) }.body!!
                assertDoesNotThrow { portfolioApi.deletePortfolio(portfolioResponse.portfolioId) }
                assertEquals(0, portfolioApi.getAllPortfoliosForCurrentUser().body?.size)
            }

            @Test
            fun `test that removing a company from an existing portfolio works as expected`() {
                val portfolioResponse = assertDoesNotThrow { portfolioApi.createPortfolio(dummyPortfolioPayload1) }.body!!
                assertDoesNotThrow {
                    portfolioApi.removeCompanyFromPortfolio(
                        portfolioResponse.portfolioId,
                        validCompanyId1,
                    )
                }
                assertEquals(
                    1,
                    portfolioApi
                        .getPortfolio(portfolioResponse.portfolioId)
                        .body!!
                        .companyIds.size,
                )
            }

            @Test
            fun `test that removing the last company from an existing portfolio is prohibited`() {
                val portfolioResponse = assertDoesNotThrow { portfolioApi.createPortfolio(dummyPortfolioPayload2) }.body!!
                assertThrows<InvalidInputApiException> {
                    portfolioApi.removeCompanyFromPortfolio(portfolioResponse.portfolioId, validCompanyId1)
                }
            }
        }
    }
