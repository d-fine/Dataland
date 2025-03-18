package org.dataland.datalanduserservice.controller

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.dataland.datalanduserservice.service.PortfolioService
import org.dataland.datalanduserservice.utils.Validator
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PortfolioControllerTest {
    private val mockPortfolioService = mock<PortfolioService>()
    private val mockValidator = mock<Validator>()
    private val mockSecurityContext = mock<SecurityContext>()

    private lateinit var mockAuthentication: DatalandAuthentication
    private lateinit var portfolioController: PortfolioController

    private val username = "data_reader"
    private val userId = "user-id"
    private val dummyPortfolioId = UUID.randomUUID()
    private val dummyPortfolioName = "Test Portfolio"
    private val validCompanyId = "valid-company-id"

    private val validPortfolioUpload =
        PortfolioUpload(dummyPortfolioName, setOf(validCompanyId), setOf(DataTypeEnum.lksg))

    @BeforeEach
    fun setup() {
        this.resetSecurityContext()
        doNothing().whenever(mockValidator).validatePortfolioCreation(eq(validPortfolioUpload), any())
        portfolioController = PortfolioController(mockPortfolioService, mockValidator)
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

    @Test
    fun `test that creating a valid portfolio returns 201 response`() {
        doReturn(false).whenever(mockPortfolioService).existsPortfolioWithNameForUser(eq(dummyPortfolioName), any())
        val response = assertDoesNotThrow { portfolioController.createPortfolio(validPortfolioUpload) }
        assertEquals(HttpStatus.CREATED, response.statusCode)
    }

    @Test
    fun `test that replacing an existing portfolio by a valid portfolio returns 200 response`() {
        doReturn(true).whenever(mockPortfolioService).existsPortfolioForUser(eq(dummyPortfolioId.toString()), any())

        val response = assertDoesNotThrow { portfolioController.replacePortfolio(dummyPortfolioId.toString(), validPortfolioUpload) }
        assertEquals(HttpStatus.OK, response.statusCode)
    }
}
