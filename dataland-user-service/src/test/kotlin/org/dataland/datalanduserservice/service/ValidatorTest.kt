package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.dataland.datalanduserservice.utils.Validator
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

class ValidatorTest {
    private val mockCompanyDataController = mock<CompanyDataControllerApi>()
    private val mockPortfolioService = mock<PortfolioService>()
    private val mockSecurityContext = mock<SecurityContext>()

    private lateinit var mockAuthentication: DatalandAuthentication
    private lateinit var validator: Validator

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
        reset(mockPortfolioService, mockCompanyDataController)
        this.resetSecurityContext()
        doNothing().whenever(mockCompanyDataController).isCompanyIdValid(validCompanyId)
        doThrow(ClientException(statusCode = HttpStatus.NOT_FOUND.value()))
            .whenever(mockCompanyDataController)
            .isCompanyIdValid(invalidCompanyId)
        validator = Validator(mockCompanyDataController, mockPortfolioService)
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
    fun `test that validating a portfolioUpload with existing name throws throw ConflictApiException`() {
        doReturn(true)
            .whenever(mockPortfolioService)
            .existsPortfolioWithNameForUser(dummyPortfolioName, dummyCorrelationId)

        assertThrows<ConflictApiException> {
            validator.validatePortfolioCreation(
                validPortfolioUpload,
                dummyCorrelationId,
            )
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [400, 401, 402, 403, 405, 406, 407, 408, 409, 410])
    fun `test that a ClientException other than 404 is passed through the isCompanyIdValid function`(statusCode: Int) {
        doThrow(ClientException(statusCode = statusCode))
            .whenever(mockCompanyDataController)
            .isCompanyIdValid(validCompanyId)

        val exception =
            assertThrows<ClientException> {
                validator.validatePortfolioCreation(
                    validPortfolioUpload,
                    dummyCorrelationId,
                )
            }
        assertEquals(statusCode, exception.statusCode)
    }

    @Test
    fun `test that replacing an nonexistent portfolio throws PortfolioNotFoundApiException`() {
        doReturn(false)
            .whenever(mockPortfolioService)
            .existsPortfolioForUser(dummyPortfolioId.toString(), dummyCorrelationId)
        assertThrows<PortfolioNotFoundApiException> {
            validator.validatePortfolioReplacement(
                dummyPortfolioId.toString(),
                validPortfolioUpload,
                dummyCorrelationId,
            )
        }
    }

    @Test
    fun `test that replacing an existing portfolio by portfolio with invalid companyId throws ResourceNotFoundException`() {
        val invalidPortfolioPayload = validPortfolioUpload.copy(companyIds = setOf(validCompanyId, invalidCompanyId))
        doReturn(true)
            .whenever(mockPortfolioService)
            .existsPortfolioForUser(dummyPortfolioId.toString(), dummyCorrelationId)

        assertThrows<ResourceNotFoundApiException> {
            validator.validatePortfolioReplacement(
                dummyPortfolioId.toString(),
                invalidPortfolioPayload,
                dummyCorrelationId,
            )
        }
    }

    @Test
    fun `test that replacing an existing portfolio by a portfolio with an invalid name throws ConflictApiException`() {
        val invalidPortfolioName = "already-existing-name"
        doReturn(true)
            .whenever(mockPortfolioService)
            .existsPortfolioForUser(dummyPortfolioId.toString(), dummyCorrelationId)
        doReturn(BasePortfolio(validPortfolioUpload)).whenever(mockPortfolioService).getPortfolioForUser(any())
        doReturn(true)
            .whenever(mockPortfolioService)
            .existsPortfolioWithNameForUser(invalidPortfolioName, dummyCorrelationId)

        assertThrows<ConflictApiException> {
            validator
                .validatePortfolioReplacement(
                    dummyPortfolioId.toString(),
                    validPortfolioUpload.copy(portfolioName = invalidPortfolioName),
                    dummyCorrelationId,
                )
        }
    }
}
