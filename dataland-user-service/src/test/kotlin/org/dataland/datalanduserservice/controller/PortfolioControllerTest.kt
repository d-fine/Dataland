package org.dataland.datalanduserservice.controller

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
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
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
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
    private val dummyCorrelationId = UUID.randomUUID().toString()
    private val dummyPortfolioId = UUID.randomUUID()
    private val dummyPortfolioName = "Test Portfolio"
    private val validCompanyId = "valid-company-id"
    private val invalidCompanyId = "invalid-company-id"

    private val validPortfolioUpload =
        PortfolioUpload(dummyPortfolioName, setOf(validCompanyId), setOf(DataTypeEnum.lksg))

    @BeforeEach
    fun setup() {
        this.resetSecurityContext()
        doNothing().whenever(mockValidator).validatePortfolioCreation(validPortfolioUpload, dummyCorrelationId)
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

    //  @Test
    // fun `test that creating a portfolio with existing name throws ConflictApiException`() {
    //   doReturn(true).whenever(mockPortfolioService).existsPortfolioWithNameForUser(dummyPortfolioName, dummyCorrelationId)
//
    //      assertThrows<ConflictApiException> { portfolioController.createPortfolio(validPortfolioUpload) }
    // }

    @Test
    fun `test that creating a valid portfolio returns 201 response`() {
        doReturn(false).whenever(mockPortfolioService).existsPortfolioWithNameForUser(dummyPortfolioName, dummyCorrelationId)
        val response = assertDoesNotThrow { portfolioController.createPortfolio(validPortfolioUpload) }
        assertEquals(HttpStatus.CREATED, response.statusCode)
    }

    // @ParameterizedTest
    // @ValueSource(ints = [400, 401, 402, 403, 405, 406, 407, 408, 409, 410])
    // fun `test that a ClientException other than 404 is passed through the isCompanyIdValid function`(statusCode: Int) {
    //    doThrow(ClientException(statusCode = statusCode)).whenever(mockCompanyDataController).isCompanyIdValid(validCompanyId)
//
    //      val exception = assertThrows<ClientException> { portfolioController.createPortfolio(validPortfolioUpload) }
    //    assertEquals(statusCode, exception.statusCode)
    // }

    @Test
    fun `test that replacing an nonexistent portfolio throws PortfolioNotFoundApiException`() {
        doReturn(false).whenever(mockPortfolioService).existsPortfolioForUser(dummyPortfolioId.toString(), dummyCorrelationId)
        assertThrows<PortfolioNotFoundApiException> {
            portfolioController.replacePortfolio(dummyPortfolioId.toString(), validPortfolioUpload)
        }
    }

    @Test
    fun `test that replacing an existing portfolio by portfolio with invalid companyId throws ResourceNotFoundException`() {
        val invalidPortfolioPayload = validPortfolioUpload.copy(companyIds = setOf(validCompanyId, invalidCompanyId))
        doReturn(true).whenever(mockPortfolioService).existsPortfolioForUser(dummyPortfolioId.toString(), dummyCorrelationId)

        assertThrows<ResourceNotFoundApiException> {
            portfolioController.replacePortfolio(dummyPortfolioId.toString(), invalidPortfolioPayload)
        }
    }

    @Test
    fun `test that replacing an existing portfolio by a portfolio with an invalid name throws ConflictApiException`() {
        doReturn(true).whenever(mockPortfolioService).existsPortfolioForUser(dummyPortfolioId.toString(), dummyCorrelationId)
        doReturn(true).whenever(mockPortfolioService).existsPortfolioWithNameForUser(dummyPortfolioName, dummyCorrelationId)

        assertThrows<ConflictApiException> { portfolioController.replacePortfolio(dummyPortfolioId.toString(), validPortfolioUpload) }
    }

    @Test
    fun `test that replacing an existing portfolio by a valid portfolio returns 200 response`() {
        doReturn(true).whenever(mockPortfolioService).existsPortfolioForUser(dummyPortfolioId.toString(), dummyCorrelationId)

        val response = assertDoesNotThrow { portfolioController.replacePortfolio(dummyPortfolioId.toString(), validPortfolioUpload) }
        assertEquals(HttpStatus.OK, response.statusCode)
    }
}
